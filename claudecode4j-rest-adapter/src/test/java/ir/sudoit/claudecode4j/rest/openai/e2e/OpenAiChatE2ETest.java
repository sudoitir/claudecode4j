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
package ir.sudoit.claudecode4j.rest.openai.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.rest.config.E2ETestConfig;
import ir.sudoit.claudecode4j.rest.openai.dto.request.ChatCompletionRequest;
import ir.sudoit.claudecode4j.rest.openai.dto.request.ChatMessage;
import ir.sudoit.claudecode4j.rest.streaming.SseTestUtils;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * End-to-End tests for the OpenAI Chat Completions API that invoke the REAL Claude CLI. These tests require Claude CLI
 * to be installed and authenticated.
 *
 * <p>Run with: mvn test -Dgroups=e2e -Pall Skip with: mvn test -DexcludedGroups=e2e
 */
@Tag("e2e")
@SpringBootTest(classes = E2ETestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
            "claude.code.enabled=true",
            "claude.code.dangerously-skip-permissions=true",
            "claude.code.default-timeout=2m"
        })
class OpenAiChatE2ETest {

    private static final Logger log = LoggerFactory.getLogger(OpenAiChatE2ETest.class);

    @LocalServerPort
    private int port;

    @Autowired
    private ClaudeClient claudeClient;

    private WebTestClient webTestClient;

    @BeforeAll
    static void checkClaudeCliAvailable() {
        log.info("╔══════════════════════════════════════════════════════════════╗");
        log.info("║           ClaudeCode4J OpenAI E2E Tests - Real Claude CLI      ║");
        log.info("╚══════════════════════════════════════════════════════════════╝");
    }

    @BeforeEach
    void setUp() {
        log.info("──────────────────────────────────────────────────────────────");
        log.info("Checking Claude CLI availability...");

        boolean available = claudeClient.isAvailable();
        String version = available ? claudeClient.getCliVersion() : "N/A";

        log.info("Claude CLI available: {}", available);
        log.info("Claude CLI version: {}", version);

        assumeTrue(available, "Claude CLI is not available - skipping E2E tests");

        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofMinutes(2))
                .build();

        log.info("WebTestClient configured for http://localhost:{}", port);
    }

    @Test
    void shouldExecuteRealNonStreamingChatCompletion() {
        log.info("TEST: shouldExecuteRealNonStreamingChatCompletion");
        log.info("  Sending real chat completion request to Claude CLI via OpenAI endpoint...");

        var request = new ChatCompletionRequest(
                List.of(new ChatMessage("user", "What is 2+2? Reply with just the number.", null, null, null)),
                "haiku",
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null);

        log.info("  Request: POST /v1/chat/completions");
        log.info("    model: haiku");
        log.info("    messages: user message");

        var response = webTestClient
                .post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        log.info("  Response received: {}", response);

        assertThat(response).isNotNull();
        assertThat(response).contains("\"object\":\"chat.completion\"");
        assertThat(response).contains("\"model\":");
        assertThat(response).contains("\"choices\"");
        assertThat(response).containsIgnoringCase("4");

        log.info("  PASSED: Real Claude CLI invoked successfully via OpenAI endpoint");
    }

    @Test
    void shouldExecuteRealStreamingChatCompletion() {
        log.info("TEST: shouldExecuteRealStreamingChatCompletion");
        log.info("  Sending real streaming chat completion request to Claude CLI...");

        var request = new ChatCompletionRequest(
                List.of(new ChatMessage("user", "Count from 1 to 3, one number per line.", null, null, null)),
                "haiku",
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                null,
                null,
                null);

        log.info("  Request: POST /v1/chat/completions (stream=true)");

        var responseSpec = webTestClient
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
        var events = SseTestUtils.consumeSseStream(responseSpec);

        log.info("  Received {} SSE events", events.size());

        // Verify SSE format compliance
        SseTestUtils.verifyOpenAiFormat(events);

        // Verify specific SSE format requirements
        boolean foundDataPrefix = events.stream().anyMatch(e -> e.trim().startsWith("data:"));
        boolean foundDone = events.stream().anyMatch(e -> e.contains("[DONE]"));
        boolean foundHeartbeat = events.stream().anyMatch(e -> e.trim().startsWith(":"));

        assertThat(foundDataPrefix).as("Should have data: prefix in events").isTrue();
        assertThat(foundDone).as("Should have [DONE] marker").isTrue();
        assertThat(foundHeartbeat).as("Should have heartbeat comments").isTrue();

        log.info("  PASSED: Real streaming chat completion with full SSE compliance");
    }

    @Test
    void shouldExecuteRealChatWithSystemMessage() {
        log.info("TEST: shouldExecuteRealChatWithSystemMessage");
        log.info("  Sending real chat with system message...");

        var request = new ChatCompletionRequest(
                List.of(
                        new ChatMessage("system", "You are a math assistant. Be concise.", null, null, null),
                        new ChatMessage("user", "What is 3+3? Reply with just the number.", null, null, null)),
                "haiku",
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null);

        log.info("  Request: POST /v1/chat/completions");
        log.info("    messages: system + user");

        var response = webTestClient
                .post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        log.info("  Response received: {}", response);

        assertThat(response).isNotNull();
        assertThat(response).containsIgnoringCase("6");

        log.info("  PASSED: Real chat with system message handled correctly");
    }

    @Test
    void shouldExecuteRealChatWithTemperature() {
        log.info("TEST: shouldExecuteRealChatWithTemperature");
        log.info("  Sending real chat with temperature parameter...");

        var request = new ChatCompletionRequest(
                List.of(new ChatMessage("user", "Say hello", null, null, null)),
                "haiku",
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                0.5,
                null,
                null);

        log.info("  Request: POST /v1/chat/completions");
        log.info("    temperature: 0.5");

        var response = webTestClient
                .post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        log.info("  Response received: {}", response);

        assertThat(response).isNotNull();
        assertThat(response).contains("\"object\":\"chat.completion\"");

        log.info("  PASSED: Real chat with temperature parameter handled correctly");
    }
}
