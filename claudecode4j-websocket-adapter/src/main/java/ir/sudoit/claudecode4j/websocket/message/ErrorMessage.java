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
import org.jspecify.annotations.Nullable;

/**
 * Message from server indicating an error.
 *
 * @param sessionId the session ID
 * @param code the error code
 * @param message the error message
 * @param details optional additional details
 * @param timestamp the message timestamp
 */
public record ErrorMessage(
        String sessionId,
        String code,
        String message,
        @Nullable String details,
        Instant timestamp) implements WebSocketMessage {

    @Override
    public String type() {
        return "error";
    }

    public static ErrorMessage of(String sessionId, String code, String message) {
        return new ErrorMessage(sessionId, code, message, null, Instant.now());
    }

    public static ErrorMessage of(String sessionId, String code, String message, String details) {
        return new ErrorMessage(sessionId, code, message, details, Instant.now());
    }

    public static ErrorMessage timeout(String sessionId) {
        return of(sessionId, "TIMEOUT", "Session timed out");
    }

    public static ErrorMessage executionError(String sessionId, String details) {
        return of(sessionId, "EXECUTION_ERROR", "Failed to execute prompt", details);
    }
}
