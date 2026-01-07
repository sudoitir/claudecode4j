/*
 * MIT License
 *
 * Copyright (c) 2026 Mahdi Amirabdollahi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ir.sudoit.claudecode4j.rest.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.api.exception.ClaudeExecutionException;
import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.api.model.request.PromptOptions;
import ir.sudoit.claudecode4j.api.model.response.TextResponse;
import ir.sudoit.claudecode4j.rest.dto.PromptRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

/** Integration tests for the Claude REST API. Tests the full HTTP request/response flow with mocked ClaudeClient. */
@SpringBootTest(
        classes = ClaudeRestIntegrationTest.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClaudeRestIntegrationTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = "ir.sudoit.claudecode4j.rest")
    static class TestConfig {}

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @MockitoBean
    private ClaudeClient claudeClient;

    @BeforeEach
    void setUp() {
        this.webTestClient =
                WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void shouldExecutePromptAndReturnResponse() {
        // Given: mock ClaudeClient returns a response
        var expectedResponse = new TextResponse(
                "The answer to 2+2 is 4.", Instant.now(), Duration.ofMillis(500), "haiku", 25, "session-123");
        when(claudeClient.execute(any(Prompt.class), any(PromptOptions.class))).thenReturn(expectedResponse);

        var request = new PromptRequest("What is 2+2?", null, null, null, null, null, null, null, null, null, null);

        // When & Then: send HTTP POST request and verify response
        webTestClient
                .post()
                .uri("/api/claude/prompt")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.content")
                .isEqualTo("The answer to 2+2 is 4.")
                .jsonPath("$.success")
                .isEqualTo(true)
                .jsonPath("$.model")
                .isEqualTo("haiku");
    }

    @Test
    void shouldExecuteAsyncPromptAndReturnResponse() {
        // Given: mock ClaudeClient returns a response asynchronously
        var expectedResponse =
                new TextResponse("Async result here", Instant.now(), Duration.ofMillis(1000), "haiku", 50, null);
        when(claudeClient.executeAsync(any(Prompt.class), any(PromptOptions.class)))
                .thenReturn(CompletableFuture.completedFuture(expectedResponse));

        var request = new PromptRequest(
                "Process this asynchronously", null, null, null, null, null, null, null, null, null, null);

        // When & Then: send HTTP POST request to async endpoint
        webTestClient
                .post()
                .uri("/api/claude/prompt/async")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.content")
                .isEqualTo("Async result here")
                .jsonPath("$.success")
                .isEqualTo(true);
    }

    @Test
    void shouldReturnHealthStatus() {
        // Given: mock ClaudeClient reports available
        when(claudeClient.isAvailable()).thenReturn(true);
        when(claudeClient.getCliVersion()).thenReturn("1.2.3");

        // When & Then: send HTTP GET request to health endpoint
        webTestClient
                .get()
                .uri("/api/claude/health")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.available")
                .isEqualTo(true)
                .jsonPath("$.version")
                .isEqualTo("1.2.3");
    }

    @Test
    void shouldReturnHealthStatusWhenUnavailable() {
        // Given: mock ClaudeClient reports unavailable
        when(claudeClient.isAvailable()).thenReturn(false);

        // When & Then: send HTTP GET request to health endpoint
        webTestClient
                .get()
                .uri("/api/claude/health")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.available")
                .isEqualTo(false)
                .jsonPath("$.version")
                .isEqualTo("unknown");
    }

    @Test
    void shouldReturnBadRequestForEmptyPrompt() {
        var request = new PromptRequest("", null, null, null, null, null, null, null, null, null, null);

        // When & Then: send HTTP POST request with empty text
        webTestClient
                .post()
                .uri("/api/claude/prompt")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void shouldHandleClaudeExecutionException() {
        // Given: mock ClaudeClient throws ClaudeExecutionException
        when(claudeClient.execute(any(Prompt.class), any(PromptOptions.class)))
                .thenThrow(new ClaudeExecutionException(1, "Claude CLI failed"));

        var request = new PromptRequest("This will fail", null, null, null, null, null, null, null, null, null, null);

        // When & Then: send HTTP POST request and verify error response
        webTestClient
                .post()
                .uri("/api/claude/prompt")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.error")
                .isEqualTo("CLAUDE_EXECUTION_FAILED");
    }

    @Test
    void shouldAcceptPromptWithAllOptions() {
        // Given: mock ClaudeClient returns a response
        var expectedResponse =
                new TextResponse("Full options response", Instant.now(), Duration.ofMillis(200), "haiku", 100, null);
        when(claudeClient.execute(any(Prompt.class), any(PromptOptions.class))).thenReturn(expectedResponse);

        var request = new PromptRequest(
                "Complex prompt with options",
                "You are a helpful assistant",
                null,
                "/tmp",
                "code-agent",
                60L,
                "TEXT",
                "haiku",
                false,
                true,
                4096);

        // When & Then: send HTTP POST request with all options
        webTestClient
                .post()
                .uri("/api/claude/prompt")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.content")
                .isEqualTo("Full options response");
    }
}
