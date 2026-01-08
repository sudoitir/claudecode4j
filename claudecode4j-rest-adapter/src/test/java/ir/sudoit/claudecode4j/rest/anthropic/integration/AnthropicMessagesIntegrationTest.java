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
package ir.sudoit.claudecode4j.rest.anthropic.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.api.model.request.PromptOptions;
import ir.sudoit.claudecode4j.api.model.response.StreamEvent;
import ir.sudoit.claudecode4j.api.model.response.TextResponse;
import ir.sudoit.claudecode4j.rest.anthropic.dto.request.TestMessageRequest;
import ir.sudoit.claudecode4j.rest.config.IntegrationTestConfig;
import ir.sudoit.claudecode4j.rest.streaming.SseTestUtils;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.publisher.Flux;

/**
 * Integration tests for the Anthropic Messages API. Tests the full HTTP request/response flow with mocked ClaudeClient.
 */
@SpringBootTest(classes = IntegrationTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AnthropicMessagesIntegrationTest {

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
    void shouldExecuteMessageAndReturnResponse() {
        // Given: mock ClaudeClient returns a response
        var expectedResponse = new TextResponse(
                "The answer is 42.", Instant.now(), Duration.ofMillis(500), "claude-3-5-haiku-20241022", 25, "msg-123");
        when(claudeClient.execute(any(Prompt.class), any(PromptOptions.class))).thenReturn(expectedResponse);

        var request = TestMessageRequest.simpleUserMessage("What is the meaning of life?");

        // When & Then: send HTTP POST request and verify response
        webTestClient
                .post()
                .uri("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id")
                .exists()
                .jsonPath("$.type")
                .isEqualTo("message")
                .jsonPath("$.model")
                .isEqualTo("claude-3-5-haiku-20241022")
                .jsonPath("$.content[0].text")
                .isEqualTo("The answer is 42.");
    }

    @Test
    void shouldExecuteStreamingMessage() {
        // Given: mock ClaudeClient returns a streaming response using cold publisher (Flux)
        var events = Flux.just(
                new StreamEvent(StreamEvent.EventType.ASSISTANT, "Start", Instant.now(), 1, null, null),
                new StreamEvent(StreamEvent.EventType.ASSISTANT, "Content", Instant.now(), 2, null, null),
                new StreamEvent(StreamEvent.EventType.COMPLETE, "End", Instant.now(), 3, null, null));

        when(claudeClient.stream(any(Prompt.class), any(PromptOptions.class)))
                .thenReturn(JdkFlowAdapter.publisherToFlowPublisher(events));

        var request = TestMessageRequest.streaming(true);

        // When & Then: send HTTP POST request and verify SSE stream
        var response = webTestClient
                .post()
                .uri("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType("text/event-stream");

        // Collect SSE events
        var sseEvents = SseTestUtils.consumeSseStream(response);

        // Verify SSE format compliance
        SseTestUtils.verifyAnthropicFormat(sseEvents);
    }

    @Test
    void shouldReturnBadRequestForEmptyMessages() {
        var request = TestMessageRequest.emptyMessages();

        // When & Then: send HTTP POST request with empty messages
        webTestClient
                .post()
                .uri("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void shouldReturnBadRequestForMissingMaxTokens() {
        var request = TestMessageRequest.missingMaxTokens();

        // When & Then: send HTTP POST request with missing max_tokens
        webTestClient
                .post()
                .uri("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void shouldReturnBadRequestForMissingModel() {
        var request = TestMessageRequest.missingModel();

        // When & Then: send HTTP POST request with missing model
        webTestClient
                .post()
                .uri("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void shouldHandleClaudeException() {
        // Given: mock ClaudeClient throws ClaudeException
        when(claudeClient.execute(any(Prompt.class), any(PromptOptions.class)))
                .thenThrow(new ir.sudoit.claudecode4j.api.exception.ClaudeExecutionException(
                        1, "Claude CLI failed", null));

        var request = TestMessageRequest.simpleUserMessage("This will fail");

        // When & Then: send HTTP POST request and verify error response
        webTestClient
                .post()
                .uri("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void shouldHandleGenericException() {
        // Given: mock ClaudeClient throws generic Exception
        when(claudeClient.execute(any(Prompt.class), any(PromptOptions.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        var request = TestMessageRequest.simpleUserMessage("This will fail");

        // When & Then: send HTTP POST request and verify error response
        webTestClient
                .post()
                .uri("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }
}
