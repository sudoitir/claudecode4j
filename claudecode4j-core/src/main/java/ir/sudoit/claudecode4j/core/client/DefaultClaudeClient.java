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

import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.api.client.ClaudeSession;
import ir.sudoit.claudecode4j.api.config.ClaudeConfig;
import ir.sudoit.claudecode4j.api.exception.ClaudeExecutionException;
import ir.sudoit.claudecode4j.api.exception.ClaudeTimeoutException;
import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.api.model.request.PromptOptions;
import ir.sudoit.claudecode4j.api.model.response.ClaudeResponse;
import ir.sudoit.claudecode4j.api.model.response.StreamEvent;
import ir.sudoit.claudecode4j.api.spi.InputSanitizer;
import ir.sudoit.claudecode4j.api.spi.OutputParser;
import ir.sudoit.claudecode4j.api.spi.ProcessExecutor;
import ir.sudoit.claudecode4j.core.process.ClaudeCommandBuilder;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DefaultClaudeClient implements ClaudeClient {

    private final ClaudeConfig config;
    private final Path binaryPath;
    private final InputSanitizer sanitizer;
    private final OutputParser parser;
    private final ProcessExecutor executor;
    private final Semaphore concurrencyLimiter;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public DefaultClaudeClient(
            ClaudeConfig config,
            Path binaryPath,
            InputSanitizer sanitizer,
            OutputParser parser,
            ProcessExecutor executor) {
        this.config = config;
        this.binaryPath = binaryPath;
        this.sanitizer = sanitizer;
        this.parser = parser;
        this.executor = executor;
        this.concurrencyLimiter = new Semaphore(config.concurrencyLimit(), true);
    }

    @Override
    public ClaudeResponse execute(Prompt prompt) {
        return execute(prompt, PromptOptions.defaults());
    }

    @Override
    public ClaudeResponse execute(Prompt prompt, PromptOptions options) {
        ensureOpen();
        var sanitized = sanitizer.sanitize(prompt);
        var effectiveOptions = applyConfigDefaults(options);
        var commandWithStdin = new ClaudeCommandBuilder(binaryPath)
                .prompt(sanitized)
                .options(effectiveOptions)
                .buildWithStdin();

        try {
            concurrencyLimiter.acquire();
            try {
                var timeout = effectiveOptions.timeout() != null ? effectiveOptions.timeout() : config.defaultTimeout();
                var result = executor.execute(
                        commandWithStdin.command(), resolveWorkingDir(prompt), timeout, commandWithStdin.stdinInput());

                if (result.exitCode() == -1 && result.stderr().contains("Timeout")) {
                    throw new ClaudeTimeoutException(timeout);
                }

                return parser.parse(result.stdout(), result.exitCode());
            } finally {
                concurrencyLimiter.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ClaudeExecutionException(-1, "Interrupted", e);
        }
    }

    @Override
    public CompletableFuture<ClaudeResponse> executeAsync(Prompt prompt) {
        return executeAsync(prompt, PromptOptions.defaults());
    }

    @Override
    public CompletableFuture<ClaudeResponse> executeAsync(Prompt prompt, PromptOptions options) {
        return CompletableFuture.supplyAsync(
                () -> execute(prompt, options), runnable -> Thread.ofVirtual().start(runnable));
    }

    @Override
    public Flow.Publisher<StreamEvent> stream(Prompt prompt) {
        return stream(prompt, PromptOptions.defaults());
    }

    @Override
    public Flow.Publisher<StreamEvent> stream(Prompt prompt, PromptOptions options) {
        ensureOpen();
        var sanitized = sanitizer.sanitize(prompt);
        var effectiveOptions = applyConfigDefaults(options);
        var commandWithStdin = new ClaudeCommandBuilder(binaryPath)
                .prompt(sanitized)
                .options(effectiveOptions)
                .buildWithStdin();

        var publisher = new SubmissionPublisher<StreamEvent>();

        Thread.ofVirtual().start(() -> {
            try {
                concurrencyLimiter.acquire();
                try {
                    var timeout =
                            effectiveOptions.timeout() != null ? effectiveOptions.timeout() : config.defaultTimeout();

                    executor.executeStreaming(
                                    commandWithStdin.command(),
                                    resolveWorkingDir(prompt),
                                    line -> parser.parseStream(java.util.stream.Stream.of(line))
                                            .forEach(publisher::submit),
                                    timeout,
                                    commandWithStdin.stdinInput())
                            .join();
                } finally {
                    concurrencyLimiter.release();
                    publisher.close();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                publisher.closeExceptionally(new ClaudeExecutionException(-1, "Interrupted", e));
            } catch (Exception e) {
                publisher.closeExceptionally(e);
            }
        });

        return publisher;
    }

    @Override
    public ClaudeSession createSession() {
        ensureOpen();
        return new DefaultClaudeSession(this);
    }

    @Override
    public boolean isAvailable() {
        try {
            var result = executor.execute(
                    java.util.List.of(binaryPath.toString(), "--version"),
                    Path.of(System.getProperty("user.dir")),
                    Duration.ofSeconds(10));
            return result.isSuccess();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getCliVersion() {
        try {
            var result = executor.execute(
                    java.util.List.of(binaryPath.toString(), "--version"),
                    Path.of(System.getProperty("user.dir")),
                    Duration.ofSeconds(10));
            return result.isSuccess() ? result.stdout().trim() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    @Override
    public void close() {
        closed.set(true);
    }

    private void ensureOpen() {
        if (closed.get()) {
            throw new IllegalStateException("Client is closed");
        }
    }

    private Path resolveWorkingDir(Prompt prompt) {
        return prompt.workingDirectory() != null ? prompt.workingDirectory() : Path.of(System.getProperty("user.dir"));
    }

    private PromptOptions applyConfigDefaults(PromptOptions options) {
        if (config.dangerouslySkipPermissions() && !options.dangerouslySkipPermissions()) {
            return PromptOptions.builder()
                    .timeout(options.timeout())
                    .outputFormat(options.outputFormat())
                    .model(options.model())
                    .dangerouslySkipPermissions(true)
                    .printMode(options.printMode())
                    .maxTurns(options.maxTurns())
                    .allowedTools(options.allowedTools())
                    .disallowedTools(options.disallowedTools())
                    .build();
        }
        return options;
    }
}
