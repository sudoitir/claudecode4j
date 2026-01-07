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

import static org.assertj.core.api.Assertions.assertThat;

import ir.sudoit.claudecode4j.api.model.request.OutputFormat;
import ir.sudoit.claudecode4j.api.model.response.ErrorResponse;
import ir.sudoit.claudecode4j.api.model.response.StreamEvent;
import ir.sudoit.claudecode4j.api.model.response.StreamResponse;
import ir.sudoit.claudecode4j.api.model.response.TextResponse;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("JacksonStreamParser")
class JacksonStreamParserTest {

    private JacksonStreamParser parser;

    @BeforeEach
    void setUp() {
        parser = new JacksonStreamParser();
    }

    @Nested
    @DisplayName("supports")
    class Supports {

        @Test
        @DisplayName("should support STREAM_JSON format")
        void shouldSupportStreamJsonFormat() {
            assertThat(parser.supports(OutputFormat.STREAM_JSON)).isTrue();
        }

        @Test
        @DisplayName("should support JSON format")
        void shouldSupportJsonFormat() {
            assertThat(parser.supports(OutputFormat.JSON)).isTrue();
        }

        @Test
        @DisplayName("should not support TEXT format")
        void shouldNotSupportTextFormat() {
            assertThat(parser.supports(OutputFormat.TEXT)).isFalse();
        }
    }

    @Nested
    @DisplayName("parse")
    class Parse {

