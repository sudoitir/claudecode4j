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
package ir.sudoit.claudecode4j.rest.anthropic.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.rest.anthropic.dto.request.Message;
import ir.sudoit.claudecode4j.rest.anthropic.dto.request.MessageRequest;
import ir.sudoit.claudecode4j.rest.anthropic.dto.request.TestMessageRequest;
import ir.sudoit.claudecode4j.rest.config.E2ETestConfig;
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
 * End-to-End tests for the Anthropic Messages API that invoke the REAL Claude CLI. These tests require Claude CLI to be
 * installed and authenticated.
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
class AnthropicMessagesE2ETest {

    private static final Logger log = LoggerFactory.getLogger(AnthropicMessagesE2ETest.class);

    @LocalServerPort
    private int port;

    @Autowired
    private ClaudeClient claudeClient;

    private WebTestClient webTestClient;

    @BeforeAll
    static void checkClaudeCliAvailable() {
        log.info("╔══════════════════════════════════════════════════════════════╗");
        log.info("║          ClaudeCode4J Anthropic E2E Tests - Real Claude CLI    ║");
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
    void shouldExecuteRealNonStreamingMessage() {
        log.info("TEST: shouldExecuteRealNonStreamingMessage");
        log.info("  Sending real message request to Claude CLI via Anthropic endpoint...");

        var request = TestMessageRequest.simpleUserMessage("What is 2+2? Reply with just the number.");

        log.info("  Request: POST /v1/messages");
        log.info("    model: claude-3-5-haiku-20241022");
        log.info("    max_tokens: 1024");

        var response = webTestClient
                .post()
                .uri("/v1/messages")
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
        assertThat(response).contains("\"type\":\"message\"");
        assertThat(response).contains("\"model\":");
        assertThat(response).contains("\"content\"");
        assertThat(response).containsIgnoringCase("4");

        log.info("  PASSED: Real Claude CLI invoked successfully via Anthropic endpoint");
    }

    @Test
    void shouldExecuteRealStreamingMessage() {
        log.info("TEST: shouldExecuteRealStreamingMessage");
        log.info("  Sending real streaming message request to Claude CLI...");

        var request = TestMessageRequest.streaming(true);

        log.info("  Request: POST /v1/messages (stream=true)");

        var responseSpec = webTestClient
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
        var events = SseTestUtils.consumeSseStream(responseSpec);

        log.info("  Received {} SSE events", events.size());

        // Verify Anthropic SSE format compliance
        SseTestUtils.verifyAnthropicFormat(events);

        // Verify specific SSE format requirements
        boolean foundEventPrefix = events.stream().anyMatch(e -> e.contains("event:"));
        boolean foundDataPrefix = events.stream().anyMatch(e -> e.contains("data:"));
        boolean foundHeartbeat = events.stream().anyMatch(e -> e.trim().startsWith(":"));

        assertThat(foundEventPrefix).as("Should have event: prefix in events").isTrue();
        assertThat(foundDataPrefix).as("Should have data: prefix in events").isTrue();
        assertThat(foundHeartbeat).as("Should have heartbeat comments").isTrue();

        log.info("  PASSED: Real streaming message with full SSE compliance");
    }

    @Test
    void shouldExecuteRealMessageWithSystemPrompt() {
        log.info("TEST: shouldExecuteRealMessageWithSystemPrompt");
        log.info("  Sending real message with system prompt...");

        var request = TestMessageRequest.withSystemPrompt(
                "You are a math assistant. Be concise.", "What is 3+3? Reply with just the number.");

        log.info("  Request: POST /v1/messages");
        log.info("    system: You are a math assistant. Be concise.");

        var response = webTestClient
                .post()
                .uri("/v1/messages")
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

        log.info("  PASSED: Real message with system prompt handled correctly");
    }

    @Test
    void shouldExecuteRealMessageWithStopSequences() {
        log.info("TEST: shouldExecuteRealMessageWithStopSequences");
        log.info("  Sending real message with stop sequences...");

        var request = new MessageRequest(
                "claude-3-5-haiku-20241022",
                List.of(new Message("user", "Count from 1 to 5, then say END")),
                1024,
                null,
                null,
                null,
                null,
                false,
                List.of("END"),
                null,
                null,
                null);

        log.info("  Request: POST /v1/messages");
        log.info("    stop_sequences: [\"END\"]");

        var response = webTestClient
                .post()
                .uri("/v1/messages")
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
        assertThat(response).contains("\"type\":\"message\"");

        log.info("  PASSED: Real message with stop sequences handled correctly");
    }

    @Test
    void shouldExecuteRealMessageWithSamplingParameters() {
        log.info("TEST: shouldExecuteRealMessageWithSamplingParameters");
        log.info("  Sending real message with sampling parameters...");

        var request = TestMessageRequest.withSamplingParameters(0.7, 0.9, 40);

        log.info("  Request: POST /v1/messages");
        log.info("    temperature: 0.7, top_p: 0.9, top_k: 40");

        var response = webTestClient
                .post()
                .uri("/v1/messages")
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
        assertThat(response).contains("\"type\":\"message\"");

        log.info("  PASSED: Real message with sampling parameters handled correctly");
    }
}
