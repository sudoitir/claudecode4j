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
package ir.sudoit.claudecode4j.core.parser;

import ir.sudoit.claudecode4j.api.model.request.OutputFormat;
import ir.sudoit.claudecode4j.api.model.response.ClaudeResponse;
import ir.sudoit.claudecode4j.api.model.response.ErrorResponse;
import ir.sudoit.claudecode4j.api.model.response.StreamEvent;
import ir.sudoit.claudecode4j.api.model.response.StreamResponse;
import ir.sudoit.claudecode4j.api.model.response.TextResponse;
import ir.sudoit.claudecode4j.api.spi.OutputParser;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Jackson 3-based JSON parser for Claude CLI output.
 *
 * <p>Replaces the regex-based StreamJsonParser with proper JSON parsing using Jackson 3's JsonMapper for correct
 * handling of:
 *
 * <ul>
 *   <li>Nested JSON objects in tool inputs
 *   <li>All JSON escape sequences (backslash-b, backslash-f, unicode escapes, etc.)
 *   <li>Complex string content
 * </ul>
 */
public final class JacksonStreamParser implements OutputParser {

    private static final System.Logger log = System.getLogger(JacksonStreamParser.class.getName());
    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private final AtomicLong sequenceCounter = new AtomicLong(0);

    @Override
    public boolean supports(OutputFormat format) {
        return format == OutputFormat.STREAM_JSON || format == OutputFormat.JSON;
    }

    @Override
    public ClaudeResponse parse(String output, int exitCode) {
        var start = Instant.now();
        var events = new ArrayList<StreamEvent>();
        var contentBuilder = new StringBuilder();

        for (var line : output.split("\n")) {
            if (line.isBlank()) continue;

            var event = parseLine(line);
            if (event != null) {
                events.add(event);
                if (event.type() == StreamEvent.EventType.ASSISTANT || event.type() == StreamEvent.EventType.RESULT) {
                    contentBuilder.append(event.content());
                }
            }
        }

        var duration = Duration.between(start, Instant.now());
        var content = contentBuilder.toString().trim();

        if (exitCode != 0) {
            var errorContent = events.stream()
                    .filter(e -> e.type() == StreamEvent.EventType.ERROR)
                    .map(StreamEvent::content)
                    .findFirst()
                    .orElse(content.isEmpty() ? "Unknown error" : content);

            return new ErrorResponse(errorContent, start, duration, "CLI_ERROR", output, exitCode);
        }

        if (events.isEmpty()) {
            return new TextResponse(content.isEmpty() ? output.trim() : content, start, duration, null, null, null);
        }

        return new StreamResponse(content, start, duration, events, null, null);
    }

    @Override
    public Stream<StreamEvent> parseStream(Stream<String> lines) {
        return lines.filter(line -> !line.isBlank()).map(this::parseLine).filter(Objects::nonNull);
    }

    private @Nullable StreamEvent parseLine(String line) {
        var trimmed = line.trim();
        if (!trimmed.startsWith("{")) {
            return StreamEvent.of(StreamEvent.EventType.ASSISTANT, trimmed, sequenceCounter.incrementAndGet());
        }

        try {
            JsonNode root = JSON_MAPPER.readTree(trimmed);

            var type = mapType(getTextOrNull(root, "type"));
            var content = getTextOrNull(root, "content");
            if (content == null) {
                content = getTextOrNull(root, "message");
            }
            if (content == null) {
                content = "";
            }

            var toolName = getTextOrNull(root, "name");
            String toolInput = null;
            if (root.has("input")) {
                var inputNode = root.get("input");
                if (inputNode != null && !inputNode.isNull()) {
                    toolInput = JSON_MAPPER.writeValueAsString(inputNode);
                }
            }

            return new StreamEvent(
                    type, content, Instant.now(), sequenceCounter.incrementAndGet(), toolName, toolInput);
        } catch (Exception e) {
            log.log(System.Logger.Level.DEBUG, "Failed to parse JSON line, treating as text: {0}", e.getMessage());
            return StreamEvent.of(StreamEvent.EventType.ASSISTANT, trimmed, sequenceCounter.incrementAndGet());
        }
    }

    private @Nullable String getTextOrNull(JsonNode node, String field) {
        var child = node.get(field);
        return (child != null && child.isTextual()) ? child.asText() : null;
    }

    private StreamEvent.EventType mapType(@Nullable String type) {
        if (type == null) return StreamEvent.EventType.ASSISTANT;
        return switch (type.toLowerCase()) {
            case "system" -> StreamEvent.EventType.SYSTEM;
            case "assistant", "text" -> StreamEvent.EventType.ASSISTANT;
            case "user" -> StreamEvent.EventType.USER;
            case "result" -> StreamEvent.EventType.RESULT;
            case "tool_use" -> StreamEvent.EventType.TOOL_USE;
            case "tool_result" -> StreamEvent.EventType.TOOL_RESULT;
            case "error" -> StreamEvent.EventType.ERROR;
            case "message_stop", "complete" -> StreamEvent.EventType.COMPLETE;
            default -> StreamEvent.EventType.ASSISTANT;
        };
    }
}
