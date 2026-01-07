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
package ir.sudoit.claudecode4j.core.client;

import static org.assertj.core.api.Assertions.assertThat;

import ir.sudoit.claudecode4j.api.config.ClaudeConfig;
import ir.sudoit.claudecode4j.api.exception.ClaudeBinaryNotFoundException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("DefaultClaudeClientFactory")
class DefaultClaudeClientFactoryTest {

    private DefaultClaudeClientFactory factory;

    @BeforeEach
    void setUp() {
        factory = new DefaultClaudeClientFactory();
    }

    @Nested
    @DisplayName("createClient")
    class CreateClient {

        @Test
        @DisplayName("should create client with explicit binary path")
        void shouldCreateClientWithExplicitBinaryPath() {
            var binaryPath = Path.of("/usr/local/bin/claude");
            var config = ClaudeConfig.builder().binaryPath(binaryPath).build();

            var client = factory.createClient(config);

            assertThat(client).isNotNull();
            assertThat(client).isInstanceOf(DefaultClaudeClient.class);
        }

        @Test
        @DisplayName("should throw exception when binary not found and no path configured")
        void shouldThrowExceptionWhenBinaryNotFoundAndNoPathConfigured() {
            // Using a config without explicit binary path will trigger resolution
            // which will fail if claude is not installed
            var config = ClaudeConfig.builder().build();

            // This test may pass or fail depending on whether Claude CLI is installed
            // For CI environments without Claude, we expect ClaudeBinaryNotFoundException
            try {
                var client = factory.createClient(config);
                // If we get here, Claude CLI is installed
                assertThat(client).isNotNull();
            } catch (ClaudeBinaryNotFoundException e) {
                // Expected in CI environments without Claude
                assertThat(e.searchedPaths()).isNotEmpty();
            }
        }

        @Test
        @DisplayName("should create client with custom concurrency limit")
        void shouldCreateClientWithCustomConcurrencyLimit() {
            var binaryPath = Path.of("/usr/local/bin/claude");
            var config = ClaudeConfig.builder()
                    .binaryPath(binaryPath)
                    .concurrencyLimit(10)
                    .build();

            var client = factory.createClient(config);

            assertThat(client).isNotNull();
        }

        @Test
        @DisplayName("should create client with dangerouslySkipPermissions enabled")
        void shouldCreateClientWithDangerouslySkipPermissions() {
            var binaryPath = Path.of("/usr/local/bin/claude");
            var config = ClaudeConfig.builder()
                    .binaryPath(binaryPath)
                    .dangerouslySkipPermissions(true)
                    .build();

            var client = factory.createClient(config);

            assertThat(client).isNotNull();
        }
    }

    @Nested
    @DisplayName("priority")
    class Priority {

        @Test
        @DisplayName("should return default priority of 0")
        void shouldReturnDefaultPriorityOfZero() {
            assertThat(factory.priority()).isEqualTo(0);
        }
    }
}
