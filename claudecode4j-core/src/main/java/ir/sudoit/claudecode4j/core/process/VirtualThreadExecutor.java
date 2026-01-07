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

import ir.sudoit.claudecode4j.api.spi.ProcessExecutor;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class VirtualThreadExecutor implements ProcessExecutor {

    @Override
    public ExecutionResult execute(List<String> command, Path workingDirectory, Duration timeout) {
        Process process = null;
        try {
            process = new ProcessBuilder(command)
                    .directory(workingDirectory.toFile())
                    .start();
            try (var scope = StructuredTaskScope.open(
                    StructuredTaskScope.Joiner.<String>awaitAllSuccessfulOrThrow(),
                    config -> config.withTimeout(timeout))) {
                Process finalProcess = process;
                var stdoutTask = scope.fork(() -> readStream(finalProcess.inputReader()));
                Process finalProcess1 = process;
                var stderrTask = scope.fork(() -> readStream(finalProcess1.errorReader()));
                scope.join();
                boolean completed = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
                if (!completed) {
                    process.destroyForcibly();
                    return new ExecutionResult(-1, "", "Timeout: Process did not exit");
                }

                return new ExecutionResult(process.exitValue(), stdoutTask.get(), stderrTask.get());
            }
        } catch (StructuredTaskScope.TimeoutException e) {
            if (process != null) process.destroyForcibly();
            return new ExecutionResult(-1, "", "Timeout");
        } catch (StructuredTaskScope.FailedException e) {
            if (process != null) process.destroyForcibly();
            return new ExecutionResult(-1, "", "Error: " + e.getCause().getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (process != null) process.destroyForcibly();
            return new ExecutionResult(-1, "", "Interrupted: " + e.getMessage());
        } catch (Exception e) {
            if (process != null) process.destroyForcibly();
            return new ExecutionResult(-1, "", "Error: " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<ExecutionResult> executeAsync(
            List<String> command, Path workingDirectory, Duration timeout) {
        return CompletableFuture.supplyAsync(
                () -> execute(command, workingDirectory, timeout),
                runnable -> Thread.ofVirtual().start(runnable));
    }

    @Override
    public CompletableFuture<Integer> executeStreaming(
            List<String> command, Path workingDirectory, Consumer<String> lineConsumer, Duration timeout) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        var process = new ProcessBuilder(command)
                                .directory(workingDirectory.toFile())
                                .redirectErrorStream(true)
                                .start();

                        Thread.ofVirtual().start(() -> {
                            try (var reader = process.inputReader()) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    lineConsumer.accept(line);
                                }
                            } catch (IOException ignored) {
                            }
                        });

                        boolean completed = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
                        if (!completed) {
                            process.destroyForcibly();
                            return -1;
                        }

                        return process.exitValue();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return -1;
                    } catch (Exception e) {
                        return -1;
                    }
                },
                runnable -> Thread.ofVirtual().start(runnable));
    }

    private String readStream(BufferedReader reader) throws IOException {
        var sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append(System.lineSeparator());
        }
        return sb.toString();
    }
}
