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
package ir.sudoit.claudecode4j.rest.anthropic.sse;

import ir.sudoit.claudecode4j.api.model.response.StreamEvent;
import ir.sudoit.claudecode4j.rest.anthropic.dto.response.MessageStreamEvent;
import java.io.IOException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Formats streaming events in Anthropic SSE format.
 *
 * <p>Anthropic streaming format:
 *
 * <pre>{@code
 * event: message_start
 * data: {"type":"message_start","message":{...}}
 *
 * event: content_block_delta
 * data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":"..."}}
 *
 * event: message_delta
 * data: {"type":"message_delta","delta":{"stop_reason":"end_turn"},"usage":{"output_tokens":15}}
 *
 * event: message_stop
 * data: {"type":"message_stop"}
 * }</pre>
 */
public class AnthropicSseFormatter {

    private final String requestId;
    private final String model;
    private boolean messageStarted = false;
    private boolean contentBlockStarted = false;

    /**
     * Creates a new formatter.
     *
     * @param requestId the request ID
     * @param model the model name
     */
    public AnthropicSseFormatter(String requestId, String model) {
        this.requestId = requestId;
        this.model = model;
    }

    /**
     * Sends a stream event in Anthropic format.
     *
     * @param emitter the SSE emitter
     * @param event the stream event
     */
    public void sendEvent(SseEmitter emitter, StreamEvent event) throws IOException {
        if (!messageStarted) {
            // Send message_start event
            var startEvent = new MessageStreamEvent.MessageStart(requestId, model);
            sendSseEvent(emitter, "message_start", startEvent);
            messageStarted = true;

            // Send content_block_start event
            var blockStart = new MessageStreamEvent.ContentBlockStart();
            sendSseEvent(emitter, "content_block_start", blockStart);
            contentBlockStarted = true;
        }

        if (event.isComplete()) {
            // Send content_block_stop
            var blockStop = new MessageStreamEvent.ContentBlockStop();
            sendSseEvent(emitter, "content_block_stop", blockStop);

            // Send message_delta with usage
            var deltaEvent = new MessageStreamEvent.MessageDelta(event);
            sendSseEvent(emitter, "message_delta", deltaEvent);

            // Send message_stop
            var stopEvent = new MessageStreamEvent.MessageStop();
            sendSseEvent(emitter, "message_stop", stopEvent);
        } else {
            // Send content_block_delta with text content
            var deltaEvent = new MessageStreamEvent.ContentBlockDelta(event.content());
            sendSseEvent(emitter, "content_block_delta", deltaEvent);
        }
    }

    /**
     * Sends an error event in Anthropic format.
     *
     * @param emitter the SSE emitter
     * @param error the error
     */
    public void sendError(SseEmitter emitter, Throwable error) throws IOException {
        var errorEvent = new MessageStreamEvent.Error(error.getMessage());
        sendSseEvent(emitter, "error", errorEvent);
    }

    /** Sends an SSE event with type and data. */
    private void sendSseEvent(SseEmitter emitter, String eventType, Object data) throws IOException {
        emitter.send(SseEmitter.event().name(eventType).data(data));
    }
}
