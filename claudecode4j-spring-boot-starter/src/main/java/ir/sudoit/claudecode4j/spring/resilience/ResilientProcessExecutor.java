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
package ir.sudoit.claudecode4j.spring.resilience;

import ir.sudoit.claudecode4j.api.spi.ProcessExecutor;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.springframework.retry.support.RetryTemplate;

/**
 * A ProcessExecutor wrapper that adds retry logic with exponential backoff.
 *
 * <p>This decorator wraps a ProcessExecutor and uses Spring Retry to automatically retry failed executions. It's
 * particularly useful for handling transient failures like network issues or temporary API unavailability.
 */
public final class ResilientProcessExecutor implements ProcessExecutor {

    private final ProcessExecutor delegate;
    private final RetryTemplate retryTemplate;

    /**
     * Creates a new resilient process executor.
     *
     * @param delegate the underlying process executor
     * @param retryTemplate the retry template to use
     */
    public ResilientProcessExecutor(ProcessExecutor delegate, RetryTemplate retryTemplate) {
        this.delegate = delegate;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public ExecutionResult execute(List<String> command, Path workingDirectory, Duration timeout) {
        return retryTemplate.execute(context -> delegate.execute(command, workingDirectory, timeout));
    }

    @Override
    public ExecutionResult execute(
            List<String> command, Path workingDirectory, Duration timeout, @Nullable String stdinInput) {
        return retryTemplate.execute(context -> delegate.execute(command, workingDirectory, timeout, stdinInput));
    }

    @Override
    public CompletableFuture<ExecutionResult> executeAsync(
            List<String> command, Path workingDirectory, Duration timeout) {
        // For async, we wrap the future result with retry logic
        return CompletableFuture.supplyAsync(
                () -> retryTemplate.execute(context -> delegate.execute(command, workingDirectory, timeout)));
    }

    @Override
    public CompletableFuture<ExecutionResult> executeAsync(
            List<String> command, Path workingDirectory, Duration timeout, @Nullable String stdinInput) {
        return CompletableFuture.supplyAsync(() ->
                retryTemplate.execute(context -> delegate.execute(command, workingDirectory, timeout, stdinInput)));
    }

    @Override
    public CompletableFuture<Integer> executeStreaming(
            List<String> command, Path workingDirectory, Consumer<String> lineConsumer, Duration timeout) {
        // Streaming is trickier with retry since output has already been consumed
        // We attempt a single execution and rely on the caller to handle retries if needed
        return delegate.executeStreaming(command, workingDirectory, lineConsumer, timeout);
    }

    @Override
    public CompletableFuture<Integer> executeStreaming(
            List<String> command,
            Path workingDirectory,
            Consumer<String> lineConsumer,
            Duration timeout,
            @Nullable String stdinInput) {
        // Streaming with stdin - same concern as above
        return delegate.executeStreaming(command, workingDirectory, lineConsumer, timeout, stdinInput);
    }

    /**
     * Returns the underlying process executor.
     *
     * @return the delegate executor
     */
    public ProcessExecutor getDelegate() {
        return delegate;
    }

    /**
     * Returns the retry template being used.
     *
     * @return the retry template
     */
    public RetryTemplate getRetryTemplate() {
        return retryTemplate;
    }
}
