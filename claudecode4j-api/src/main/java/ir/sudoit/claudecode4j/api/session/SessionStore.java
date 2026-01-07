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

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * SPI for persistent session storage.
 *
 * <p>Implementations can store sessions in files, databases, or other backends.
 */
public interface SessionStore {

    /**
     * Saves session data to the store.
     *
     * @param data the session data to save
     * @throws IOException if the save fails
     */
    void save(SessionData data) throws IOException;

    /**
     * Loads session data by ID.
     *
     * @param sessionId the session ID
     * @return the session data if found
     * @throws IOException if the load fails
     */
    Optional<SessionData> load(String sessionId) throws IOException;

    /**
     * Deletes a session by ID.
     *
     * @param sessionId the session ID
     * @return true if the session was deleted, false if not found
     * @throws IOException if the delete fails
     */
    boolean delete(String sessionId) throws IOException;

    /**
     * Lists all stored session IDs.
     *
     * @return list of session IDs
     * @throws IOException if the listing fails
     */
    List<String> listIds() throws IOException;

    /**
     * Lists all stored sessions with full data.
     *
     * @return list of session data
     * @throws IOException if the listing fails
     */
    default List<SessionData> listAll() throws IOException {
        return listIds().stream()
                .map(id -> {
                    try {
                        return load(id).orElse(null);
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    /**
     * Checks if a session exists.
     *
     * @param sessionId the session ID
     * @return true if exists
     * @throws IOException if the check fails
     */
    default boolean exists(String sessionId) throws IOException {
        return load(sessionId).isPresent();
    }

    /**
     * Deletes sessions older than the specified age.
     *
     * @param maxAge maximum session age
     * @return number of sessions deleted
     * @throws IOException if the cleanup fails
     */
    int deleteOlderThan(Duration maxAge) throws IOException;

    /**
     * Clears all stored sessions.
     *
     * @throws IOException if the clear fails
     */
    void clear() throws IOException;

    /** Priority for SPI selection (higher = preferred). */
    default int priority() {
        return 0;
    }
}
