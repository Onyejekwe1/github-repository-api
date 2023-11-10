package com.redpharm.takehomechallenge.service;

import com.redpharm.takehomechallenge.cache.CacheService;
import com.redpharm.takehomechallenge.contract.GitResponseDTO;
import com.redpharm.takehomechallenge.exception.CustomException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class GitHubService {
    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;
    private final CacheService cacheService;

    public GitHubService(WebClient.Builder webClientBuilder, CircuitBreakerRegistry circuitBreakerRegistry, CacheService cacheService) {
        this.webClient = webClientBuilder.baseUrl("https://api.github.com/search").build();
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("github");
        this.cacheService = cacheService;
    }

    public Mono<GitResponseDTO> getPopularRepositories(int count, String language, LocalDate since) {
        String cacheKey = buildCacheKey(count, language);

        // Retrieve the cached data if available
        GitResponseDTO cachedResponse = cacheService.getFromCache("popular-repositories", cacheKey, GitResponseDTO.class);


        // If the cached data is available, return it wrapped in a Mono
        if (cachedResponse != null) {
            return Mono.just(cachedResponse);
        }

        // If the cache is empty, fetch from the web client and cache the result
        return fetchRepositories(count, language, since)
                .doOnSuccess(response -> {
                    if (response != null) {
                        cacheService.putIntoCache("popular-repositories", cacheKey, response);
                    }
                })
                .onErrorResume(throwable -> fallbackMethod(count, language, throwable));
    }

    private Mono<GitResponseDTO> fetchRepositories(int count, String language, LocalDate since) {
        String uri = buildUri(count, language, since);

        return webClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        Mono.error(new CustomException("Client error"))
                )
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        Mono.error(new CustomException("Server error"))
                )
                .bodyToMono(GitResponseDTO.class)
                .transform(CircuitBreakerOperator.of(circuitBreaker))
                // It's important to handle errors after circuit breaker transformation
                .onErrorResume(throwable -> fallbackMethod(count, language, throwable));
    }


    private String buildCacheKey(int count, String language) {
        return count + "-" + (language != null ? language : "none");
    }

    private Mono<GitResponseDTO> fallbackMethod(int count, String language, Throwable throwable) {
        String cacheKey = count + "-" + language;
        GitResponseDTO cachedResponse = cacheService.getFromCache("popular-repositories", cacheKey, GitResponseDTO.class);
        if (cachedResponse != null) {
            return Mono.just(cachedResponse);
        }
        // If there's no cached response, return an error or empty Mono
        return Mono.error(new RuntimeException("Service unavailable and no cached value found."));
    }

    private String buildUri(int count, String language, LocalDate since) {
        String dateString = since != null ? since.format(DateTimeFormatter.ISO_DATE) : "1970-01-01";

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/repositories")
                .queryParam("sort", "stars")
                .queryParam("order", "desc")
                .queryParam("per_page", count);

        String query = "created:>" + dateString;
        if (language != null && !language.isEmpty()) {
            query += "+language:" + language;
        }
        return uriBuilder.queryParam("q", query).build().toString();
    }
}
