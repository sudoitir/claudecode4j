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
package ir.sudoit.claudecode4j.rest.openai.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.api.model.request.PromptOptions;
import ir.sudoit.claudecode4j.api.model.response.StreamEvent;
import ir.sudoit.claudecode4j.api.model.response.TextResponse;
import ir.sudoit.claudecode4j.rest.config.IntegrationTestConfig;
import ir.sudoit.claudecode4j.rest.openai.dto.request.TestChatCompletionRequest;
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
 * Integration tests for the OpenAI Chat Completions API. Tests the full HTTP request/response flow with mocked
 * ClaudeClient.
 */
@SpringBootTest(classes = IntegrationTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenAiChatIntegrationTest {

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
    void shouldExecuteChatCompletionAndReturnResponse() {
        // Given: mock ClaudeClient returns a response
        var expectedResponse = new TextResponse(
                "The answer is 42.", Instant.now(), Duration.ofMillis(500), "haiku", 25, "session-123");
        when(claudeClient.execute(any(Prompt.class), any(PromptOptions.class))).thenReturn(expectedResponse);

        var request = TestChatCompletionRequest.simpleUserMessage("What is the meaning of life?");

        // When & Then: send HTTP POST request and verify response
        webTestClient
                .post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id")
                .exists()
                .jsonPath("$.object")
                .isEqualTo("chat.completion")
                .jsonPath("$.model")
                .isEqualTo("haiku")
                .jsonPath("$.choices[0].message.content")
                .isEqualTo("The answer is 42.");
    }

    @Test
    void shouldExecuteStreamingChatCompletion() {
        // Given: mock ClaudeClient returns a streaming response using cold publisher (Flux)
        var events = Flux.just(
                new StreamEvent(StreamEvent.EventType.ASSISTANT, "Start", Instant.now(), 1, null, null),
                new StreamEvent(StreamEvent.EventType.ASSISTANT, "Content", Instant.now(), 2, null, null),
                new StreamEvent(StreamEvent.EventType.COMPLETE, "End", Instant.now(), 3, null, null));

        when(claudeClient.stream(any(Prompt.class), any(PromptOptions.class)))
                .thenReturn(JdkFlowAdapter.publisherToFlowPublisher(events));

        var request = TestChatCompletionRequest.streaming(true);

        // When & Then: send HTTP POST request and verify SSE stream
        var response = webTestClient
                .post()
                .uri("/v1/chat/completions")
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
        SseTestUtils.verifyOpenAiFormat(sseEvents);
    }

    @Test
    void shouldExecuteChatWithSystemMessage() {
        // Given: mock ClaudeClient returns a response
        var expectedResponse = new TextResponse(
                "You are a helpful assistant.", Instant.now(), Duration.ofMillis(300), "haiku", 15, null);
        when(claudeClient.execute(any(Prompt.class), any(PromptOptions.class))).thenReturn(expectedResponse);

        var request = TestChatCompletionRequest.withSystemMessage("You are a helpful assistant.", "Hello!");

        // When & Then: send HTTP POST request and verify response
        webTestClient
                .post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.choices[0].message.content")
                .isEqualTo("You are a helpful assistant.");
    }

    @Test
    void shouldReturnBadRequestForEmptyMessages() {
        var request = TestChatCompletionRequest.emptyMessages();

        // When & Then: send HTTP POST request with empty messages
        webTestClient
                .post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void shouldReturnBadRequestForMissingModel() {
        var request = TestChatCompletionRequest.missingModel();

        // When & Then: send HTTP POST request with missing model
        webTestClient
                .post()
                .uri("/v1/chat/completions")
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

        var request = TestChatCompletionRequest.simpleUserMessage("This will fail");

        // When & Then: send HTTP POST request and verify error response
        webTestClient
                .post()
                .uri("/v1/chat/completions")
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

        var request = TestChatCompletionRequest.simpleUserMessage("This will fail");

        // When & Then: send HTTP POST request and verify error response
        webTestClient
                .post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    void shouldPassTemperatureParameter() {
        // Given: mock ClaudeClient returns a response
        var expectedResponse = new TextResponse("Response", Instant.now(), Duration.ofMillis(200), "haiku", 10, null);
        when(claudeClient.execute(any(Prompt.class), any(PromptOptions.class))).thenReturn(expectedResponse);

        var request = TestChatCompletionRequest.withTemperature(0.7, 0.9);

        // When & Then: send HTTP POST request
        webTestClient
                .post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void shouldPassMaxTokensParameter() {
        // Given: mock ClaudeClient returns a response
        var expectedResponse = new TextResponse("Response", Instant.now(), Duration.ofMillis(200), "haiku", 10, null);
        when(claudeClient.execute(any(Prompt.class), any(PromptOptions.class))).thenReturn(expectedResponse);

        var request = TestChatCompletionRequest.withMaxTokens(2048);

        // When & Then: send HTTP POST request
        webTestClient
                .post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isOk();
    }
}
