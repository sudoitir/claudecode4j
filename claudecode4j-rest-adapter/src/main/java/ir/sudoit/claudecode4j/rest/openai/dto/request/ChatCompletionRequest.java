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
import ir.sudoit.claudecode4j.rest.openai.mapper.OpenAiRequestMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * OpenAI Chat Completions API request format.
 *
 * <p>Compatible with OpenAI's {@code POST /v1/chat/completions} endpoint.
 *
 * @see <a href="https://platform.openai.com/docs/api-reference/chat/create">OpenAI API Reference</a>
 */
public record ChatCompletionRequest(
        @NotEmpty @JsonProperty("messages") List<@Valid ChatMessage> messages,

        @NotNull @JsonProperty("model") String model,

        @Nullable @JsonProperty("frequency_penalty") Double frequencyPenalty,

        @Nullable @JsonProperty("max_tokens") Integer maxTokens,

        @Nullable @JsonProperty("presence_penalty") Double presencePenalty,

        @Nullable @JsonProperty("response_format") Map<String, Object> responseFormat,

        @Nullable @JsonProperty("seed") Integer seed,

        @Nullable @JsonProperty("stop") List<String> stop,

        @Nullable @JsonProperty("stream") Boolean stream,

        @Nullable @JsonProperty("temperature") Double temperature,

        @Nullable @JsonProperty("top_p") Double topP,

        @Nullable @JsonProperty("user") String user) {

    /**
     * Converts this OpenAI request to a ClaudeCode4J Prompt.
     *
     * <p>Extracts system messages and builds conversation text from the messages array.
     *
     * @return the mapped Prompt
     */
    public ir.sudoit.claudecode4j.api.model.request.Prompt toPrompt() {
        return OpenAiRequestMapper.toPrompt(this);
    }

    /**
     * Converts this OpenAI request to ClaudeCode4J PromptOptions.
     *
     * <p>Maps model, max_tokens, and other parameters to PromptOptions.
     *
     * @return the mapped PromptOptions
     */
    public ir.sudoit.claudecode4j.api.model.request.PromptOptions toOptions() {
        return OpenAiRequestMapper.toOptions(this);
    }
}
