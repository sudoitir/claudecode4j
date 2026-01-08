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
package ir.sudoit.claudecode4j.rest.openai.sse;

import ir.sudoit.claudecode4j.api.model.response.StreamEvent;
import ir.sudoit.claudecode4j.rest.openai.dto.response.ChatCompletionChunk;
import java.io.IOException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Formats streaming events in OpenAI SSE format.
 *
 * <p>OpenAI streaming format:
 *
 * <pre>{@code
 * data: {"id":"...","object":"chat.completion.chunk",...}
 *
 * data: [DONE]
 * }</pre>
 */
public class OpenAiSseFormatter {

    private final String requestId;
    private final String model;

    /**
     * Creates a new formatter.
     *
     * @param requestId the request ID
     * @param model the model name
     */
    public OpenAiSseFormatter(String requestId, String model) {
        this.requestId = requestId;
        this.model = model;
    }

    /**
     * Sends a stream event in OpenAI format.
     *
     * @param emitter the SSE emitter
     * @param event the stream event
     */
    public void sendEvent(SseEmitter emitter, StreamEvent event) throws IOException {
        if (event.isComplete()) {
            // Send [DONE] marker
            emitter.send(SseEmitter.event().data("[DONE]"));
        } else {
            var chunk = ChatCompletionChunk.from(event, requestId, model);
            emitter.send(SseEmitter.event().data(chunk));
        }
    }

    /**
     * Sends an error event in OpenAI format.
     *
     * @param emitter the SSE emitter
     * @param error the error
     */
    public void sendError(SseEmitter emitter, Throwable error) throws IOException {
        var errorChunk = ChatCompletionChunk.error(requestId, model, error);
        emitter.send(SseEmitter.event().data(errorChunk));
    }
}