        @Test
        @DisplayName("should parse plain text output as StreamResponse with ASSISTANT event")
        void shouldParsePlainTextOutputAsStreamResponse() {
            var output = "Hello, I am Claude!";
            var result = parser.parse(output, 0);

            // Plain text is treated as an ASSISTANT event, resulting in StreamResponse
            assertThat(result).isInstanceOf(StreamResponse.class);
            var streamResponse = (StreamResponse) result;
            assertThat(streamResponse.content()).isEqualTo("Hello, I am Claude!");
            assertThat(streamResponse.events()).hasSize(1);
            assertThat(streamResponse.events().getFirst().type()).isEqualTo(StreamEvent.EventType.ASSISTANT);
            assertThat(streamResponse.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("should parse JSON stream output as StreamResponse")
        void shouldParseJsonStreamOutputAsStreamResponse() {
            var output = """
                    {"type": "assistant", "content": "Hello "}
                    {"type": "assistant", "content": "World!"}
                    {"type": "complete"}
                    """;
            var result = parser.parse(output, 0);

            assertThat(result).isInstanceOf(StreamResponse.class);
            var streamResponse = (StreamResponse) result;
            assertThat(streamResponse.content()).isEqualTo("Hello World!");
            assertThat(streamResponse.events()).hasSize(3);
            assertThat(streamResponse.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("should parse error output with non-zero exit code")
        void shouldParseErrorOutputWithNonZeroExitCode() {
            var output = """
                    {"type": "error", "message": "Authentication failed"}
                    """;
            var result = parser.parse(output, 1);

            assertThat(result).isInstanceOf(ErrorResponse.class);
            var errorResponse = (ErrorResponse) result;
            assertThat(errorResponse.content()).isEqualTo("Authentication failed");
            assertThat(errorResponse.exitCode()).isEqualTo(1);
            assertThat(errorResponse.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("should extract tool name and input from tool_use events")
        void shouldExtractToolNameAndInputFromToolUseEvents() {
            var output = """
                    {"type": "tool_use", "name": "Read", "input": {"file": "/path/to/file"}}
                    """;
            var result = parser.parse(output, 0);

            assertThat(result).isInstanceOf(StreamResponse.class);
            var streamResponse = (StreamResponse) result;
            var event = streamResponse.events().getFirst();
            assertThat(event.type()).isEqualTo(StreamEvent.EventType.TOOL_USE);
            assertThat(event.toolName()).isEqualTo("Read");
            assertThat(event.toolInput()).contains("file");
            assertThat(event.toolInput()).contains("/path/to/file");
        }

        @Test
        @DisplayName("should handle JSON with escaped characters")
        void shouldHandleJsonWithEscapedCharacters() {
            var output = """
                    {"type": "assistant", "content": "Line1\\nLine2\\tTabbed"}
                    """;
            var result = parser.parse(output, 0);

            assertThat(result).isInstanceOf(StreamResponse.class);
            var streamResponse = (StreamResponse) result;
            assertThat(streamResponse.content()).isEqualTo("Line1\nLine2\tTabbed");
        }

        @Test
        @DisplayName("should handle mixed JSON and plain text lines")
        void shouldHandleMixedJsonAndPlainTextLines() {
            var output = """
                    Plain text line
                    {"type": "assistant", "content": "JSON content"}
                    Another plain text
                    """;
            var result = parser.parse(output, 0);

            assertThat(result).isInstanceOf(StreamResponse.class);
            var streamResponse = (StreamResponse) result;
            assertThat(streamResponse.events()).hasSize(3);
        }

        @Test
        @DisplayName("should skip blank lines")
        void shouldSkipBlankLines() {
            var output = """

                    {"type": "assistant", "content": "Hello"}

                    {"type": "assistant", "content": "World"}

                    """;
            var result = parser.parse(output, 0);

            assertThat(result).isInstanceOf(StreamResponse.class);
            var streamResponse = (StreamResponse) result;
            assertThat(streamResponse.events()).hasSize(2);
        }

        @Test
        @DisplayName("should map all event types correctly")
        void shouldMapAllEventTypesCorrectly() {
            var output = """
                    {"type": "system", "content": "sys"}
                    {"type": "assistant", "content": "asst"}
                    {"type": "text", "content": "txt"}
                    {"type": "user", "content": "usr"}
                    {"type": "result", "content": "res"}
                    {"type": "tool_use", "content": "tool"}
                    {"type": "tool_result", "content": "toolres"}
                    {"type": "error", "content": "err"}
                    {"type": "message_stop", "content": "stop"}
                    {"type": "complete", "content": "done"}
                    {"type": "unknown", "content": "unk"}
                    """;
            var result = parser.parse(output, 0);

            assertThat(result).isInstanceOf(StreamResponse.class);
            var events = ((StreamResponse) result).events();
            assertThat(events.get(0).type()).isEqualTo(StreamEvent.EventType.SYSTEM);
            assertThat(events.get(1).type()).isEqualTo(StreamEvent.EventType.ASSISTANT);
            assertThat(events.get(2).type()).isEqualTo(StreamEvent.EventType.ASSISTANT); // text maps to assistant
            assertThat(events.get(3).type()).isEqualTo(StreamEvent.EventType.USER);
            assertThat(events.get(4).type()).isEqualTo(StreamEvent.EventType.RESULT);
            assertThat(events.get(5).type()).isEqualTo(StreamEvent.EventType.TOOL_USE);
            assertThat(events.get(6).type()).isEqualTo(StreamEvent.EventType.TOOL_RESULT);
            assertThat(events.get(7).type()).isEqualTo(StreamEvent.EventType.ERROR);
            assertThat(events.get(8).type()).isEqualTo(StreamEvent.EventType.COMPLETE);
            assertThat(events.get(9).type()).isEqualTo(StreamEvent.EventType.COMPLETE);
            assertThat(events.get(10).type())
                    .isEqualTo(StreamEvent.EventType.ASSISTANT); // unknown defaults to assistant
        }

        @Test
        @DisplayName("should handle nested JSON in tool input")
        void shouldHandleNestedJsonInToolInput() {
            var output = """
                    {"type": "tool_use", "name": "Write", "input": {"path": "/file", "content": {"nested": {"deep": "value"}}}}
                    """;
            var result = parser.parse(output, 0);

            assertThat(result).isInstanceOf(StreamResponse.class);
            var event = ((StreamResponse) result).events().getFirst();
            assertThat(event.toolInput()).contains("nested");
            assertThat(event.toolInput()).contains("deep");
            assertThat(event.toolInput()).contains("value");
        }

        @Test
        @DisplayName("should return TextResponse only when output is completely empty")
        void shouldReturnTextResponseWhenOutputIsEmpty() {
            var output = "";
            var result = parser.parse(output, 0);

            // Empty output results in TextResponse since no events are created
            assertThat(result).isInstanceOf(TextResponse.class);
            assertThat(((TextResponse) result).content()).isEmpty();
        }

        @Test
        @DisplayName("should return StreamResponse for plain text (treated as ASSISTANT events)")
        void shouldReturnStreamResponseForPlainText() {
            var output = "Just plain text without any JSON";
            var result = parser.parse(output, 0);

            // Plain text creates ASSISTANT events, so it returns StreamResponse
            assertThat(result).isInstanceOf(StreamResponse.class);
            var streamResponse = (StreamResponse) result;
            assertThat(streamResponse.content()).isEqualTo("Just plain text without any JSON");
            assertThat(streamResponse.events()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("parseStream")
    class ParseStream {

        @Test
        @DisplayName("should parse stream of JSON lines")
        void shouldParseStreamOfJsonLines() {
            var lines = Stream.of(
                    "{\"type\": \"assistant\", \"content\": \"Hello\"}",
                    "{\"type\": \"assistant\", \"content\": \" World\"}");

            var events = parser.parseStream(lines).toList();

            assertThat(events).hasSize(2);
            assertThat(events.get(0).content()).isEqualTo("Hello");
            assertThat(events.get(1).content()).isEqualTo(" World");
        }

        @Test
        @DisplayName("should filter blank lines in stream")
        void shouldFilterBlankLinesInStream() {
            var lines = Stream.of("", "{\"type\": \"assistant\", \"content\": \"Test\"}", "   ");

            var events = parser.parseStream(lines).toList();

            assertThat(events).hasSize(1);
        }

        @Test
        @DisplayName("should handle plain text lines in stream")
        void shouldHandlePlainTextLinesInStream() {
            var lines = Stream.of("Plain text output");

            var events = parser.parseStream(lines).toList();

            assertThat(events).hasSize(1);
            assertThat(events.getFirst().type()).isEqualTo(StreamEvent.EventType.ASSISTANT);
            assertThat(events.getFirst().content()).isEqualTo("Plain text output");
        }

        @Test
        @DisplayName("should handle invalid JSON gracefully")
        void shouldHandleInvalidJsonGracefully() {
            var lines = Stream.of("{invalid json}", "{\"type\": \"assistant\", \"content\": \"Valid\"}");

            var events = parser.parseStream(lines).toList();

            assertThat(events).hasSize(2);
            // Invalid JSON is treated as plain text
            assertThat(events.get(0).content()).isEqualTo("{invalid json}");
            assertThat(events.get(1).content()).isEqualTo("Valid");
        }
    }
}
