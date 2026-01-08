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

import java.util.List;

/** Test factory methods for creating {@link ChatCompletionRequest} instances. */
public final class TestChatCompletionRequest {

    private TestChatCompletionRequest() {}

    /**
     * Creates a simple user message request.
     *
     * @param content the user message content
     * @return a ChatCompletionRequest with a single user message
     */
    public static ChatCompletionRequest simpleUserMessage(String content) {
        return new ChatCompletionRequest(
                List.of(new ChatMessage("user", content, null, null, null)),
                "haiku",
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null);
    }

    /**
     * Creates a request with a system message and a user message.
     *
     * @param system the system message content
     * @param user the user message content
     * @return a ChatCompletionRequest with system and user messages
     */
    public static ChatCompletionRequest withSystemMessage(String system, String user) {
        return new ChatCompletionRequest(
                List.of(
                        new ChatMessage("system", system, null, null, null),
                        new ChatMessage("user", user, null, null, null)),
                "haiku",
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null);
    }

    /**
     * Creates a request with streaming enabled or disabled.
     *
     * @param stream whether to enable streaming
     * @return a ChatCompletionRequest with streaming configured
     */
    public static ChatCompletionRequest streaming(boolean stream) {
        return new ChatCompletionRequest(
                List.of(new ChatMessage("user", "Test message", null, null, null)),
                "haiku",
                null,
                null,
                null,
                null,
                null,
                null,
                stream,
                null,
                null,
                null);
    }

    /**
     * Creates a request with temperature and top_p parameters.
     *
     * @param temperature the temperature parameter
     * @param topP the top_p parameter
     * @return a ChatCompletionRequest with temperature and top_p configured
     */
    public static ChatCompletionRequest withTemperature(Double temperature, Double topP) {
        return new ChatCompletionRequest(
                List.of(new ChatMessage("user", "Test message", null, null, null)),
                "haiku",
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                temperature,
                topP,
                null);
    }

    /**
     * Creates a request with max_tokens parameter.
     *
     * @param maxTokens the maximum number of tokens
     * @return a ChatCompletionRequest with max_tokens configured
     */
    public static ChatCompletionRequest withMaxTokens(Integer maxTokens) {
        return new ChatCompletionRequest(
                List.of(new ChatMessage("user", "Test message", null, null, null)),
                "haiku",
                null,
                maxTokens,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null);
    }

    /**
     * Creates an invalid request with empty messages array (for testing validation).
     *
     * @return an invalid ChatCompletionRequest
     */
    public static ChatCompletionRequest emptyMessages() {
        return new ChatCompletionRequest(
                List.of(), "haiku", null, null, null, null, null, null, false, null, null, null);
    }

    /**
     * Creates an invalid request with missing model (for testing validation).
     *
     * @return an invalid ChatCompletionRequest
     */
    public static ChatCompletionRequest missingModel() {
        return new ChatCompletionRequest(
                List.of(new ChatMessage("user", "Test message", null, null, null)),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null);
    }
}
