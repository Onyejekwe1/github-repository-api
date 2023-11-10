package com.redpharm.takehomechallenge.unit_tests;

import com.redpharm.takehomechallenge.cache.CacheService;
import com.redpharm.takehomechallenge.contract.GitResponseDTO;
import com.redpharm.takehomechallenge.exception.CustomException;
import com.redpharm.takehomechallenge.service.GitHubService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
class GitHubServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private CacheService cacheService;

    private GitHubService gitHubService;

    @BeforeEach
    void setUp() {
        // Create the mocks for WebClient behavior
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = Mockito.mock(WebClient.ResponseSpec.class);

        // Mock the onStatus method to return the responseSpec itself
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        // Set up the WebClient.Builder
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        // Mock the chain of method calls made by WebClient.get()
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(Mockito.<String>any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Mockito.<Class<GitResponseDTO>>any())).thenReturn(Mono.just(new GitResponseDTO()));

        // Create an instance of GitHubService with mocked dependencies
        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        gitHubService = new GitHubService(webClientBuilder, circuitBreakerRegistry, cacheService);

    }

    @Test
    void whenGetPopularRepositoriesAndCacheIsEmpty_thenFetchFromApi() {
        // Arrange
        GitResponseDTO mockResponse = new GitResponseDTO(); // populate with test data
        when(cacheService.getFromCache("popular-repositories", "10-Java", GitResponseDTO.class))
                .thenReturn(null);

        // Act
        Mono<GitResponseDTO> result = gitHubService.getPopularRepositories(10, "Java", LocalDate.now());

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(responseDTO ->
                        responseDTO.incomplete_results == mockResponse.incomplete_results &&
                                responseDTO.total_count == mockResponse.total_count
                ) // Custom comparison logic
                .verifyComplete();

        // Verify WebClient was used since cache was empty
        verify(webClient).get();
        // You might need to perform more granular verification depending on the behavior you expect.
    }

    @Test
    void whenGetPopularRepositoriesAndCacheIsNotEmpty_thenUseCachedData() {
        // Arrange
        GitResponseDTO cachedResponse = new GitResponseDTO(); // populate with test data
        cachedResponse.total_count = 42; // Example data
        when(cacheService.getFromCache("popular-repositories", "10-Java", GitResponseDTO.class))
                .thenReturn(cachedResponse);

        // Act
        Mono<GitResponseDTO> result = gitHubService.getPopularRepositories(10, "Java", LocalDate.now());

        // Assert
        StepVerifier.create(result)
                .expectNext(cachedResponse)
                .verifyComplete();

        // Verify WebClient was not used since cache was not empty
        verify(webClient, never()).get();
    }

}

