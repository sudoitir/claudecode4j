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
package ir.sudoit.claudecode4j.websocket.session;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.web.socket.WebSocketSession;

/** Registry for managing active WebSocket sessions. */
public final class SessionRegistry {

    private final ConcurrentMap<String, SessionEntry> sessions = new ConcurrentHashMap<>();
    private final int maxSessions;

    public SessionRegistry() {
        this(100);
    }

    public SessionRegistry(int maxSessions) {
        this.maxSessions = maxSessions;
    }

    /**
     * Registers a new WebSocket session.
     *
     * @param session the WebSocket session
     * @return the generated session ID
     * @throws IllegalStateException if max sessions limit is reached
     */
    public String register(WebSocketSession session) {
        if (sessions.size() >= maxSessions) {
            throw new IllegalStateException("Maximum session limit reached: " + maxSessions);
        }

        String sessionId = session.getId();
        sessions.put(sessionId, new SessionEntry(session, Instant.now()));
        return sessionId;
    }

    /**
     * Unregisters a session by ID.
     *
     * @param sessionId the session ID
     */
    public void unregister(String sessionId) {
        sessions.remove(sessionId);
    }

    /**
     * Gets a session by ID.
     *
     * @param sessionId the session ID
     * @return the session if found
     */
    public Optional<WebSocketSession> get(String sessionId) {
        SessionEntry entry = sessions.get(sessionId);
        if (entry != null) {
            entry.updateLastActivity();
            return Optional.of(entry.session());
        }
        return Optional.empty();
    }

    /**
     * Cleans up sessions that have been idle longer than the specified duration.
     *
     * @param maxIdleTime the maximum idle time
     * @return the number of sessions cleaned up
     */
    public int cleanup(Duration maxIdleTime) {
        Instant cutoff = Instant.now().minus(maxIdleTime);
        int removed = 0;

        var iterator = sessions.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getValue().lastActivity().isBefore(cutoff)) {
                iterator.remove();
                removed++;
            }
        }

        return removed;
    }

    /**
     * Returns the number of active sessions.
     *
     * @return the session count
     */
    public int size() {
        return sessions.size();
    }

    /**
     * Returns all active sessions.
     *
     * @return map of session IDs to WebSocket sessions
     */
    public Map<String, WebSocketSession> getAllSessions() {
        return sessions.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey, e -> e.getValue().session()));
    }

    private static final class SessionEntry {
        private final WebSocketSession session;
        private volatile Instant lastActivity;

        SessionEntry(WebSocketSession session, Instant lastActivity) {
            this.session = session;
            this.lastActivity = lastActivity;
        }

        WebSocketSession session() {
            return session;
        }

        Instant lastActivity() {
            return lastActivity;
        }

        void updateLastActivity() {
            this.lastActivity = Instant.now();
        }
    }
}
