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
package ir.sudoit.claudecode4j.rest.openai.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import ir.sudoit.claudecode4j.api.model.response.ClaudeResponse;
import ir.sudoit.claudecode4j.api.model.response.TextResponse;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * OpenAI Chat Completions API response format.
 *
 * <p>Compatible with OpenAI's non-streaming response structure.
 */
public record ChatCompletionResponse(
        @JsonProperty("id") String id,

        @JsonProperty("object") String object,

        @JsonProperty("created") Long created,

        @JsonProperty("model") String model,

        @JsonProperty("choices") List<Choice> choices,

        @Nullable @JsonProperty("usage") Usage usage,

        @Nullable @JsonProperty("system_fingerprint") String systemFingerprint) {

    public record Choice(
            @JsonProperty("index") Integer index,

            @JsonProperty("message") ChatMessage message,

            @Nullable @JsonProperty("finish_reason") String finishReason,

            @Nullable @JsonProperty("logprobs") Object logprobs) {}

    public record ChatMessage(
            @JsonProperty("role") String role,

            @JsonProperty("content") String content,

            @Nullable @JsonProperty("tool_calls") List<Object> toolCalls) {}

    /**
     * Factory method to convert from ClaudeResponse.
     *
     * @param response the Claude response
     * @param requestId request ID
     * @return OpenAI-formatted response
     */
    public static ChatCompletionResponse from(ClaudeResponse response, String requestId) {
        long created = System.currentTimeMillis() / 1000;

        return switch (response) {
            case TextResponse text -> {
                var choice = new Choice(0, new ChatMessage("assistant", text.content(), null), "stop", null);

                yield new ChatCompletionResponse(
                        requestId,
                        "chat.completion",
                        created,
                        text.model() != null ? text.model() : "claude-3-5-sonnet",
                        List.of(choice),
                        new Usage(text.tokensUsed()),
                        "fp_" + System.currentTimeMillis());
            }

            case ir.sudoit.claudecode4j.api.model.response.ErrorResponse error ->
                throw new IllegalArgumentException(
                        "Cannot convert error response to ChatCompletionResponse",
                        new RuntimeException(error.content()));

            case ir.sudoit.claudecode4j.api.model.response.StreamResponse stream ->
                throw new IllegalArgumentException("Stream responses should use ChatCompletionChunk");
        };
    }
}
