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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ir.sudoit.claudecode4j.api.config.ClaudeConfig;
import ir.sudoit.claudecode4j.api.exception.ClaudeTimeoutException;
import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.api.model.request.PromptOptions;
import ir.sudoit.claudecode4j.api.model.response.TextResponse;
import ir.sudoit.claudecode4j.api.spi.InputSanitizer;
import ir.sudoit.claudecode4j.api.spi.OutputParser;
import ir.sudoit.claudecode4j.api.spi.ProcessExecutor;
import ir.sudoit.claudecode4j.api.spi.ProcessExecutor.ExecutionResult;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("DefaultClaudeClient")
@ExtendWith(MockitoExtension.class)
class DefaultClaudeClientTest {

    private static final Path BINARY_PATH = Path.of("/usr/local/bin/claude");

    @Mock
    private InputSanitizer sanitizer;

    @Mock
    private OutputParser parser;

    @Mock
    private ProcessExecutor executor;

    private DefaultClaudeClient client;

    @BeforeEach
    void setUp() {
        var config = ClaudeConfig.builder()
                .binaryPath(BINARY_PATH)
                .concurrencyLimit(2)
                .defaultTimeout(Duration.ofMinutes(5))
                .build();

        client = new DefaultClaudeClient(config, BINARY_PATH, sanitizer, parser, executor);
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("should execute prompt and return response")
        void shouldExecutePromptAndReturnResponse() {
            var prompt = Prompt.of("Hello, Claude!");
            var expectedResponse = new TextResponse("Hello!", Instant.now(), Duration.ofMillis(100), null, null, null);

            when(sanitizer.sanitize(prompt)).thenReturn(prompt);
            when(executor.execute(anyList(), any(Path.class), any(Duration.class), any()))
                    .thenReturn(new ExecutionResult(0, "Hello!", ""));
            when(parser.parse("Hello!", 0)).thenReturn(expectedResponse);

            var result = client.execute(prompt);

            assertThat(result).isEqualTo(expectedResponse);
            verify(sanitizer).sanitize(prompt);
        }

        @Test
        @DisplayName("should apply default options when not specified")
        void shouldApplyDefaultOptionsWhenNotSpecified() {
            var prompt = Prompt.of("Test");
            var expectedResponse = new TextResponse("Response", Instant.now(), Duration.ofMillis(50), null, null, null);

            when(sanitizer.sanitize(prompt)).thenReturn(prompt);
            when(executor.execute(anyList(), any(Path.class), any(Duration.class), any()))
                    .thenReturn(new ExecutionResult(0, "Response", ""));
            when(parser.parse("Response", 0)).thenReturn(expectedResponse);

            client.execute(prompt);

            verify(executor).execute(anyList(), any(Path.class), eq(Duration.ofMinutes(5)), any());
        }

        @Test
        @DisplayName("should use custom options when provided")
        void shouldUseCustomOptionsWhenProvided() {
            var prompt = Prompt.of("Test");
            var options =
                    PromptOptions.builder().timeout(Duration.ofMinutes(10)).build();
            var expectedResponse = new TextResponse("Response", Instant.now(), Duration.ofMillis(50), null, null, null);

            when(sanitizer.sanitize(prompt)).thenReturn(prompt);
            when(executor.execute(anyList(), any(Path.class), any(Duration.class), any()))
                    .thenReturn(new ExecutionResult(0, "Response", ""));
            when(parser.parse("Response", 0)).thenReturn(expectedResponse);

            client.execute(prompt, options);

            verify(executor).execute(anyList(), any(Path.class), eq(Duration.ofMinutes(10)), any());
        }

        @Test
        @DisplayName("should throw timeout exception when process times out")
        void shouldThrowTimeoutExceptionWhenProcessTimesOut() {
            var prompt = Prompt.of("Test");

            when(sanitizer.sanitize(prompt)).thenReturn(prompt);
            when(executor.execute(anyList(), any(Path.class), any(Duration.class), any()))
                    .thenReturn(new ExecutionResult(-1, "", "Timeout"));

            assertThatThrownBy(() -> client.execute(prompt)).isInstanceOf(ClaudeTimeoutException.class);
        }

        @Test
        @DisplayName("should throw exception when client is closed")
        void shouldThrowExceptionWhenClientIsClosed() {
            var prompt = Prompt.of("Test");

            client.close();

            assertThatThrownBy(() -> client.execute(prompt))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Client is closed");
        }

        @Test
        @DisplayName("should use working directory from prompt")
        void shouldUseWorkingDirectoryFromPrompt() {
            var workDir = Path.of("/custom/work/dir");
            var prompt = Prompt.builder().text("Test").workingDirectory(workDir).build();
            var expectedResponse = new TextResponse("Response", Instant.now(), Duration.ofMillis(50), null, null, null);

            when(sanitizer.sanitize(prompt)).thenReturn(prompt);
            when(executor.execute(anyList(), any(Path.class), any(Duration.class), any()))
                    .thenReturn(new ExecutionResult(0, "Response", ""));
            when(parser.parse("Response", 0)).thenReturn(expectedResponse);

            client.execute(prompt);

            verify(executor).execute(anyList(), eq(workDir), any(Duration.class), any());
        }
    }

