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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Serializable session data for persistent storage.
 *
 * <p>Contains all the information needed to restore a session.
 *
 * @param sessionId unique session identifier
 * @param systemPrompt the system prompt for this session
 * @param history list of conversation exchanges
 * @param metadata additional metadata (model, config, etc.)
 * @param createdAt when the session was created
 * @param lastActiveAt when the session was last used
 */
public record SessionData(
        String sessionId,
        @Nullable String systemPrompt,
        List<ConversationEntry> history,
        Map<String, String> metadata,
        Instant createdAt,
        Instant lastActiveAt) {

    /** Creates a new SessionData with empty history. */
    public static SessionData create(String sessionId) {
        var now = Instant.now();
        return new SessionData(sessionId, null, List.of(), Map.of(), now, now);
    }

    /** Creates a builder for SessionData. */
    public static Builder builder(String sessionId) {
        return new Builder(sessionId);
    }

    /** A single conversation entry (prompt + response). */
    public record ConversationEntry(
            String prompt,
            String response,
            Instant timestamp,
            @Nullable String model,
            @Nullable Integer tokensUsed) {

        public static ConversationEntry of(String prompt, String response) {
            return new ConversationEntry(prompt, response, Instant.now(), null, null);
        }
    }

    public static final class Builder {
        private final String sessionId;
        private @Nullable String systemPrompt;
        private List<ConversationEntry> history = List.of();
        private Map<String, String> metadata = Map.of();
        private Instant createdAt = Instant.now();
        private Instant lastActiveAt = Instant.now();

        private Builder(String sessionId) {
            this.sessionId = sessionId;
        }

        public Builder systemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        public Builder history(List<ConversationEntry> history) {
            this.history = history;
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder lastActiveAt(Instant lastActiveAt) {
            this.lastActiveAt = lastActiveAt;
            return this;
        }

        public SessionData build() {
            return new SessionData(sessionId, systemPrompt, history, metadata, createdAt, lastActiveAt);
        }
    }
}
