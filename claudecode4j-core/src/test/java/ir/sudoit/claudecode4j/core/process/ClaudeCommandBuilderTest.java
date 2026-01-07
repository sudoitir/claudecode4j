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
package ir.sudoit.claudecode4j.core.process;

import static org.assertj.core.api.Assertions.assertThat;

import ir.sudoit.claudecode4j.api.model.request.OutputFormat;
import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.api.model.request.PromptOptions;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ClaudeCommandBuilder")
class ClaudeCommandBuilderTest {

    private static final Path BINARY_PATH = Path.of("/usr/local/bin/claude");

    private ClaudeCommandBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new ClaudeCommandBuilder(BINARY_PATH);
    }

    @Nested
    @DisplayName("build")
    class Build {

        @Test
        @DisplayName("should build command with binary path only")
        void shouldBuildCommandWithBinaryPathOnly() {
            var command = builder.build();

            assertThat(command).containsExactly("/usr/local/bin/claude");
        }

        @Test
        @DisplayName("should build command with prompt text")
        void shouldBuildCommandWithPromptText() {
            var prompt = Prompt.of("Hello, Claude!");
            var command = builder.prompt(prompt).build();

            assertThat(command).containsExactly("/usr/local/bin/claude", "Hello, Claude!");
        }

        @Test
        @DisplayName("should build command with system prompt")
        void shouldBuildCommandWithSystemPrompt() {
            var prompt = Prompt.builder()
                    .text("What is the weather?")
                    .systemPrompt("You are a weather assistant.")
                    .build();
            var command = builder.prompt(prompt).build();

            assertThat(command)
                    .containsExactly(
                            "/usr/local/bin/claude",
                            "--system-prompt",
                            "You are a weather assistant.",
                            "What is the weather?");
        }

        @Test
        @DisplayName("should build command with agent name")
        void shouldBuildCommandWithAgentName() {
            var prompt = Prompt.builder().text("Help me").agentName("coder").build();
            var command = builder.prompt(prompt).build();

            assertThat(command).containsExactly("/usr/local/bin/claude", "--agent", "coder", "Help me");
        }

        @Test
        @DisplayName("should build command with context files")
        void shouldBuildCommandWithContextFiles() {
            var prompt = Prompt.builder()
                    .text("Analyze this")
                    .contextFiles(List.of(Path.of("/path/to/file1"), Path.of("/path/to/file2")))
                    .build();
            var command = builder.prompt(prompt).build();

            assertThat(command)
                    .containsExactly(
                            "/usr/local/bin/claude",
                            "--add-dir",
                            "/path/to/file1",
                            "--add-dir",
                            "/path/to/file2",
                            "Analyze this");
        }

        @Test
        @DisplayName("should build command with print mode")
        void shouldBuildCommandWithPrintMode() {
            var options = PromptOptions.builder().printMode(true).build();
            var command = builder.options(options).build();

            assertThat(command).contains("--print");
        }

        @Test
        @DisplayName("should build command with dangerously skip permissions")
        void shouldBuildCommandWithDangerouslySkipPermissions() {
            var options =
                    PromptOptions.builder().dangerouslySkipPermissions(true).build();
            var command = builder.options(options).build();

            assertThat(command).contains("--dangerously-skip-permissions");
        }

        @Test
        @DisplayName("should build command with output format")
        void shouldBuildCommandWithOutputFormat() {
            var options = PromptOptions.builder()
                    .outputFormat(OutputFormat.STREAM_JSON)
                    .build();
            var command = builder.options(options).build();

            assertThat(command).containsSequence("--output-format", "stream-json");
        }

        @Test
        @DisplayName("should build command with model")
        void shouldBuildCommandWithModel() {
            var options = PromptOptions.builder().model("claude-3-opus").build();
            var command = builder.options(options).build();

            assertThat(command).containsSequence("--model", "claude-3-opus");
        }

        @Test
        @DisplayName("should build command with max turns")
        void shouldBuildCommandWithMaxTurns() {
            var options = PromptOptions.builder().maxTurns(5).build();
            var command = builder.options(options).build();

            assertThat(command).containsSequence("--max-turns", "5");
        }

        @Test
        @DisplayName("should build command with allowed tools")
        void shouldBuildCommandWithAllowedTools() {
            var options = PromptOptions.builder()
                    .allowedTools("Read", "Write", "Edit")
                    .build();
            var command = builder.options(options).build();

            assertThat(command)
                    .containsSequence("--allowedTools", "Read")
                    .containsSequence("--allowedTools", "Write")
                    .containsSequence("--allowedTools", "Edit");
        }

        @Test
        @DisplayName("should build command with disallowed tools")
        void shouldBuildCommandWithDisallowedTools() {
            var options = PromptOptions.builder().disallowedTools("Bash").build();
            var command = builder.options(options).build();

            assertThat(command).containsSequence("--disallowedTools", "Bash");
        }

        @Test
        @DisplayName("should build complete command with all options")
        void shouldBuildCompleteCommandWithAllOptions() {
            var prompt = Prompt.builder()
                    .text("Help me refactor")
                    .systemPrompt("You are a code expert")
                    .agentName("refactor-agent")
                    .contextFiles(List.of(Path.of("/src/main")))
                    .build();

            var options = PromptOptions.builder()
                    .printMode(true)
                    .dangerouslySkipPermissions(true)
                    .outputFormat(OutputFormat.JSON)
                    .model("claude-3-sonnet")
                    .maxTurns(10)
                    .allowedTools("Read", "Edit")
                    .disallowedTools("Bash")
                    .build();

            var command = builder.prompt(prompt).options(options).build();

            assertThat(command)
                    .startsWith("/usr/local/bin/claude")
                    .contains("--print")
                    .contains("--dangerously-skip-permissions")
                    .containsSequence("--output-format", "json")
                    .containsSequence("--model", "claude-3-sonnet")
                    .containsSequence("--max-turns", "10")
                    .containsSequence("--allowedTools", "Read")
                    .containsSequence("--allowedTools", "Edit")
                    .containsSequence("--disallowedTools", "Bash")
                    .containsSequence("--agent", "refactor-agent")
                    .containsSequence("--system-prompt", "You are a code expert")
                    .containsSequence("--add-dir", "/src/main")
                    .endsWith("Help me refactor");
        }

        @Test
        @DisplayName("should return immutable command list")
        void shouldReturnImmutableCommandList() {
            var command = builder.prompt(Prompt.of("test")).build();

            assertThat(command).isUnmodifiable();
        }
    }

    @Nested
    @DisplayName("buildWithStdin")
    class BuildWithStdin {

        @Test
        @DisplayName("should build command with stdin input")
        void shouldBuildCommandWithStdinInput() {
            var prompt = Prompt.of("Hello from stdin!");
            var result = builder.prompt(prompt).buildWithStdin();

            assertThat(result.command())
                    .contains("/usr/local/bin/claude")
                    .contains("-p")
                    .contains("-")
                    .doesNotContain("Hello from stdin!");

            assertThat(result.stdinInput()).isEqualTo("Hello from stdin!");
        }

        @Test
        @DisplayName("should build command with options via stdin")
        void shouldBuildCommandWithOptionsViaStdin() {
            var prompt = Prompt.builder()
                    .text("Process this")
                    .systemPrompt("Be helpful")
                    .build();

            var options = PromptOptions.builder()
                    .outputFormat(OutputFormat.STREAM_JSON)
                    .dangerouslySkipPermissions(true)
                    .build();

            var result = builder.prompt(prompt).options(options).buildWithStdin();

            assertThat(result.command())
                    .contains("--dangerously-skip-permissions")
                    .containsSequence("--output-format", "stream-json")
                    .containsSequence("--system-prompt", "Be helpful")
                    .containsSequence("-p", "-");

            assertThat(result.stdinInput()).isEqualTo("Process this");
        }

        @Test
        @DisplayName("should return null stdin when no prompt")
        void shouldReturnNullStdinWhenNoPrompt() {
            var result = builder.buildWithStdin();

            assertThat(result.command()).containsExactly("/usr/local/bin/claude");
            assertThat(result.stdinInput()).isNull();
        }

        @Test
        @DisplayName("should return immutable command list from stdin build")
        void shouldReturnImmutableCommandListFromStdinBuild() {
            var result = builder.prompt(Prompt.of("test")).buildWithStdin();

            assertThat(result.command()).isUnmodifiable();
        }
    }
}
