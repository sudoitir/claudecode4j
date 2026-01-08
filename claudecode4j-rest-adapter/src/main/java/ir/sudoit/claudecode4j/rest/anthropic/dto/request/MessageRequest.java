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
package ir.sudoit.claudecode4j.rest.anthropic.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import ir.sudoit.claudecode4j.rest.anthropic.mapper.AnthropicRequestMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Anthropic Messages API request format.
 *
 * <p>Compatible with Anthropic's {@code POST /v1/messages} endpoint.
 *
 * @see <a href="https://docs.anthropic.com/en/api/messages">Anthropic API Reference</a>
 */
public record MessageRequest(
        @NotNull @JsonProperty("model") String model,

        @NotEmpty @JsonProperty("messages") List<@Valid Message> messages,

        @Nullable @JsonProperty("max_tokens") Integer maxTokens,

        @Nullable @JsonProperty("system") String system,

        @Nullable @JsonProperty("temperature") Double temperature,

        @Nullable @JsonProperty("top_p") Double topP,

        @Nullable @JsonProperty("top_k") Integer topK,

        @Nullable @JsonProperty("stream") Boolean stream,

        @Nullable @JsonProperty("stop_sequences") List<String> stopSequences,

        @Nullable @JsonProperty("tools") List<@Valid Tool> tools,

        @Nullable @JsonProperty("tool_choice") Object toolChoice,

        @Nullable @JsonProperty("metadata") Map<String, String> metadata) {

    /** Tool definition for function calling. */
    public record Tool(
            @JsonProperty("name") String name,

            @JsonProperty("description") String description,

            @JsonProperty("input_schema") Map<String, Object> inputSchema) {}

    /**
     * Converts this Anthropic request to a ClaudeCode4J Prompt.
     *
     * <p>Uses system parameter and concatenates messages.
     *
     * @return the mapped Prompt
     */
    public ir.sudoit.claudecode4j.api.model.request.Prompt toPrompt() {
        return AnthropicRequestMapper.toPrompt(this);
    }

    /**
     * Converts this Anthropic request to ClaudeCode4J PromptOptions.
     *
     * <p>Maps model, max_tokens, temperature, top_p, top_k.
     *
     * @return the mapped PromptOptions
     */
    public ir.sudoit.claudecode4j.api.model.request.PromptOptions toOptions() {
        return AnthropicRequestMapper.toOptions(this);
    }
}
