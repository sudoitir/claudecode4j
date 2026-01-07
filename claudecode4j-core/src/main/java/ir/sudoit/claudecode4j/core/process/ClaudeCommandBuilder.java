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

import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.api.model.request.PromptOptions;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

public final class ClaudeCommandBuilder {

    /**
     * Represents a CLI command with optional stdin input.
     *
     * <p>Used to pass the prompt via stdin instead of command-line arguments, avoiding OS command length limits and
     * potential escaping issues.
     *
     * @param command the CLI arguments
     * @param stdinInput the prompt text to send via stdin, or null if using positional arg
     */
    public record CommandWithStdin(
            List<String> command, @Nullable String stdinInput) {}

    private static final System.Logger log = System.getLogger(ClaudeCommandBuilder.class.getName());

    private final Path binaryPath;
    private Prompt prompt;
    private PromptOptions options;

    public ClaudeCommandBuilder(Path binaryPath) {
        this.binaryPath = binaryPath;
    }

    public ClaudeCommandBuilder prompt(Prompt prompt) {
        this.prompt = prompt;
        return this;
    }

    public ClaudeCommandBuilder options(PromptOptions options) {
        this.options = options;
        return this;
    }

    public List<String> build() {
        var command = new ArrayList<String>();
        command.add(binaryPath.toString());
        log.log(System.Logger.Level.DEBUG, "Building command, binaryPath={0}", binaryPath);

        if (options != null) {
            log.log(
                    System.Logger.Level.DEBUG,
                    "Options: printMode={0}, dangerouslySkipPermissions={1}, outputFormat={2}, model={3}, maxTurns={4}",
                    options.printMode(),
                    options.dangerouslySkipPermissions(),
                    options.outputFormat(),
                    options.model(),
                    options.maxTurns());

            if (options.printMode()) {
                command.add("--print");
            }

            if (options.dangerouslySkipPermissions()) {
                command.add("--dangerously-skip-permissions");
            }

            if (options.outputFormat() != null) {
                command.add("--output-format");
                command.add(options.outputFormat().cliValue());
            }

            if (options.model() != null) {
                command.add("--model");
                command.add(options.model());
            }

            if (options.maxTurns() != null) {
                command.add("--max-turns");
                command.add(options.maxTurns().toString());
            }

            // Tool filtering options
            for (var tool : options.allowedTools()) {
                command.add("--allowedTools");
                command.add(tool);
            }
            for (var tool : options.disallowedTools()) {
                command.add("--disallowedTools");
                command.add(tool);
            }
        }

        if (prompt != null) {
            if (prompt.agentName() != null) {
                command.add("--agent");
                command.add(prompt.agentName());
            }

            if (prompt.systemPrompt() != null) {
                command.add("--system-prompt");
                command.add(prompt.systemPrompt());
            }

            for (var contextFile : prompt.contextFiles()) {
                command.add("--add-dir");
                command.add(contextFile.toString());
            }

            // Prompt is positional argument (must be last)
            command.add(prompt.text());
        }

        log.log(System.Logger.Level.DEBUG, "Final command: {0}", String.join(" ", command));
        return List.copyOf(command);
    }

    /**
     * Builds the command with prompt passed via stdin.
     *
     * <p>This method passes the prompt text via stdin instead of as a positional argument, avoiding OS command length
     * limits (ARG_MAX) and potential shell escaping issues.
     *
     * @return a record containing the command and the stdin input
     */
    public CommandWithStdin buildWithStdin() {
        var command = new ArrayList<String>();
        command.add(binaryPath.toString());
        log.log(System.Logger.Level.DEBUG, "Building command with stdin, binaryPath={0}", binaryPath);

        if (options != null) {
            log.log(
                    System.Logger.Level.DEBUG,
                    "Options: printMode={0}, dangerouslySkipPermissions={1}, outputFormat={2}, model={3}, maxTurns={4}",
                    options.printMode(),
                    options.dangerouslySkipPermissions(),
                    options.outputFormat(),
                    options.model(),
                    options.maxTurns());

            if (options.printMode()) {
                command.add("--print");
            }

            if (options.dangerouslySkipPermissions()) {
                command.add("--dangerously-skip-permissions");
            }

            if (options.outputFormat() != null) {
                command.add("--output-format");
                command.add(options.outputFormat().cliValue());
            }

            if (options.model() != null) {
                command.add("--model");
                command.add(options.model());
            }

            if (options.maxTurns() != null) {
                command.add("--max-turns");
                command.add(options.maxTurns().toString());
            }

            // Tool filtering options
            for (var tool : options.allowedTools()) {
                command.add("--allowedTools");
                command.add(tool);
            }
            for (var tool : options.disallowedTools()) {
                command.add("--disallowedTools");
                command.add(tool);
            }
        }

        String stdinInput = null;
        if (prompt != null) {
            if (prompt.agentName() != null) {
                command.add("--agent");
                command.add(prompt.agentName());
            }

            if (prompt.systemPrompt() != null) {
                command.add("--system-prompt");
                command.add(prompt.systemPrompt());
            }

            for (var contextFile : prompt.contextFiles()) {
                command.add("--add-dir");
                command.add(contextFile.toString());
            }

            // Pass prompt via stdin using "-" to read from stdin
            command.add("-p");
            command.add("-");
            stdinInput = prompt.text();
        }

        log.log(
                System.Logger.Level.DEBUG,
                "Final command (stdin mode): {0}, stdinLength={1}",
                String.join(" ", command),
                stdinInput != null ? stdinInput.length() : 0);
        return new CommandWithStdin(List.copyOf(command), stdinInput);
    }
}
