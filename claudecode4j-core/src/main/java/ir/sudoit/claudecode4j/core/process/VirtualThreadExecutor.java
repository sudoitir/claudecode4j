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
import org.jspecify.annotations.Nullable;

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
                    ProcessTerminator.terminate(process);
                    return new ExecutionResult(-1, "", "Timeout: Process did not exit");
                }

                return new ExecutionResult(process.exitValue(), stdoutTask.get(), stderrTask.get());
            }
        } catch (StructuredTaskScope.TimeoutException e) {
            ProcessTerminator.terminate(process);
            return new ExecutionResult(-1, "", "Timeout");
        } catch (StructuredTaskScope.FailedException e) {
            ProcessTerminator.terminate(process);
            return new ExecutionResult(-1, "", "Error: " + e.getCause().getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ProcessTerminator.terminate(process);
            return new ExecutionResult(-1, "", "Interrupted: " + e.getMessage());
        } catch (Exception e) {
            ProcessTerminator.terminate(process);
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
                            ProcessTerminator.terminate(process);
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

    @Override
    public ExecutionResult execute(
            List<String> command, Path workingDirectory, Duration timeout, @Nullable String stdinInput) {
        if (stdinInput == null) {
            return execute(command, workingDirectory, timeout);
        }

        Process process = null;
        try {
            process = new ProcessBuilder(command)
                    .directory(workingDirectory.toFile())
                    .start();

            // Write stdin input in a virtual thread
            Process finalProcess = process;
            Thread.ofVirtual().start(() -> {
                try (var writer = finalProcess.outputWriter()) {
                    writer.write(stdinInput);
                    writer.flush();
                } catch (IOException ignored) {
                    // Process may have terminated
                }
            });

            try (var scope = StructuredTaskScope.open(
                    StructuredTaskScope.Joiner.<String>awaitAllSuccessfulOrThrow(),
                    config -> config.withTimeout(timeout))) {
                var stdoutTask = scope.fork(() -> readStream(finalProcess.inputReader()));
                var stderrTask = scope.fork(() -> readStream(finalProcess.errorReader()));
                scope.join();
                boolean completed = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
                if (!completed) {
                    ProcessTerminator.terminate(process);
                    return new ExecutionResult(-1, "", "Timeout: Process did not exit");
                }

                return new ExecutionResult(process.exitValue(), stdoutTask.get(), stderrTask.get());
            }
        } catch (StructuredTaskScope.TimeoutException e) {
            ProcessTerminator.terminate(process);
            return new ExecutionResult(-1, "", "Timeout");
        } catch (StructuredTaskScope.FailedException e) {
            ProcessTerminator.terminate(process);
            return new ExecutionResult(-1, "", "Error: " + e.getCause().getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ProcessTerminator.terminate(process);
            return new ExecutionResult(-1, "", "Interrupted: " + e.getMessage());
        } catch (Exception e) {
            ProcessTerminator.terminate(process);
            return new ExecutionResult(-1, "", "Error: " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<ExecutionResult> executeAsync(
            List<String> command, Path workingDirectory, Duration timeout, @Nullable String stdinInput) {
        return CompletableFuture.supplyAsync(
                () -> execute(command, workingDirectory, timeout, stdinInput),
                runnable -> Thread.ofVirtual().start(runnable));
    }

    @Override
    public CompletableFuture<Integer> executeStreaming(
            List<String> command,
            Path workingDirectory,
            Consumer<String> lineConsumer,
            Duration timeout,
            @Nullable String stdinInput) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        var process = new ProcessBuilder(command)
                                .directory(workingDirectory.toFile())
                                .redirectErrorStream(true)
                                .start();

                        // Write stdin input if provided
                        if (stdinInput != null) {
                            Thread.ofVirtual().start(() -> {
                                try (var writer = process.outputWriter()) {
                                    writer.write(stdinInput);
                                    writer.flush();
                                } catch (IOException ignored) {
                                    // Process may have terminated
                                }
                            });
                        }

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
                            ProcessTerminator.terminate(process);
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
