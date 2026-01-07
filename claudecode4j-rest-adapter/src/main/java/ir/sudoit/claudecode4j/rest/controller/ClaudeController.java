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
package ir.sudoit.claudecode4j.rest.controller;

import ir.sudoit.claudecode4j.api.annotation.ConcurrencyLimit;
import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.api.exception.ClaudeException;
import ir.sudoit.claudecode4j.rest.dto.PromptRequest;
import ir.sudoit.claudecode4j.rest.dto.PromptResponse;
import ir.sudoit.claudecode4j.rest.sse.SseHeartbeatEmitter;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("${claude.code.rest.base-path:/api/claude}")
public class ClaudeController {

    private final ClaudeClient claudeClient;
    private final ScheduledExecutorService heartbeatScheduler;

    public ClaudeController(ClaudeClient claudeClient) {
        this.claudeClient = claudeClient;
        // Use virtual thread-based scheduler for heartbeats
        this.heartbeatScheduler =
                Executors.newScheduledThreadPool(1, Thread.ofVirtual().factory());
    }

    @PostMapping("/prompt")
    @ConcurrencyLimit
    public ResponseEntity<PromptResponse> executePrompt(@Valid @RequestBody PromptRequest request) {
        var response = claudeClient.execute(request.toPrompt(), request.toOptions());
        return ResponseEntity.ok(PromptResponse.from(response));
    }

    @PostMapping("/prompt/async")
    public CompletableFuture<ResponseEntity<PromptResponse>> executePromptAsync(
            @Valid @RequestBody PromptRequest request) {
        return claudeClient
                .executeAsync(request.toPrompt(), request.toOptions())
                .thenApply(response -> ResponseEntity.ok(PromptResponse.from(response)));
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ConcurrencyLimit
    public SseEmitter streamPrompt(@Valid @RequestBody PromptRequest request) {
        var emitter = new SseHeartbeatEmitter(Duration.ofMinutes(10).toMillis(), heartbeatScheduler);

        Thread.ofVirtual().start(() -> {
            try {
                var publisher = claudeClient.stream(request.toPrompt(), request.toOptions());
                publisher.subscribe(new java.util.concurrent.Flow.Subscriber<>() {
                    private java.util.concurrent.Flow.Subscription subscription;

                    @Override
                    public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
                        this.subscription = subscription;
                        subscription.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(ir.sudoit.claudecode4j.api.model.response.StreamEvent event) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name(event.type().name().toLowerCase())
                                    .data(Map.of(
                                            "content", event.content(),
                                            "timestamp", event.timestamp().toString(),
                                            "sequence", event.sequenceNumber())));
                        } catch (IOException e) {
                            subscription.cancel();
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        emitter.completeWithError(throwable);
                    }

                    @Override
                    public void onComplete() {
                        emitter.complete();
                    }
                });
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        var available = claudeClient.isAvailable();
        var version = available ? claudeClient.getCliVersion() : "unknown";
        return ResponseEntity.ok(Map.of(
                "available", available,
                "version", version,
                "timestamp", Instant.now().toString()));
    }

    @ExceptionHandler(ClaudeException.class)
    public ResponseEntity<Map<String, Object>> handleClaudeException(ClaudeException e) {
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "error", e.errorCode(),
                        "message", e.getMessage(),
                        "timestamp", Instant.now().toString()));
    }
}
