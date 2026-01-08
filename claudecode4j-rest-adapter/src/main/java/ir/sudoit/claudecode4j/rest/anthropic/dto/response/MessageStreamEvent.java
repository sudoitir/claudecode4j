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
package ir.sudoit.claudecode4j.rest.anthropic.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import ir.sudoit.claudecode4j.api.model.response.StreamEvent;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Anthropic streaming event types.
 *
 * <p>Sent via SSE with {@code event: <type>} and {@code data: <json>} format.
 *
 * <p>Event types:
 *
 * <ul>
 *   <li>message_start - Initial message metadata
 *   <li>content_block_start - New content block begins
 *   <li>content_block_delta - Content increment (text delta)
 *   <li>content_block_stop - Content block ends
 *   <li>message_delta - Message-level updates (usage, stop_reason)
 *   <li>message_stop - Stream completion
 *   <li>error - Error occurred
 * </ul>
 */
public sealed interface MessageStreamEvent
        permits MessageStreamEvent.MessageStart,
                MessageStreamEvent.ContentBlockStart,
                MessageStreamEvent.ContentBlockDelta,
                MessageStreamEvent.ContentBlockStop,
                MessageStreamEvent.MessageDelta,
                MessageStreamEvent.MessageStop,
                MessageStreamEvent.Error {

    @JsonProperty("type")
    String type();

    record MessageStart(
            @JsonProperty("type") String type,

            @JsonProperty("message") MessageInfo message) implements MessageStreamEvent {

        public MessageStart(String id, String model) {
            this("message_start", new MessageInfo(id, model));
        }

        record MessageInfo(
                @JsonProperty("id") String id,

                @JsonProperty("type") String type,

                @JsonProperty("role") String role,

                @JsonProperty("content") List<?> content,

                @JsonProperty("model") String model,

                @Nullable @JsonProperty("stop_reason") String stopReason) {

            public MessageInfo(String id, String model) {
                this(id, "message", "assistant", List.of(), model, null);
            }
        }
    }

    record ContentBlockStart(
            @JsonProperty("type") String type,

            @JsonProperty("index") Integer index,

            @JsonProperty("content_block") ContentBlock contentBlock)
            implements MessageStreamEvent {

        public ContentBlockStart() {
            this("content_block_start", 0, new ContentBlock());
        }

        record ContentBlock(
                @JsonProperty("type") String type,

                @JsonProperty("text") String text) {

            public ContentBlock() {
                this("text", "");
            }
        }
    }

    record ContentBlockDelta(
            @JsonProperty("type") String type,

            @JsonProperty("index") Integer index,

            @JsonProperty("delta") Delta delta) implements MessageStreamEvent {

        public ContentBlockDelta(String text) {
            this("content_block_delta", 0, new Delta(text));
        }

        record Delta(
                @JsonProperty("type") String type,

                @JsonProperty("text") String text) {

            public Delta(String text) {
                this("text_delta", text);
            }
        }
    }

    record ContentBlockStop(
            @JsonProperty("type") String type,

            @JsonProperty("index") Integer index) implements MessageStreamEvent {

        public ContentBlockStop() {
            this("content_block_stop", 0);
        }
    }

    record MessageDelta(
            @JsonProperty("type") String type,

            @JsonProperty("delta") Delta delta,

            @Nullable @JsonProperty("usage") Usage usage)
            implements MessageStreamEvent {

        public MessageDelta(StreamEvent event) {
            this("message_delta", new Delta(), new Usage((int) event.sequenceNumber()));
        }

        record Delta(@JsonProperty("stop_reason") String stopReason) {

            public Delta() {
                this("end_turn");
            }
        }
    }

    record MessageStop(@JsonProperty("type") String type) implements MessageStreamEvent {

        public MessageStop() {
            this("message_stop");
        }
    }

    record Error(
            @JsonProperty("type") String type,

            @JsonProperty("error") ErrorInfo error) implements MessageStreamEvent {

        public Error(String message) {
            this("error", new ErrorInfo(message));
        }

        record ErrorInfo(
                @JsonProperty("type") String type,

                @JsonProperty("message") String message) {

            public ErrorInfo(String message) {
                this("internal_error", message);
            }
        }
    }
}
