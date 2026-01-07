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
package ir.sudoit.claudecode4j.api.session;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory session store for testing and development.
 *
 * <p>Note: Sessions are not persisted across application restarts.
 */
public final class InMemorySessionStore implements SessionStore {

    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();

    @Override
    public void save(SessionData data) {
        sessions.put(data.sessionId(), data);
    }

    @Override
    public Optional<SessionData> load(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    @Override
    public boolean delete(String sessionId) {
        return sessions.remove(sessionId) != null;
    }

    @Override
    public List<String> listIds() {
        return new ArrayList<>(sessions.keySet());
    }

    @Override
    public List<SessionData> listAll() {
        return new ArrayList<>(sessions.values());
    }

    @Override
    public int deleteOlderThan(Duration maxAge) {
        var cutoff = Instant.now().minus(maxAge);
        var toDelete = sessions.entrySet().stream()
                .filter(e -> e.getValue().lastActiveAt().isBefore(cutoff))
                .map(Map.Entry::getKey)
                .toList();

        toDelete.forEach(sessions::remove);
        return toDelete.size();
    }

    @Override
    public void clear() {
        sessions.clear();
    }

    @Override
    public int priority() {
        return 10;
    }

    /** Returns the number of stored sessions. */
    public int size() {
        return sessions.size();
    }
}
