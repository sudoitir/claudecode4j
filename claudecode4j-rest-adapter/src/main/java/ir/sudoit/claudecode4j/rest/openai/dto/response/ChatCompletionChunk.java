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
import ir.sudoit.claudecode4j.api.model.response.StreamEvent;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * OpenAI streaming chunk format.
 *
 * <p>Sent via SSE with {@code data: <json>} format for each chunk.
 */
public record ChatCompletionChunk(
        @JsonProperty("id") String id,

        @JsonProperty("object") String object,

        @JsonProperty("created") Long created,

        @JsonProperty("model") String model,

        @JsonProperty("choices") List<ChunkChoice> choices) {

    public record ChunkChoice(
            @JsonProperty("index") Integer index,

            @JsonProperty("delta") Delta delta,

            @Nullable @JsonProperty("finish_reason") String finishReason,

            @Nullable @JsonProperty("logprobs") Object logprobs) {}

    public record Delta(
            @Nullable @JsonProperty("role") String role,

            @Nullable @JsonProperty("content") String content) {}

    /**
     * Creates a chunk from a stream event.
     *
     * @param event the stream event
     * @param id request ID
     * @param model model name
     * @return formatted chunk
     */
    public static ChatCompletionChunk from(StreamEvent event, String id, String model) {
        return new ChatCompletionChunk(
                id,
                "chat.completion.chunk",
                System.currentTimeMillis() / 1000,
                model,
                List.of(new ChunkChoice(0, new Delta("assistant", event.content()), null, null)));
    }

    /**
     * Creates an error chunk.
     *
     * @param id request ID
     * @param model model name
     * @param error the error
     * @return error chunk
     */
    public static ChatCompletionChunk error(String id, String model, Throwable error) {
        return new ChatCompletionChunk(
                id,
                "chat.completion.chunk",
                System.currentTimeMillis() / 1000,
                model,
                List.of(new ChunkChoice(0, new Delta(null, "Error: " + error.getMessage()), "error", null)));
    }
}
