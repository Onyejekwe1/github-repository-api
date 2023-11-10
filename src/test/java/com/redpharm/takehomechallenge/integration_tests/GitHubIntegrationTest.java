package com.redpharm.takehomechallenge.integration_tests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GitHubIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void whenGetPopularRepositories_thenStatus200() {
        // Act
        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/api/github/repositories?count=10", String.class);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void whenGetPopularRepositories_thenResponseBodyIsCorrect() {
        // Act
        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/api/github/repositories?count=10", String.class);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
    }

    @Test
    void whenGetPopularRepositoriesWithInvalidParameters_thenClientError() {
        // Act
        ResponseEntity<String> responseEntity = restTemplate.getForEntity("/api/github/repositories?count=invalid", String.class);

        // Assert
        assertTrue(responseEntity.getStatusCode().is4xxClientError());
    }

}

