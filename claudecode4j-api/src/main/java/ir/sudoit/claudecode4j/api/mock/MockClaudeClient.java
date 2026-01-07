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
package ir.sudoit.claudecode4j.api.mock;

import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.api.client.ClaudeSession;
import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.api.model.request.PromptOptions;
import ir.sudoit.claudecode4j.api.model.response.ClaudeResponse;
import ir.sudoit.claudecode4j.api.model.response.StreamEvent;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import org.jspecify.annotations.Nullable;

/**
 * A decorator that wraps a real ClaudeClient and can substitute mock responses.
 *
 * <p>When mock mode is enabled, all requests are routed to the MockResponseProvider. Otherwise, requests are forwarded
 * to the delegate client.
 */
public final class MockClaudeClient implements ClaudeClient {

    private final @Nullable ClaudeClient delegate;
    private final MockResponseProvider mockProvider;
    private final boolean mockEnabled;
    private final @Nullable Duration mockDelay;

    private MockClaudeClient(Builder builder) {
        this.delegate = builder.delegate;
        this.mockProvider = builder.mockProvider;
        this.mockEnabled = builder.mockEnabled;
        this.mockDelay = builder.mockDelay;
    }

    public static Builder builder(MockResponseProvider provider) {
        return new Builder(provider);
    }

    @Override
    public ClaudeResponse execute(Prompt prompt) {
        return execute(prompt, PromptOptions.defaults());
    }

    @Override
    public ClaudeResponse execute(Prompt prompt, PromptOptions options) {
        if (mockEnabled) {
            mockProvider.recordRequest(prompt, options);
            applyDelay();
            return mockProvider.getMockResponse(prompt, options);
        }
        if (delegate == null) {
            throw new IllegalStateException("Mock mode disabled but no delegate client provided");
        }
        return delegate.execute(prompt, options);
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
        if (mockEnabled) {
            mockProvider.recordRequest(prompt, options);
            var publisher = new SubmissionPublisher<StreamEvent>();

            Thread.ofVirtual().start(() -> {
                try {
                    applyDelay();
                    mockProvider.getMockStream(prompt, options).forEach(publisher::submit);
                } finally {
                    publisher.close();
                }
            });

            return publisher;
        }
        if (delegate == null) {
            throw new IllegalStateException("Mock mode disabled but no delegate client provided");
        }
        return delegate.stream(prompt, options);
    }

    @Override
    public ClaudeSession createSession() {
        if (mockEnabled) {
            return new MockClaudeSession(this);
        }
        if (delegate == null) {
            throw new IllegalStateException("Mock mode disabled but no delegate client provided");
        }
        return delegate.createSession();
    }

    @Override
    public boolean isAvailable() {
        if (mockEnabled) {
            return true;
        }
        return delegate != null && delegate.isAvailable();
    }

    @Override
    public String getCliVersion() {
        if (mockEnabled) {
            return "mock-1.0.0";
        }
        return delegate != null ? delegate.getCliVersion() : "unknown";
    }

    @Override
    public void close() {
        if (delegate != null) {
            delegate.close();
        }
    }

    /** Returns the mock provider for verification in tests. */
    public MockResponseProvider getMockProvider() {
        return mockProvider;
    }

    /** Returns all recorded requests. */
    public List<MockResponseProvider.RecordedRequest> getRecordedRequests() {
        return mockProvider.getRecordedRequests();
    }

    /** Resets the mock provider state. */
    public void reset() {
        mockProvider.reset();
    }

    private void applyDelay() {
        if (mockDelay != null && !mockDelay.isZero()) {
            try {
                Thread.sleep(mockDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static final class Builder {
        private final MockResponseProvider mockProvider;
        private @Nullable ClaudeClient delegate;
        private boolean mockEnabled = true;
        private @Nullable Duration mockDelay;

        private Builder(MockResponseProvider mockProvider) {
            this.mockProvider = mockProvider;
        }

        /** Sets the delegate client to use when mock mode is disabled. */
        public Builder delegate(ClaudeClient delegate) {
            this.delegate = delegate;
            return this;
        }

        /** Enables or disables mock mode. */
        public Builder mockEnabled(boolean enabled) {
            this.mockEnabled = enabled;
            return this;
        }

        /** Sets an artificial delay for mock responses. */
        public Builder mockDelay(Duration delay) {
            this.mockDelay = delay;
            return this;
        }

        public MockClaudeClient build() {
            return new MockClaudeClient(this);
        }
    }
}
