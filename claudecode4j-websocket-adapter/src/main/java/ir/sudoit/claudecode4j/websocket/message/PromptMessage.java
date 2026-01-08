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
package ir.sudoit.claudecode4j.websocket.message;

import java.time.Instant;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Message from client containing a prompt to execute.
 *
 * @param sessionId the session ID
 * @param text the prompt text
 * @param systemPrompt optional system prompt
 * @param contextFiles optional context files
 * @param timestamp the message timestamp
 */
public record PromptMessage(
        String sessionId,
        String text,
        @Nullable String systemPrompt,
        @Nullable List<String> contextFiles,
        Instant timestamp)
        implements WebSocketMessage {

    @Override
    public String type() {
        return "prompt";
    }

    public static PromptMessage of(String sessionId, String text) {
        return new PromptMessage(sessionId, text, null, null, Instant.now());
    }
}
