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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * File-based session store using simple text format.
 *
 * <p>Each session is stored in a separate file with .session extension. Uses a simple key=value format for portability
 * (no JSON dependency in API module).
 */
public final class FileSessionStore implements SessionStore {

    private static final String SESSION_EXTENSION = ".session";

    private final Path storageDirectory;

    public FileSessionStore(Path storageDirectory) throws IOException {
        this.storageDirectory = storageDirectory;
        Files.createDirectories(storageDirectory);
    }

    @Override
    public void save(SessionData data) throws IOException {
        var path = getSessionPath(data.sessionId());
        var content = serialize(data);
        Files.writeString(path, content);
    }

    @Override
    public Optional<SessionData> load(String sessionId) throws IOException {
        var path = getSessionPath(sessionId);
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        var content = Files.readString(path);
        return Optional.of(deserialize(sessionId, content));
    }

    @Override
    public boolean delete(String sessionId) throws IOException {
        var path = getSessionPath(sessionId);
        return Files.deleteIfExists(path);
    }

    @Override
    public List<String> listIds() throws IOException {
        if (!Files.exists(storageDirectory)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.list(storageDirectory)) {
            return paths.filter(p -> p.toString().endsWith(SESSION_EXTENSION))
                    .map(p -> {
                        var name = p.getFileName().toString();
                        return name.substring(0, name.length() - SESSION_EXTENSION.length());
                    })
                    .toList();
        }
    }

    @Override
    public int deleteOlderThan(Duration maxAge) throws IOException {
        var cutoff = Instant.now().minus(maxAge);
        var count = 0;

        for (var sessionId : listIds()) {
            var data = load(sessionId);
            if (data.isPresent() && data.get().lastActiveAt().isBefore(cutoff)) {
                delete(sessionId);
                count++;
            }
        }

        return count;
    }

    @Override
    public void clear() throws IOException {
        for (var sessionId : listIds()) {
            delete(sessionId);
        }
    }

    @Override
    public int priority() {
        return 20;
    }

    private Path getSessionPath(String sessionId) {
        // Sanitize session ID to prevent path traversal
        var safeName = sessionId.replaceAll("[^a-zA-Z0-9_-]", "_");
        return storageDirectory.resolve(safeName + SESSION_EXTENSION);
    }

    private String serialize(SessionData data) {
        var sb = new StringBuilder();
        sb.append("sessionId=").append(data.sessionId()).append("\n");
        if (data.systemPrompt() != null) {
            sb.append("systemPrompt=").append(escape(data.systemPrompt())).append("\n");
        }
        sb.append("createdAt=").append(data.createdAt()).append("\n");
        sb.append("lastActiveAt=").append(data.lastActiveAt()).append("\n");

        // Metadata
        for (var entry : data.metadata().entrySet()) {
            sb.append("meta.")
                    .append(entry.getKey())
                    .append("=")
                    .append(escape(entry.getValue()))
                    .append("\n");
        }

        // History
        var i = 0;
        for (var entry : data.history()) {
            sb.append("history.")
                    .append(i)
                    .append(".prompt=")
                    .append(escape(entry.prompt()))
                    .append("\n");
            sb.append("history.")
                    .append(i)
                    .append(".response=")
                    .append(escape(entry.response()))
                    .append("\n");
            sb.append("history.")
                    .append(i)
                    .append(".timestamp=")
                    .append(entry.timestamp())
                    .append("\n");
            if (entry.model() != null) {
                sb.append("history.")
                        .append(i)
                        .append(".model=")
                        .append(entry.model())
                        .append("\n");
            }
            if (entry.tokensUsed() != null) {
                sb.append("history.")
                        .append(i)
                        .append(".tokensUsed=")
                        .append(entry.tokensUsed())
                        .append("\n");
            }
            i++;
        }

        return sb.toString();
    }

    private SessionData deserialize(String sessionId, String content) {
        var props = new HashMap<String, String>();
        for (var line : content.split("\n")) {
            var eqIndex = line.indexOf('=');
            if (eqIndex > 0) {
                props.put(line.substring(0, eqIndex), unescape(line.substring(eqIndex + 1)));
            }
        }

        var systemPrompt = props.get("systemPrompt");
        var createdAt =
                Instant.parse(props.getOrDefault("createdAt", Instant.now().toString()));
        var lastActiveAt =
                Instant.parse(props.getOrDefault("lastActiveAt", Instant.now().toString()));

        // Parse metadata
        var metadata = new HashMap<String, String>();
        for (var entry : props.entrySet()) {
            if (entry.getKey().startsWith("meta.")) {
                metadata.put(entry.getKey().substring(5), entry.getValue());
            }
        }

        // Parse history
        var history = new ArrayList<SessionData.ConversationEntry>();
        for (var i = 0; ; i++) {
            var prefix = "history." + i + ".";
            if (!props.containsKey(prefix + "prompt")) {
                break;
            }
            var prompt = props.get(prefix + "prompt");
            var response = props.get(prefix + "response");
            var timestamp = Instant.parse(
                    props.getOrDefault(prefix + "timestamp", Instant.now().toString()));
            var model = props.get(prefix + "model");
            Integer tokensUsed = null;
            if (props.containsKey(prefix + "tokensUsed")) {
                tokensUsed = Integer.parseInt(props.get(prefix + "tokensUsed"));
            }
            history.add(new SessionData.ConversationEntry(prompt, response, timestamp, model, tokensUsed));
        }

        return new SessionData(sessionId, systemPrompt, history, metadata, createdAt, lastActiveAt);
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r");
    }

    private String unescape(String value) {
        return value.replace("\\n", "\n").replace("\\r", "\r").replace("\\\\", "\\");
    }
}
