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
package ir.sudoit.claudecode4j.rest.openai.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Represents a message in the OpenAI chat completion format.
 *
 * <p>Supports role-based messages (system, user, assistant, tool) with flexible content types:
 *
 * <ul>
 *   <li>String content for simple text messages
 *   <li>List of content blocks for complex messages (text, image_url, etc.)
 * </ul>
 *
 * @see <a href="https://platform.openai.com/docs/api-reference/chat/create">OpenAI Chat Completions API</a>
 */
public record ChatMessage(
        @JsonProperty("role") String role,
        @JsonProperty("content") Object content,
        @Nullable @JsonProperty("name") String name,
        @Nullable @JsonProperty("tool_call_id") String toolCallId,
        @Nullable @JsonProperty("tool_calls") List<ToolCall> toolCalls) {

    /**
     * Sealed interface for content blocks in structured content.
     *
     * <p>OpenAI supports complex content types including text, images, and other media.
     */
    public sealed interface ContentBlock permits TextContentBlock, ImageUrlContentBlock {

        @JsonProperty("type")
        String type();
    }

    /** Text content block. */
    public record TextContentBlock(
            @JsonProperty("type") String type,
            @JsonProperty("text") String text) implements ContentBlock {

        public TextContentBlock(String text) {
            this("text", text);
        }
    }

    /** Image URL content block. */
    public record ImageUrlContentBlock(
            @JsonProperty("type") String type,
            @JsonProperty("image_url") ImageUrl imageUrl) implements ContentBlock {

        public ImageUrlContentBlock(ImageUrl imageUrl) {
            this("image_url", imageUrl);
        }
    }

    /** Image URL reference. */
    public record ImageUrl(
            @JsonProperty("url") String url,
            @Nullable @JsonProperty("detail") String detail) {}

    /** Tool call information. */
    public record ToolCall(
            @JsonProperty("id") String id,
            @JsonProperty("type") String type,
            @JsonProperty("function") FunctionCall function) {}

    /** Function call details. */
    public record FunctionCall(
            @JsonProperty("name") String name,
            @JsonProperty("arguments") String arguments) {}
}
