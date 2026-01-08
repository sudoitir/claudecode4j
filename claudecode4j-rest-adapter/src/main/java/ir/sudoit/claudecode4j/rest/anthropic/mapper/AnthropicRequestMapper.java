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
package ir.sudoit.claudecode4j.rest.anthropic.mapper;

import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.api.model.request.PromptOptions;
import ir.sudoit.claudecode4j.rest.anthropic.dto.request.Message;
import ir.sudoit.claudecode4j.rest.anthropic.dto.request.MessageRequest;
import java.time.Duration;
import java.util.stream.Collectors;

/** Maps between Anthropic API format and ClaudeCode4J domain models. */
public final class AnthropicRequestMapper {

    private AnthropicRequestMapper() {}

    /**
     * Converts Anthropic MessageRequest to ClaudeCode4J Prompt.
     *
     * <p>Strategy:
     *
     * <ul>
     *   <li>Use top-level system parameter as systemPrompt
     *   <li>Concatenate user/assistant messages as text prompt
     *   <li>Extract tool_use blocks for context
     * </ul>
     *
     * @param request the Anthropic request
     * @return the mapped Prompt
     */
    public static Prompt toPrompt(MessageRequest request) {
        StringBuilder textBuilder = new StringBuilder();

        for (Message message : request.messages()) {
            if (!textBuilder.isEmpty()) {
                textBuilder.append("\n\n");
            }
            textBuilder.append(message.role()).append(": ").append(contentToString(message.content()));
        }

        var builder = Prompt.builder().text(textBuilder.toString());

        // Use system parameter if provided
        if (request.system() != null && !request.system().isBlank()) {
            builder.systemPrompt(request.system());
        }

        return builder.build();
    }

    /**
     * Converts Anthropic MessageRequest to ClaudeCode4J PromptOptions.
     *
     * <p>Maps model, max_tokens, temperature, top_p, top_k. Temperature and sampling parameters are passed via model
     * parameter syntax.
     *
     * @param request the Anthropic request
     * @return the mapped PromptOptions
     */
    public static PromptOptions toOptions(MessageRequest request) {
        var builder = PromptOptions.builder();

        // Map model parameter with sampling parameters
        if (request.model() != null) {
            StringBuilder modelParam = new StringBuilder(request.model());

            // Append sampling parameters using @ syntax
            boolean hasParams = false;
            if (request.temperature() != null) {
                if (!hasParams) {
                    modelParam.append("@");
                    hasParams = true;
                } else {
                    modelParam.append(",");
                }
                modelParam.append("temperature=").append(request.temperature());
            }
            if (request.topP() != null) {
                if (!hasParams) {
                    modelParam.append("@");
                    hasParams = true;
                } else {
                    modelParam.append(",");
                }
                modelParam.append("top_p=").append(request.topP());
            }
            if (request.topK() != null) {
                if (!hasParams) {
                    modelParam.append("@");
                    hasParams = true;
                } else {
                    modelParam.append(",");
                }
                modelParam.append("top_k=").append(request.topK());
            }

            builder.model(modelParam.toString());
        }

        // Map max_tokens to timeout (heuristic: 100 tokens ≈ 1 second)
        if (request.maxTokens() != null) {
            builder.timeout(Duration.ofSeconds(Math.max(request.maxTokens() / 100, 30)));
        }

        return builder.build();
    }

    /**
     * Converts content object to string.
     *
     * @param content the content (String or List of blocks)
     * @return the string content
     */
    private static String contentToString(Object content) {
        if (content instanceof String text) {
            return text;
        } else if (content instanceof java.util.List<?> blocks) {
            return blocks.stream()
                    .filter(block -> block instanceof Message.TextContentBlock)
                    .map(block -> ((Message.TextContentBlock) block).text())
                    .collect(Collectors.joining("\n"));
        }
        return content.toString();
    }
}