    @Nested
    @DisplayName("executeAsync")
    class ExecuteAsync {

        @Test
        @DisplayName("should execute prompt asynchronously")
        void shouldExecutePromptAsynchronously() throws Exception {
            var prompt = Prompt.of("Async test");
            var expectedResponse =
                    new TextResponse("Async Response", Instant.now(), Duration.ofMillis(100), null, null, null);

            when(sanitizer.sanitize(prompt)).thenReturn(prompt);
            when(executor.execute(anyList(), any(Path.class), any(Duration.class), any()))
                    .thenReturn(new ExecutionResult(0, "Async Response", ""));
            when(parser.parse("Async Response", 0)).thenReturn(expectedResponse);

            var future = client.executeAsync(prompt);
            var result = future.get();

            assertThat(result).isEqualTo(expectedResponse);
        }
    }

    @Nested
    @DisplayName("isAvailable")
    class IsAvailable {

        @Test
        @DisplayName("should return true when CLI is available")
        void shouldReturnTrueWhenCliIsAvailable() {
            when(executor.execute(
                            eq(List.of(BINARY_PATH.toString(), "--version")),
                            any(Path.class),
                            eq(Duration.ofSeconds(10))))
                    .thenReturn(new ExecutionResult(0, "1.0.0", ""));

            assertThat(client.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("should return false when CLI is not available")
        void shouldReturnFalseWhenCliIsNotAvailable() {
            when(executor.execute(
                            eq(List.of(BINARY_PATH.toString(), "--version")),
                            any(Path.class),
                            eq(Duration.ofSeconds(10))))
                    .thenReturn(new ExecutionResult(1, "", "Not found"));

            assertThat(client.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("should return false when execution throws exception")
        void shouldReturnFalseWhenExecutionThrowsException() {
            when(executor.execute(
                            eq(List.of(BINARY_PATH.toString(), "--version")),
                            any(Path.class),
                            eq(Duration.ofSeconds(10))))
                    .thenThrow(new RuntimeException("Process failed"));

            assertThat(client.isAvailable()).isFalse();
        }
    }

    @Nested
    @DisplayName("getCliVersion")
    class GetCliVersion {

        @Test
        @DisplayName("should return CLI version when successful")
        void shouldReturnCliVersionWhenSuccessful() {
            when(executor.execute(
                            eq(List.of(BINARY_PATH.toString(), "--version")),
                            any(Path.class),
                            eq(Duration.ofSeconds(10))))
                    .thenReturn(new ExecutionResult(0, "claude 1.2.3\n", ""));

            assertThat(client.getCliVersion()).isEqualTo("claude 1.2.3");
        }

        @Test
        @DisplayName("should return unknown when CLI version check fails")
        void shouldReturnUnknownWhenCliVersionCheckFails() {
            when(executor.execute(
                            eq(List.of(BINARY_PATH.toString(), "--version")),
                            any(Path.class),
                            eq(Duration.ofSeconds(10))))
                    .thenReturn(new ExecutionResult(1, "", "Error"));

            assertThat(client.getCliVersion()).isEqualTo("unknown");
        }
    }

    @Nested
    @DisplayName("createSession")
    class CreateSession {

        @Test
        @DisplayName("should create a new session")
        void shouldCreateANewSession() {
            var session = client.createSession();

            assertThat(session).isNotNull();
        }

        @Test
        @DisplayName("should throw exception when creating session on closed client")
        void shouldThrowExceptionWhenCreatingSessionOnClosedClient() {
            client.close();

            assertThatThrownBy(() -> client.createSession())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Client is closed");
        }
    }

    @Nested
    @DisplayName("dangerouslySkipPermissions config")
    class DangerouslySkipPermissionsConfig {

        @Test
        @DisplayName("should apply config dangerouslySkipPermissions to options")
        void shouldApplyConfigDangerouslySkipPermissionsToOptions() {
            var config = ClaudeConfig.builder()
                    .binaryPath(BINARY_PATH)
                    .concurrencyLimit(2)
                    .defaultTimeout(Duration.ofMinutes(5))
                    .dangerouslySkipPermissions(true)
                    .build();

            var clientWithSkip = new DefaultClaudeClient(config, BINARY_PATH, sanitizer, parser, executor);
            var prompt = Prompt.of("Test");
            var options =
                    PromptOptions.builder().dangerouslySkipPermissions(false).build();
            var expectedResponse = new TextResponse("Response", Instant.now(), Duration.ofMillis(50), null, null, null);

            when(sanitizer.sanitize(prompt)).thenReturn(prompt);
            when(executor.execute(anyList(), any(Path.class), any(Duration.class), any()))
                    .thenReturn(new ExecutionResult(0, "Response", ""));
            when(parser.parse("Response", 0)).thenReturn(expectedResponse);

            clientWithSkip.execute(prompt, options);

            verify(executor).execute(anyList(), any(Path.class), any(Duration.class), any());
        }
    }
}
