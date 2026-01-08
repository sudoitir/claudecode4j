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
package ir.sudoit.claudecode4j.rest.openai.controller;

import ir.sudoit.claudecode4j.api.annotation.ConcurrencyLimit;
import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.api.exception.ClaudeException;
import ir.sudoit.claudecode4j.api.model.response.ClaudeResponse;
import ir.sudoit.claudecode4j.rest.common.dto.ErrorDetails;
import ir.sudoit.claudecode4j.rest.openai.dto.request.ChatCompletionRequest;
import ir.sudoit.claudecode4j.rest.openai.dto.response.ChatCompletionResponse;
import ir.sudoit.claudecode4j.rest.openai.sse.OpenAiSseFormatter;
import ir.sudoit.claudecode4j.rest.sse.SseHeartbeatEmitter;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * OpenAI-compatible Chat Completions API controller.
 *
 * <p>Provides {@code POST /v1/chat/completions} endpoint compatible with OpenAI's API format.
 *
 * @see <a href="https://platform.openai.com/docs/api-reference/chat">OpenAI API Reference</a>
 */
@RestController
@RequestMapping("${claude.code.rest.openai.base-path:/v1}")
public class OpenAiChatController {

    private final ClaudeClient claudeClient;
    private final ScheduledExecutorService heartbeatScheduler;

    public OpenAiChatController(ClaudeClient claudeClient) {
        this.claudeClient = claudeClient;
        this.heartbeatScheduler =
                Executors.newScheduledThreadPool(1, Thread.ofVirtual().factory());
    }

    /**
     * OpenAI Chat Completions endpoint.
     *
     * <p>Supports both non-streaming and streaming modes based on {@code stream} parameter.
     *
     * @param request the chat completion request
     * @return response or SSE emitter for streaming
     */
    @PostMapping("/chat/completions")
    @ConcurrencyLimit
    public ResponseEntity<?> chatCompletions(@Valid @RequestBody ChatCompletionRequest request) {
        String requestId = "chatcmpl-" + UUID.randomUUID();

        // Non-streaming mode
        if (request.stream() == null || !request.stream()) {
            ClaudeResponse response = claudeClient.execute(request.toPrompt(), request.toOptions());
            ChatCompletionResponse openaiResponse = ChatCompletionResponse.from(response, requestId);
            return ResponseEntity.ok(openaiResponse);
        }

        // Streaming mode - return SseEmitter
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(createStreamingEmitter(request, requestId));
    }

    /** Creates an SSE emitter for streaming responses. */
    private SseEmitter createStreamingEmitter(ChatCompletionRequest request, String requestId) {
        var emitter = new SseHeartbeatEmitter(Duration.ofMinutes(10).toMillis(), heartbeatScheduler);

        var formatter = new OpenAiSseFormatter(requestId, request.model());

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
                            formatter.sendEvent(emitter, event);
                        } catch (Exception e) {
                            subscription.cancel();
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        try {
                            formatter.sendError(emitter, throwable);
                        } catch (Exception e) {
                            // Ignore send error
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
                    formatter.sendError(emitter, e);
                } catch (Exception ignored) {
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        var error = new ErrorDetails("invalid_request_error", message, "INVALID_REQUEST");
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDetails> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        var error = new ErrorDetails("invalid_request_error", "Invalid request body or parameters", "INVALID_REQUEST");
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ClaudeException.class)
    public ResponseEntity<ErrorDetails> handleClaudeException(ClaudeException e) {
        var error = new ErrorDetails("error", e.getMessage(), e.errorCode());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGenericException(Exception e) {
        var error = new ErrorDetails("internal_error", e.getMessage(), "INTERNAL_ERROR");
        return ResponseEntity.internalServerError().body(error);
    }
}
