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
 * Message from client responding to a permission request.
 *
 * @param sessionId the session ID
 * @param requestId the permission request ID being responded to
 * @param granted whether permission is granted
 * @param reason optional reason for denial
 * @param timestamp the message timestamp
 */
public record PermissionResponseMessage(
        String sessionId,
        String requestId,
        boolean granted,
        @Nullable String reason,
        Instant timestamp) implements WebSocketMessage {

    @Override
    public String type() {
        return "permission_response";
    }

    public static PermissionResponseMessage grant(String sessionId, String requestId) {
        return new PermissionResponseMessage(sessionId, requestId, true, null, Instant.now());
    }

    public static PermissionResponseMessage deny(String sessionId, String requestId, String reason) {
        return new PermissionResponseMessage(sessionId, requestId, false, reason, Instant.now());
    }
}
