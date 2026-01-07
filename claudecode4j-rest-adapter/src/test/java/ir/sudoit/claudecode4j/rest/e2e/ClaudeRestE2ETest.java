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
package ir.sudoit.claudecode4j.rest.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.rest.dto.PromptRequest;
import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * End-to-End tests that invoke the REAL Claude CLI. These tests require Claude CLI to be installed and authenticated.
 * Run with: mvn test -Dgroups=e2e Skip with: mvn test -DexcludedGroups=e2e
 */
@Tag("e2e")
@SpringBootTest(
        classes = ClaudeRestE2ETest.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
            "claude.code.enabled=true",
            "claude.code.dangerously-skip-permissions=true",
            "claude.code.default-timeout=2m"
        })
class ClaudeRestE2ETest {

    private static final Logger log = LoggerFactory.getLogger(ClaudeRestE2ETest.class);

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = "ir.sudoit.claudecode4j")
    static class TestConfig {}

    @LocalServerPort
    private int port;

    @Autowired
    private ClaudeClient claudeClient;

    private WebTestClient webTestClient;

    @BeforeAll
    static void checkClaudeCliAvailable() {
        log.info("╔══════════════════════════════════════════════════════════════╗");
        log.info("║           ClaudeCode4J E2E Tests - Real Claude CLI           ║");
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
    void shouldExecuteRealPromptViaRest() {
        log.info("TEST: shouldExecuteRealPromptViaRest");
        log.info("  Sending real prompt to Claude CLI via REST API...");

        var request = new PromptRequest(
                "What is 2+2? Reply with just the number.",
                null,
                null,
                null,
                null,
                60L,
                "TEXT",
                "haiku",
                true, // dangerouslySkipPermissions - REQUIRED for non-interactive
                true,
                null); // no maxTurns limit

        log.info("  Request: POST /api/claude/prompt");
        log.info("    text: 'What is 2+2? Reply with just the number.'");
        log.info("    model: haiku, timeout: 60s, printMode: true");

        var response = webTestClient
                .post()
                .uri("/api/claude/prompt")
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
        assertThat(response).contains("\"success\":true");
        assertThat(response).contains("\"content\":");
        // The response should contain "4" somewhere in the content
        assertThat(response).containsIgnoringCase("4");

        log.info("  PASSED: Real Claude CLI invoked successfully via REST");
    }

    @Test
    void shouldExecuteRealAsyncPromptViaRest() {
        log.info("TEST: shouldExecuteRealAsyncPromptViaRest");
        log.info("  Sending real async prompt to Claude CLI via REST API...");

        var request = new PromptRequest(
                "What is the capital of France? Reply with just the city name.",
                null,
                null,
                null,
                null,
                60L,
                "TEXT",
                "haiku",
                true, // dangerouslySkipPermissions - REQUIRED for non-interactive
                true,
                null); // no maxTurns limit

        log.info("  Request: POST /api/claude/prompt/async");
        log.info("    text: 'What is the capital of France? Reply with just the city name.'");

        var response = webTestClient
                .post()
                .uri("/api/claude/prompt/async")
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
        assertThat(response).contains("\"success\":true");
        assertThat(response).containsIgnoringCase("Paris");

        log.info("  PASSED: Real Claude CLI async invocation successful via REST");
    }

    @Test
    void shouldReturnRealHealthStatus() {
        log.info("TEST: shouldReturnRealHealthStatus");
        log.info("  Checking real health endpoint...");

        var response = webTestClient
                .get()
                .uri("/api/claude/health")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        log.info("  Response received: {}", response);

        assertThat(response).isNotNull();
        assertThat(response).contains("\"available\":true");
        assertThat(response).contains("\"version\":");
        assertThat(response).doesNotContain("\"version\":\"unknown\"");

        log.info("  PASSED: Health endpoint returns real CLI status");
    }

    @Test
    void shouldHandleComplexPrompt() {
        log.info("TEST: shouldHandleComplexPrompt");
        log.info("  Sending complex prompt with system instructions...");

        var request = new PromptRequest(
                "List 3 prime numbers less than 10, comma separated.",
                "You are a math assistant. Be concise.",
                null,
                null,
                null,
                90L,
                "TEXT",
                "haiku",
                true, // dangerouslySkipPermissions - REQUIRED for non-interactive
                true,
                null); // no maxTurns limit

        log.info("  Request: POST /api/claude/prompt");
        log.info("    text: 'List 3 prime numbers less than 10, comma separated.'");
        log.info("    systemPrompt: 'You are a math assistant. Be concise.'");

        var response = webTestClient
                .post()
                .uri("/api/claude/prompt")
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
        assertThat(response).contains("\"success\":true");
        // Should contain at least one prime number
        assertThat(response).containsAnyOf("2", "3", "5", "7");

        log.info("  PASSED: Complex prompt with system instructions handled correctly");
    }
}
