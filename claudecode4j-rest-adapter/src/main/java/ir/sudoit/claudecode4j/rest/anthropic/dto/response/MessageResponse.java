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
import ir.sudoit.claudecode4j.api.model.response.ClaudeResponse;
import ir.sudoit.claudecode4j.api.model.response.TextResponse;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Anthropic Messages API response format.
 *
 * <p>Compatible with Anthropic's non-streaming response structure.
 */
public record MessageResponse(
        @JsonProperty("id") String id,

        @JsonProperty("type") String type,

        @JsonProperty("role") String role,

        @JsonProperty("content") List<ContentBlock> content,

        @JsonProperty("model") String model,

        @JsonProperty("stop_reason") String stopReason,

        @Nullable @JsonProperty("usage") Usage usage,

        @Nullable @JsonProperty("stop_sequence") String stopSequence) {

    /** Sealed interface for content blocks. */
    public sealed interface ContentBlock permits TextContentBlock, ToolUseContentBlock {

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

    /** Tool use content block. */
    public record ToolUseContentBlock(
            @JsonProperty("type") String type,

            @JsonProperty("id") String id,

            @JsonProperty("name") String name,

            @JsonProperty("input") Map<String, Object> input)
            implements ContentBlock {}

    /**
     * Factory method to convert from ClaudeResponse.
     *
     * @param response the Claude response
     * @param requestId request ID
     * @return Anthropic-formatted response
     */
    public static MessageResponse from(ClaudeResponse response, String requestId) {
        return switch (response) {
            case TextResponse text ->
                new MessageResponse(
                        requestId,
                        "message",
                        "assistant",
                        List.of(new TextContentBlock(text.content())),
                        text.model() != null ? text.model() : "claude-3-5-sonnet-20241022",
                        "end_turn",
                        new Usage(text.tokensUsed()),
                        null);

            case ir.sudoit.claudecode4j.api.model.response.ErrorResponse error ->
                throw new IllegalArgumentException("Cannot convert error response to MessageResponse");

            case ir.sudoit.claudecode4j.api.model.response.StreamResponse stream ->
                throw new IllegalArgumentException("Stream responses should use streaming format");
        };
    }
}
