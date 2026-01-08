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
package ir.sudoit.claudecode4j.rest.streaming;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

/** Utilities for testing SSE streams with full compliance verification. */
public final class SseTestUtils {

    private static final Pattern DATA_PATTERN = Pattern.compile("^data:\\s*(.+)$");
    private static final Pattern EVENT_PATTERN = Pattern.compile("^event:\\s*(.+)$");
    private static final Pattern HEARTBEAT_PATTERN = Pattern.compile("^:\\s*(.+)$");

    private SseTestUtils() {}

    /**
     * Consumes an SSE stream and returns the raw events as a list of strings. Use this to verify SSE formatting
     * (event:, data: prefixes).
     *
     * @param responseSpec the WebTestClient response spec
     * @return list of raw SSE event blocks
     */
    public static List<String> consumeSseStream(WebTestClient.ResponseSpec responseSpec) {
        // Fetch raw bytes to preserve SSE formatting (event:, data:)
        EntityExchangeResult<byte[]> result =
                responseSpec.expectBody(byte[].class).returnResult();

        byte[] body = result.getResponseBody();
        if (body == null || body.length == 0) {
            return new ArrayList<>();
        }

        String content = new String(body, StandardCharsets.UTF_8);

        // Split by double newline to separate event blocks
        // Filter out empty blocks which might result from trailing newlines
        return Arrays.stream(content.split("\n\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /**
     * Parses a raw SSE event string into its components.
     *
     * @param rawEvent the raw SSE event string
     * @return the parsed SseEvent
     */
    public static SseEvent parseEvent(String rawEvent) {
        String eventType = null;
        String data = null;
        boolean isHeartbeat = false;

        String[] lines = rawEvent.split("\n");
        for (String line : lines) {
            Matcher heartbeatMatcher = HEARTBEAT_PATTERN.matcher(line.trim());
            if (heartbeatMatcher.matches()) {
                isHeartbeat = true;
                continue;
            }

            Matcher eventMatcher = EVENT_PATTERN.matcher(line.trim());
            if (eventMatcher.matches()) {
                eventType = eventMatcher.group(1);
                continue;
            }

            Matcher dataMatcher = DATA_PATTERN.matcher(line.trim());
            if (dataMatcher.matches()) {
                data = dataMatcher.group(1);
            }
        }

        return new SseEvent(eventType, data, isHeartbeat);
    }

    /**
     * Verifies OpenAI SSE format compliance.
     *
     * @param events the list of raw SSE events
     */
    public static void verifyOpenAiFormat(List<String> events) {
        boolean foundDone = false;
        for (String rawEvent : events) {
            SseEvent event = parseEvent(rawEvent);

            // Skip heartbeats
            if (event.isHeartbeat()) {
                continue;
            }

            // Verify data exists
            if (event.data() == null) {
                throw new AssertionError("OpenAI SSE event missing 'data:' prefix: " + rawEvent);
            }

            // Check for [DONE] marker
            if ("[DONE]".equals(event.data())) {
                foundDone = true;
            } else {
                // Verify it's valid JSON
                String dataValue = event.data();
                if (!dataValue.startsWith("{")) {
                    throw new AssertionError("OpenAI SSE data is not valid JSON: " + dataValue);
                }
            }
        }

        if (!foundDone) {
            throw new AssertionError("OpenAI SSE stream missing [DONE] marker");
        }
    }

    /**
     * Verifies Anthropic SSE format compliance.
     *
     * @param events the list of raw SSE events
     */
    public static void verifyAnthropicFormat(List<String> events) {
        boolean foundMessageStart = false;
        boolean foundContentBlockDelta = false;
        boolean foundMessageDelta = false;
        boolean foundMessageStop = false;

        for (String rawEvent : events) {
            SseEvent event = parseEvent(rawEvent);

            // Skip heartbeats
            if (event.isHeartbeat()) {
                continue;
            }

            // Verify event type exists
            if (event.eventType() == null) {
                throw new AssertionError("Anthropic SSE event missing 'event:' prefix: " + rawEvent);
            }

            // Verify data exists
            if (event.data() == null) {
                throw new AssertionError("Anthropic SSE event missing 'data:' prefix: " + rawEvent);
            }

            // Track event types
            switch (event.eventType()) {
                case "message_start" -> foundMessageStart = true;
                case "content_block_delta" -> foundContentBlockDelta = true;
                case "message_delta" -> foundMessageDelta = true;
                case "message_stop" -> foundMessageStop = true;
            }
        }

        if (!foundMessageStart) {
            throw new AssertionError("Anthropic SSE stream missing message_start event");
        }
        if (!foundMessageStop) {
            throw new AssertionError("Anthropic SSE stream missing message_stop event");
        }
    }

    /**
     * Verifies heartbeat comments are present within the maximum interval.
     *
     * @param events the list of raw SSE events
     * @param maxInterval the maximum interval between heartbeats
     */
    public static void verifyHeartbeats(List<String> events, Duration maxInterval) {
        Instant lastHeartbeat = Instant.now();
        for (String rawEvent : events) {
            SseEvent event = parseEvent(rawEvent);
            if (event.isHeartbeat()) {
                Instant now = Instant.now();
                Duration elapsed = Duration.between(lastHeartbeat, now);
                if (elapsed.compareTo(maxInterval) > 0) {
                    throw new AssertionError("Heartbeat interval exceeded: " + elapsed + " > " + maxInterval);
                }
                lastHeartbeat = now;
            }
        }
    }

    /**
     * Verifies proper event ordering.
     *
     * @param events the list of raw SSE events
     * @param expectedTypes the expected event types in order
     */
    public static void verifyEventOrdering(List<String> events, List<String> expectedTypes) {
        List<String> actualTypes = new ArrayList<>();
        for (String rawEvent : events) {
            SseEvent event = parseEvent(rawEvent);
            if (event.eventType() != null) {
                actualTypes.add(event.eventType());
            }
        }

        if (actualTypes.size() != expectedTypes.size()) {
            throw new AssertionError(
                    "Event count mismatch: expected " + expectedTypes.size() + ", got " + actualTypes.size());
        }

        for (int i = 0; i < expectedTypes.size(); i++) {
            if (!actualTypes.get(i).equals(expectedTypes.get(i))) {
                throw new AssertionError("Event type mismatch at index " + i + ": expected " + expectedTypes.get(i)
                        + ", got " + actualTypes.get(i));
            }
        }
    }

    /**
     * Record representing a parsed SSE event.
     *
     * @param eventType the event type (null if not specified)
     * @param data the event data (null if not specified)
     * @param isHeartbeat whether this is a heartbeat comment
     */
    public record SseEvent(String eventType, String data, boolean isHeartbeat) {}
}
