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

import java.util.List;

/** Test factory methods for creating {@link MessageRequest} instances. */
public final class TestMessageRequest {

    private TestMessageRequest() {}

    /**
     * Creates a simple user message request.
     *
     * @param content the user message content
     * @return a MessageRequest with a single user message
     */
    public static MessageRequest simpleUserMessage(String content) {
        return new MessageRequest(
                "claude-3-5-haiku-20241022",
                List.of(new Message("user", content)),
                1024,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null);
    }

    /**
     * Creates a request with a system prompt and a user message.
     *
     * @param system the system prompt
     * @param user the user message content
     * @return a MessageRequest with system prompt and user message
     */
    public static MessageRequest withSystemPrompt(String system, String user) {
        return new MessageRequest(
                "claude-3-5-haiku-20241022",
                List.of(new Message("user", user)),
                1024,
                system,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null);
    }

    /**
     * Creates a request with streaming enabled or disabled.
     *
     * @param stream whether to enable streaming
     * @return a MessageRequest with streaming configured
     */
    public static MessageRequest streaming(boolean stream) {
        return new MessageRequest(
                "claude-3-5-haiku-20241022",
                List.of(new Message("user", "Test message")),
                1024,
                null,
                null,
                null,
                null,
                stream,
                null,
                null,
                null,
                null);
    }

    /**
     * Creates a request with temperature, top_p, and top_k parameters.
     *
     * @param temperature the temperature parameter
     * @param topP the top_p parameter
     * @param topK the top_k parameter
     * @return a MessageRequest with sampling parameters configured
     */
    public static MessageRequest withSamplingParameters(Double temperature, Double topP, Integer topK) {
        return new MessageRequest(
                "claude-3-5-haiku-20241022",
                List.of(new Message("user", "Test message")),
                1024,
                null,
                temperature,
                topP,
                topK,
                false,
                null,
                null,
                null,
                null);
    }

    /**
     * Creates a request with stop sequences.
     *
     * @param stopSequences the stop sequences
     * @return a MessageRequest with stop sequences configured
     */
    public static MessageRequest withStopSequences(List<String> stopSequences) {
        return new MessageRequest(
                "claude-3-5-haiku-20241022",
                List.of(new Message("user", "Test message")),
                1024,
                null,
                null,
                null,
                null,
                false,
                stopSequences,
                null,
                null,
                null);
    }

    /**
     * Creates an invalid request with empty messages array (for testing validation).
     *
     * @return an invalid MessageRequest
     */
    public static MessageRequest emptyMessages() {
        return new MessageRequest(
                "claude-3-5-haiku-20241022", List.of(), 1024, null, null, null, null, false, null, null, null, null);
    }

    /**
     * Creates an invalid request with missing model (for testing validation).
     *
     * @return an invalid MessageRequest
     */
    public static MessageRequest missingModel() {
        return new MessageRequest(
                null,
                List.of(new Message("user", "Test message")),
                1024,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null);
    }

    /**
     * Creates an invalid request with missing max_tokens (for testing validation).
     *
     * @return an invalid MessageRequest
     */
    public static MessageRequest missingMaxTokens() {
        return new MessageRequest(
                "claude-3-5-haiku-20241022",
                List.of(new Message("user", "Test message")),
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null);
    }
}
