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
package ir.sudoit.claudecode4j.api.spi;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

public interface ProcessExecutor {

    ExecutionResult execute(List<String> command, Path workingDirectory, Duration timeout);

    /**
     * Executes a command with optional stdin input.
     *
     * @param command the command to execute
     * @param workingDirectory the working directory for the process
     * @param timeout maximum time to wait for the process
     * @param stdinInput optional input to write to the process stdin, or null
     * @return the execution result
     */
    default ExecutionResult execute(
            List<String> command, Path workingDirectory, Duration timeout, @Nullable String stdinInput) {
        // Default implementation ignores stdinInput for backward compatibility
        return execute(command, workingDirectory, timeout);
    }

    CompletableFuture<ExecutionResult> executeAsync(List<String> command, Path workingDirectory, Duration timeout);

    /**
     * Executes a command asynchronously with optional stdin input.
     *
     * @param command the command to execute
     * @param workingDirectory the working directory for the process
     * @param timeout maximum time to wait for the process
     * @param stdinInput optional input to write to the process stdin, or null
     * @return a future containing the execution result
     */
    default CompletableFuture<ExecutionResult> executeAsync(
            List<String> command, Path workingDirectory, Duration timeout, @Nullable String stdinInput) {
        // Default implementation ignores stdinInput for backward compatibility
        return executeAsync(command, workingDirectory, timeout);
    }

    CompletableFuture<Integer> executeStreaming(
            List<String> command, Path workingDirectory, Consumer<String> lineConsumer, Duration timeout);

    /**
     * Executes a command with streaming output and optional stdin input.
     *
     * @param command the command to execute
     * @param workingDirectory the working directory for the process
     * @param lineConsumer consumer for each output line
     * @param timeout maximum time to wait for the process
     * @param stdinInput optional input to write to the process stdin, or null
     * @return a future containing the exit code
     */
    default CompletableFuture<Integer> executeStreaming(
            List<String> command,
            Path workingDirectory,
            Consumer<String> lineConsumer,
            Duration timeout,
            @Nullable String stdinInput) {
        // Default implementation ignores stdinInput for backward compatibility
        return executeStreaming(command, workingDirectory, lineConsumer, timeout);
    }

    record ExecutionResult(int exitCode, String stdout, String stderr) {
        public boolean isSuccess() {
            return exitCode == 0;
        }
    }
}
