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
package ir.sudoit.claudecode4j.rest.common.streaming;

import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.api.model.request.PromptOptions;
import ir.sudoit.claudecode4j.api.model.response.StreamEvent;
import ir.sudoit.claudecode4j.rest.sse.SseHeartbeatEmitter;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Base class for SSE streaming implementations.
 *
 * <p>Reduces code duplication between OpenAI and Anthropic compatible controllers by providing a common streaming
 * infrastructure that handles:
 *
 * <ul>
 *   <li>Creating {@link SseHeartbeatEmitter} instances with heartbeat support
 *   <li>Subscribing to {@code Flow.Publisher<StreamEvent>} from ClaudeClient
 *   <li>Delegating event formatting to subclasses via {@link EventFormatter}
 *   <li>Error handling and completion
 * </ul>
 *
 * <p>Subclasses should implement {@link EventFormatter} to format events according to their specific API specification
 * (OpenAI or Anthropic).
 */
public abstract class AbstractSseStreamer {

    protected final ClaudeClient claudeClient;
    protected final ScheduledExecutorService heartbeatScheduler;

    /**
     * Creates a new streamer.
     *
     * @param claudeClient the Claude client to stream from
     */
    protected AbstractSseStreamer(ClaudeClient claudeClient) {
        this.claudeClient = claudeClient;
        this.heartbeatScheduler =
                Executors.newScheduledThreadPool(1, Thread.ofVirtual().factory());
    }

    /**
     * Creates an SSE emitter for streaming responses.
     *
     * @param prompt the prompt to execute
     * @param options execution options
     * @param eventFormatter function to format events for the specific API
     * @return configured SseEmitter
     */
    protected SseEmitter createStreamer(Prompt prompt, PromptOptions options, EventFormatter eventFormatter) {
        var emitter = new SseHeartbeatEmitter(Duration.ofMinutes(10).toMillis(), heartbeatScheduler);

        Thread.ofVirtual().start(() -> {
            try {
                var publisher = claudeClient.stream(prompt, options);

                publisher.subscribe(new java.util.concurrent.Flow.Subscriber<>() {
                    private java.util.concurrent.Flow.Subscription subscription;

                    @Override
                    public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
                        this.subscription = subscription;
                        subscription.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(StreamEvent event) {
                        try {
                            eventFormatter.format(emitter, event);
                        } catch (Exception e) {
                            subscription.cancel();
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        try {
                            eventFormatter.formatError(emitter, throwable);
                        } catch (Exception ignored) {
                        }
                        emitter.completeWithError(throwable);
                    }

                    @Override
                    public void onComplete() {
                        emitter.complete();
                    }
                });
            } catch (Exception e) {
                try {
                    eventFormatter.formatError(emitter, e);
                } catch (Exception ignored) {
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * Functional interface for formatting SSE events.
     *
     * <p>Implementations format {@link StreamEvent} instances according to the specific API specification (OpenAI or
     * Anthropic).
     */
    @FunctionalInterface
    public interface EventFormatter {
        /**
         * Formats a stream event and sends it to the emitter.
         *
         * @param emitter the SSE emitter
         * @param event the stream event to format
         * @throws Exception if formatting or sending fails
         */
        void format(SseEmitter emitter, StreamEvent event) throws Exception;

        /**
         * Formats an error event and sends it to the emitter.
         *
         * @param emitter the SSE emitter
         * @param error the error to format
         * @throws Exception if formatting or sending fails
         */
        default void formatError(SseEmitter emitter, Throwable error) throws Exception {
            // Default error handling - subclasses may override
        }
    }
}
