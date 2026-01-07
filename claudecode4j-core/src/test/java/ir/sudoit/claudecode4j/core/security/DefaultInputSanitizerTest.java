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
package ir.sudoit.claudecode4j.core.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ir.sudoit.claudecode4j.api.model.request.Prompt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("DefaultInputSanitizer")
class DefaultInputSanitizerTest {

    private DefaultInputSanitizer sanitizer;

    @BeforeEach
    void setUp() {
        sanitizer = new DefaultInputSanitizer();
    }

    @Nested
    @DisplayName("sanitize")
    class Sanitize {

        @Test
        @DisplayName("should accept valid prompt text")
        void shouldAcceptValidPromptText() {
            var prompt = Prompt.of("Hello, Claude!");
            var result = sanitizer.sanitize(prompt);
            assertThat(result).isEqualTo(prompt);
        }

        @Test
        @DisplayName("should accept prompt with system prompt")
        void shouldAcceptPromptWithSystemPrompt() {
            var prompt = Prompt.builder()
                    .text("What is the weather?")
                    .systemPrompt("You are a helpful assistant.")
                    .build();
            var result = sanitizer.sanitize(prompt);
            assertThat(result).isEqualTo(prompt);
        }

        @Test
        @DisplayName("should accept prompt with agent name")
        void shouldAcceptPromptWithAgentName() {
            var prompt =
                    Prompt.builder().text("Help me code").agentName("coder").build();
            var result = sanitizer.sanitize(prompt);
            assertThat(result).isEqualTo(prompt);
        }

        @Test
        @DisplayName("should reject prompt text exceeding max length")
        void shouldRejectPromptTextExceedingMaxLength() {
            var longText = "x".repeat(256 * 1024 + 1);
            var prompt = Prompt.of(longText);
            assertThatThrownBy(() -> sanitizer.sanitize(prompt))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("exceeds maximum length");
        }

        @Test
        @DisplayName("should reject prompt text with null character")
        void shouldRejectPromptTextWithNullCharacter() {
            var prompt = Prompt.of("Hello\0World");
            assertThatThrownBy(() -> sanitizer.sanitize(prompt))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("Null character detected");
        }

        @Test
        @DisplayName("should reject system prompt with null character")
        void shouldRejectSystemPromptWithNullCharacter() {
            var prompt = Prompt.builder()
                    .text("Valid text")
                    .systemPrompt("System\0Prompt")
                    .build();
            assertThatThrownBy(() -> sanitizer.sanitize(prompt))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("Null character detected");
        }

        @Test
        @DisplayName("should reject agent name with dangerous character")
        void shouldRejectAgentNameWithDangerousCharacter() {
            var prompt =
                    Prompt.builder().text("Valid text").agentName("agent`name").build();
            assertThatThrownBy(() -> sanitizer.sanitize(prompt))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("Dangerous character");
        }

        @Test
        @DisplayName("should reject agent name with command substitution")
        void shouldRejectAgentNameWithCommandSubstitution() {
            var prompt =
                    Prompt.builder().text("Valid text").agentName("$(whoami)").build();
            assertThatThrownBy(() -> sanitizer.sanitize(prompt))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("Command substitution pattern detected");
        }

        @Test
        @DisplayName("should reject agent name exceeding max length")
        void shouldRejectAgentNameExceedingMaxLength() {
            var prompt = Prompt.builder()
                    .text("Valid text")
                    .agentName("a".repeat(257))
                    .build();
            assertThatThrownBy(() -> sanitizer.sanitize(prompt))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("exceeds maximum length of 256");
        }

        @Test
        @DisplayName("should accept prompt text with Markdown code blocks")
        void shouldAcceptPromptTextWithMarkdownCodeBlocks() {
            var prompt = Prompt.of("Here is some code:\n```java\npublic class Test {}\n```");
            var result = sanitizer.sanitize(prompt);
            assertThat(result).isEqualTo(prompt);
        }

        @Test
        @DisplayName("should accept prompt text with inline code")
        void shouldAcceptPromptTextWithInlineCode() {
            var prompt = Prompt.of("Use the `println()` method to print output");
            var result = sanitizer.sanitize(prompt);
            assertThat(result).isEqualTo(prompt);
        }

        @Test
        @DisplayName("should accept prompt text with multiple backtick sequences")
        void shouldAcceptPromptTextWithMultipleBacktickSequences() {
            var prompt = Prompt.of("Compare `foo()` with `bar()` and ```\nmultiline\ncode\n```");
            var result = sanitizer.sanitize(prompt);
            assertThat(result).isEqualTo(prompt);
        }

        @Test
        @DisplayName("should accept system prompt with Markdown code blocks")
        void shouldAcceptSystemPromptWithMarkdownCodeBlocks() {
            var prompt = Prompt.builder()
                    .text("Help me with code")
                    .systemPrompt("Format responses with ```code``` blocks when appropriate")
                    .build();
            var result = sanitizer.sanitize(prompt);
            assertThat(result).isEqualTo(prompt);
        }

        @Test
        @DisplayName("should reject agent name with backtick substitution pattern")
        void shouldRejectAgentNameWithBacktickSubstitutionPattern() {
            var prompt =
                    Prompt.builder().text("Valid text").agentName("`rm -rf /`").build();
            assertThatThrownBy(() -> sanitizer.sanitize(prompt))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("Dangerous character");
        }
    }

    @Nested
    @DisplayName("isValidArgument")
    class IsValidArgument {

        @Test
        @DisplayName("should return true for valid argument")
        void shouldReturnTrueForValidArgument() {
            assertThat(sanitizer.isValidArgument("valid-argument")).isTrue();
        }

        @Test
        @DisplayName("should return false for null argument")
        void shouldReturnFalseForNullArgument() {
            assertThat(sanitizer.isValidArgument(null)).isFalse();
        }

        @Test
        @DisplayName("should return false for argument exceeding max length")
        void shouldReturnFalseForArgumentExceedingMaxLength() {
            var longArg = "x".repeat(256 * 1024 + 1);
            assertThat(sanitizer.isValidArgument(longArg)).isFalse();
        }

        @Test
        @DisplayName("should return false for argument with backtick")
        void shouldReturnFalseForArgumentWithBacktick() {
            assertThat(sanitizer.isValidArgument("hello`world")).isFalse();
        }

        @Test
        @DisplayName("should return false for argument with null character")
        void shouldReturnFalseForArgumentWithNullCharacter() {
            assertThat(sanitizer.isValidArgument("hello\0world")).isFalse();
        }

        @Test
        @DisplayName("should return false for argument with command substitution")
        void shouldReturnFalseForArgumentWithCommandSubstitution() {
            assertThat(sanitizer.isValidArgument("$(rm -rf /)")).isFalse();
        }

        @Test
        @DisplayName("should return false for argument with backtick substitution")
        void shouldReturnFalseForArgumentWithBacktickSubstitution() {
            assertThat(sanitizer.isValidArgument("`whoami`")).isFalse();
        }

        @Test
        @DisplayName("should return true for safe special characters")
        void shouldReturnTrueForSafeSpecialCharacters() {
            assertThat(sanitizer.isValidArgument("hello-world_test.txt")).isTrue();
            assertThat(sanitizer.isValidArgument("path/to/file")).isTrue();
            assertThat(sanitizer.isValidArgument("email@example.com")).isTrue();
        }
    }
}
