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
package ir.sudoit.claudecode4j.core.process;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

@DisplayName("VirtualThreadExecutor")
class VirtualThreadExecutorTest {

    private VirtualThreadExecutor executor;
    private Path workingDirectory;

    @BeforeEach
    void setUp() {
        executor = new VirtualThreadExecutor();
        workingDirectory = Path.of(System.getProperty("user.dir"));
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("should execute simple command successfully")
        @DisabledOnOs(OS.WINDOWS)
        void shouldExecuteSimpleCommandSuccessfully() {
            var command = List.of("echo", "Hello World");
            var result = executor.execute(command, workingDirectory, Duration.ofSeconds(10));

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.exitCode()).isEqualTo(0);
            assertThat(result.stdout()).contains("Hello World");
        }

        @Test
        @DisplayName("should capture stderr for failing command")
        @DisabledOnOs(OS.WINDOWS)
        void shouldCaptureStderrForFailingCommand() {
            var command = List.of("ls", "/nonexistent/path/that/does/not/exist");
            var result = executor.execute(command, workingDirectory, Duration.ofSeconds(10));

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.exitCode()).isNotEqualTo(0);
        }

        @Test
        @DisplayName("should return error result on timeout")
        @DisabledOnOs(OS.WINDOWS)
        void shouldReturnErrorResultOnTimeout() {
            var command = List.of("sleep", "10");
            var result = executor.execute(command, workingDirectory, Duration.ofMillis(100));

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.exitCode()).isEqualTo(-1);
            assertThat(result.stderr()).containsIgnoringCase("timeout");
        }

        @Test
        @DisplayName("should execute command with working directory")
        @DisabledOnOs(OS.WINDOWS)
        void shouldExecuteCommandWithWorkingDirectory() {
            var command = List.of("pwd");
            var result = executor.execute(command, workingDirectory, Duration.ofSeconds(10));

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.stdout().trim()).isEqualTo(workingDirectory.toString());
        }
    }

    @Nested
    @DisplayName("execute with stdin")
    class ExecuteWithStdin {

        @Test
        @DisplayName("should pass stdin input to process")
        @DisabledOnOs(OS.WINDOWS)
        void shouldPassStdinInputToProcess() {
            var command = List.of("cat");
            var stdinInput = "Hello from stdin";
            var result = executor.execute(command, workingDirectory, Duration.ofSeconds(10), stdinInput);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.stdout()).contains("Hello from stdin");
        }

        @Test
        @DisplayName("should handle null stdin gracefully")
        @DisabledOnOs(OS.WINDOWS)
        void shouldHandleNullStdinGracefully() {
            var command = List.of("echo", "test");
            var result = executor.execute(command, workingDirectory, Duration.ofSeconds(10), null);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.stdout()).contains("test");
        }

        @Test
        @DisplayName("should handle multiline stdin input")
        @DisabledOnOs(OS.WINDOWS)
        void shouldHandleMultilineStdinInput() {
            var command = List.of("cat");
            var stdinInput = "Line 1\nLine 2\nLine 3";
            var result = executor.execute(command, workingDirectory, Duration.ofSeconds(10), stdinInput);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.stdout()).contains("Line 1");
            assertThat(result.stdout()).contains("Line 2");
            assertThat(result.stdout()).contains("Line 3");
        }
    }

    @Nested
    @DisplayName("executeAsync")
    class ExecuteAsync {

        @Test
        @DisplayName("should execute command asynchronously")
        @DisabledOnOs(OS.WINDOWS)
        void shouldExecuteCommandAsynchronously() throws Exception {
            var command = List.of("echo", "Async test");
            var future = executor.executeAsync(command, workingDirectory, Duration.ofSeconds(10));

            var result = future.get(15, TimeUnit.SECONDS);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.stdout()).contains("Async test");
        }

        @Test
        @DisplayName("should execute async with stdin")
        @DisabledOnOs(OS.WINDOWS)
        void shouldExecuteAsyncWithStdin() throws Exception {
            var command = List.of("cat");
            var stdinInput = "Async stdin";
            var future = executor.executeAsync(command, workingDirectory, Duration.ofSeconds(10), stdinInput);

            var result = future.get(15, TimeUnit.SECONDS);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.stdout()).contains("Async stdin");
        }
    }

    @Nested
    @DisplayName("executeStreaming")
    class ExecuteStreaming {

        @Test
        @DisplayName("should stream output lines to consumer")
        @DisabledOnOs(OS.WINDOWS)
        void shouldStreamOutputLinesToConsumer() throws Exception {
            var command = List.of("echo", "-e", "Line1\\nLine2\\nLine3");
            var lines = Collections.synchronizedList(new ArrayList<String>());

            var future = executor.executeStreaming(command, workingDirectory, lines::add, Duration.ofSeconds(10));
            var exitCode = future.get(15, TimeUnit.SECONDS);

            assertThat(exitCode).isEqualTo(0);
            assertThat(lines).isNotEmpty();
        }

        @Test
        @DisplayName("should return -1 on streaming timeout")
        @DisabledOnOs(OS.WINDOWS)
        void shouldReturnMinusOneOnStreamingTimeout() throws Exception {
            var command = List.of("sleep", "10");
            var lines = Collections.synchronizedList(new ArrayList<String>());

            var future = executor.executeStreaming(command, workingDirectory, lines::add, Duration.ofMillis(100));
            var exitCode = future.get(15, TimeUnit.SECONDS);

            assertThat(exitCode).isEqualTo(-1);
        }

        @Test
        @DisplayName("should stream with stdin input")
        @DisabledOnOs(OS.WINDOWS)
        void shouldStreamWithStdinInput() throws Exception {
            var command = List.of("cat");
            var stdinInput = "Streamed input";
            var lines = Collections.synchronizedList(new ArrayList<String>());

            var future = executor.executeStreaming(
                    command, workingDirectory, lines::add, Duration.ofSeconds(10), stdinInput);
            var exitCode = future.get(15, TimeUnit.SECONDS);

            assertThat(exitCode).isEqualTo(0);
            assertThat(lines).contains("Streamed input");
        }
    }
}
