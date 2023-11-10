package com.redpharm.takehomechallenge.controller;

import com.redpharm.takehomechallenge.contract.GitResponseDTO;
import com.redpharm.takehomechallenge.service.GitHubService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private final GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    /**
     * Endpoint to fetch the most popular repositories, optionally filtered by language and date.
     *
     * @param count     The number of repositories to retrieve.
     * @param language  The programming language to filter by.
     * @param since     The date from which to find repositories.
     * @return A list of popular repositories.
     */
    @GetMapping("/repositories")
    public Mono<ResponseEntity<GitResponseDTO>> getPopularRepositories(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate since) {

        return gitHubService.getPopularRepositories(count, language, since)
                .map(repos -> ResponseEntity.ok().body(repos))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}

