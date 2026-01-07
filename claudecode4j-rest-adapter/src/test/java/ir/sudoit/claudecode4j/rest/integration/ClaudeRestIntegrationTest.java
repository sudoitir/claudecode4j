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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ClaudeRestIntegrationTest.class);

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
        log.info("══════════════════════════════════════════════════════════════");
        log.info("Setting up WebTestClient for http://localhost:{}", port);
        this.webTestClient =
                WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void shouldExecutePromptAndReturnResponse() {
        log.info("TEST: shouldExecutePromptAndReturnResponse");

        // Given: mock ClaudeClient returns a response
        var expectedResponse = new TextResponse(
                "The answer to 2+2 is 4.", Instant.now(), Duration.ofMillis(500), "haiku", 25, "session-123");
        when(claudeClient.execute(any(Prompt.class), any(PromptOptions.class))).thenReturn(expectedResponse);
        log.info("  Mock configured: ClaudeClient.execute() -> TextResponse(content='The answer to 2+2 is 4.')");

        var request = new PromptRequest("What is 2+2?", null, null, null, null, null, null, null, null, null, null);
        log.info("  Request: POST /api/claude/prompt with text='What is 2+2?'");

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
                .consumeWith(result -> log.info(
                        "  Response: {} - Body: {}", result.getStatus(), new String(result.getResponseBodyContent())))
                .jsonPath("$.content")
                .isEqualTo("The answer to 2+2 is 4.")
                .jsonPath("$.success")
                .isEqualTo(true)
                .jsonPath("$.model")
                .isEqualTo("haiku");
        log.info("  PASSED: Verified content, success, and model fields");
    }

    @Test
    void shouldExecuteAsyncPromptAndReturnResponse() {
        log.info("TEST: shouldExecuteAsyncPromptAndReturnResponse");

        // Given: mock ClaudeClient returns a response asynchronously
        var expectedResponse =
                new TextResponse("Async result here", Instant.now(), Duration.ofMillis(1000), "haiku", 50, null);
        when(claudeClient.executeAsync(any(Prompt.class), any(PromptOptions.class)))
                .thenReturn(CompletableFuture.completedFuture(expectedResponse));
        log.info("  Mock configured: ClaudeClient.executeAsync() -> CompletableFuture<TextResponse>");

        var request = new PromptRequest(
                "Process this asynchronously", null, null, null, null, null, null, null, null, null, null);
        log.info("  Request: POST /api/claude/prompt/async with text='Process this asynchronously'");

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
                .consumeWith(result -> log.info(
                        "  Response: {} - Body: {}", result.getStatus(), new String(result.getResponseBodyContent())))
                .jsonPath("$.content")
                .isEqualTo("Async result here")
                .jsonPath("$.success")
                .isEqualTo(true);
        log.info("  PASSED: Async endpoint returned expected response");
    }

    @Test
    void shouldReturnHealthStatus() {
        log.info("TEST: shouldReturnHealthStatus");

        // Given: mock ClaudeClient reports available
        when(claudeClient.isAvailable()).thenReturn(true);
        when(claudeClient.getCliVersion()).thenReturn("1.2.3");
        log.info("  Mock configured: isAvailable()=true, getCliVersion()='1.2.3'");
        log.info("  Request: GET /api/claude/health");

        // When & Then: send HTTP GET request to health endpoint
        webTestClient
                .get()
                .uri("/api/claude/health")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(result -> log.info(
                        "  Response: {} - Body: {}", result.getStatus(), new String(result.getResponseBodyContent())))
                .jsonPath("$.available")
                .isEqualTo(true)
                .jsonPath("$.version")
                .isEqualTo("1.2.3");
        log.info("  PASSED: Health endpoint returned available=true, version=1.2.3");
    }

    @Test
    void shouldReturnHealthStatusWhenUnavailable() {
        log.info("TEST: shouldReturnHealthStatusWhenUnavailable");

        // Given: mock ClaudeClient reports unavailable
        when(claudeClient.isAvailable()).thenReturn(false);
        log.info("  Mock configured: isAvailable()=false");
        log.info("  Request: GET /api/claude/health");

        // When & Then: send HTTP GET request to health endpoint
        webTestClient
                .get()
                .uri("/api/claude/health")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .consumeWith(result -> log.info(
                        "  Response: {} - Body: {}", result.getStatus(), new String(result.getResponseBodyContent())))
                .jsonPath("$.available")
                .isEqualTo(false)
                .jsonPath("$.version")
                .isEqualTo("unknown");
        log.info("  PASSED: Health endpoint returned available=false, version=unknown");
    }

    @Test
    void shouldReturnBadRequestForEmptyPrompt() {
        log.info("TEST: shouldReturnBadRequestForEmptyPrompt");

        var request = new PromptRequest("", null, null, null, null, null, null, null, null, null, null);
        log.info("  Request: POST /api/claude/prompt with empty text=''");

        // When & Then: send HTTP POST request with empty text
        webTestClient
                .post()
                .uri("/api/claude/prompt")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .consumeWith(result -> log.info("  Response: {} - Validation error expected", result.getStatus()));
        log.info("  PASSED: Empty prompt correctly rejected with 400 Bad Request");
    }

    @Test
    void shouldHandleClaudeExecutionException() {
        log.info("TEST: shouldHandleClaudeExecutionException");

        // Given: mock ClaudeClient throws ClaudeExecutionException
        when(claudeClient.execute(any(Prompt.class), any(PromptOptions.class)))
                .thenThrow(new ClaudeExecutionException(1, "Claude CLI failed"));
        log.info("  Mock configured: ClaudeClient.execute() throws ClaudeExecutionException(1, 'Claude CLI failed')");

        var request = new PromptRequest("This will fail", null, null, null, null, null, null, null, null, null, null);
        log.info("  Request: POST /api/claude/prompt with text='This will fail'");

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
                .consumeWith(result -> log.info(
                        "  Response: {} - Body: {}", result.getStatus(), new String(result.getResponseBodyContent())))
                .jsonPath("$.error")
                .isEqualTo("CLAUDE_EXECUTION_FAILED");
        log.info("  PASSED: Exception correctly mapped to error response with code CLAUDE_EXECUTION_FAILED");
    }

    @Test
    void shouldAcceptPromptWithAllOptions() {
        log.info("TEST: shouldAcceptPromptWithAllOptions");

        // Given: mock ClaudeClient returns a response
        var expectedResponse =
                new TextResponse("Full options response", Instant.now(), Duration.ofMillis(200), "haiku", 100, null);
        when(claudeClient.execute(any(Prompt.class), any(PromptOptions.class))).thenReturn(expectedResponse);
        log.info("  Mock configured: ClaudeClient.execute() -> TextResponse");

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
        log.info("  Request: POST /api/claude/prompt with full options:");
        log.info("    - text='Complex prompt with options'");
        log.info("    - systemPrompt='You are a helpful assistant'");
        log.info("    - workingDirectory='/tmp'");
        log.info("    - agentName='code-agent'");
        log.info("    - timeout=60s, model='haiku', maxTokens=4096");

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
                .consumeWith(result -> log.info(
                        "  Response: {} - Body: {}", result.getStatus(), new String(result.getResponseBodyContent())))
                .jsonPath("$.content")
                .isEqualTo("Full options response");
        log.info("  PASSED: All options accepted and response received");
    }
}
