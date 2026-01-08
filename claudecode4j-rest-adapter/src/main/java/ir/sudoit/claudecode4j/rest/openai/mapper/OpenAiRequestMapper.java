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
package ir.sudoit.claudecode4j.rest.openai.mapper;

import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.api.model.request.PromptOptions;
import ir.sudoit.claudecode4j.rest.openai.dto.request.ChatCompletionRequest;
import ir.sudoit.claudecode4j.rest.openai.dto.request.ChatMessage;
import java.time.Duration;
import java.util.stream.Collectors;

/** Maps between OpenAI API format and ClaudeCode4J domain models. */
public final class OpenAiRequestMapper {

    private OpenAiRequestMapper() {}

    /**
     * Converts OpenAI ChatCompletionRequest to ClaudeCode4J Prompt.
     *
     * <p>Strategy:
     *
     * <ul>
     *   <li>Extract system message (role=system) as systemPrompt
     *   <li>Concatenate user/assistant messages as text prompt
     *   <li>Future: Support multi-turn conversation context
     * </ul>
     *
     * @param request the OpenAI request
     * @return the mapped Prompt
     */
    public static Prompt toPrompt(ChatCompletionRequest request) {
        StringBuilder textBuilder = new StringBuilder();
        String systemPrompt = null;

        for (ChatMessage message : request.messages()) {
            switch (message.role()) {
                case "system" -> {
                    if (systemPrompt == null) {
                        systemPrompt = contentToString(message.content());
                    } else {
                        systemPrompt += "\n" + contentToString(message.content());
                    }
                }
                case "user", "assistant" -> {
                    if (!textBuilder.isEmpty()) {
                        textBuilder.append("\n\n");
                    }
                    textBuilder.append(message.role()).append(": ").append(contentToString(message.content()));
                }
                default -> {
                    // Ignore tool and other roles for now
                }
            }
        }

        var builder = Prompt.builder().text(textBuilder.toString());
        if (systemPrompt != null) {
            builder.systemPrompt(systemPrompt);
        }

        return builder.build();
    }

    /**
     * Converts OpenAI ChatCompletionRequest to ClaudeCode4J PromptOptions.
     *
     * <p>Maps model parameter and estimates timeout from max_tokens.
     *
     * @param request the OpenAI request
     * @return the mapped PromptOptions
     */
    public static PromptOptions toOptions(ChatCompletionRequest request) {
        var builder = PromptOptions.builder();

        // Map model parameter
        if (request.model() != null) {
            builder.model(request.model());
        }

        // Map max_tokens to timeout (heuristic: 100 tokens ≈ 1 second)
        if (request.maxTokens() != null) {
            builder.timeout(Duration.ofSeconds(Math.max(request.maxTokens() / 100, 30)));
        }

        // Note: temperature, top_p are passed to Claude CLI via model parameter
        // e.g., "claude-3-5-sonnet@temperature=0.7,top_p=0.9"

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
                    .filter(block -> block instanceof ChatMessage.TextContentBlock)
                    .map(block -> ((ChatMessage.TextContentBlock) block).text())
                    .collect(Collectors.joining("\n"));
        }
        return content.toString();
    }
}
