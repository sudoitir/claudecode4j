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
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class StreamJsonParser implements OutputParser {

    private static final Pattern TYPE_PATTERN = Pattern.compile("\"type\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern CONTENT_PATTERN = Pattern.compile("\"content\"\\s*:\\s*\"([^\"]*(?:\\\\.[^\"]*)*)\"");
    private static final Pattern MESSAGE_PATTERN = Pattern.compile("\"message\"\\s*:\\s*\"([^\"]*(?:\\\\.[^\"]*)*)\"");

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
        return lines.filter(line -> !line.isBlank()).map(this::parseLine).filter(event -> event != null);
    }

    private StreamEvent parseLine(String line) {
        var trimmed = line.trim();
        if (!trimmed.startsWith("{")) {
            return StreamEvent.of(StreamEvent.EventType.ASSISTANT, trimmed, sequenceCounter.incrementAndGet());
        }

        var typeMatcher = TYPE_PATTERN.matcher(trimmed);
        var type = typeMatcher.find() ? mapType(typeMatcher.group(1)) : StreamEvent.EventType.ASSISTANT;

        var contentMatcher = CONTENT_PATTERN.matcher(trimmed);
        var messageMatcher = MESSAGE_PATTERN.matcher(trimmed);

        String content;
        if (contentMatcher.find()) {
            content = unescapeJson(contentMatcher.group(1));
        } else if (messageMatcher.find()) {
            content = unescapeJson(messageMatcher.group(1));
        } else {
            content = "";
        }

        return new StreamEvent(
                type,
                content,
                Instant.now(),
                sequenceCounter.incrementAndGet(),
                extractToolName(trimmed),
                extractToolInput(trimmed));
    }

    private StreamEvent.EventType mapType(String type) {
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

    private String unescapeJson(String value) {
        return value.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private String extractToolName(String json) {
        var pattern = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
        var matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractToolInput(String json) {
        var pattern = Pattern.compile("\"input\"\\s*:\\s*\\{([^}]*)\\}");
        var matcher = pattern.matcher(json);
        return matcher.find() ? "{" + matcher.group(1) + "}" : null;
    }
}
