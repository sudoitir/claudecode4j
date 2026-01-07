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

public final class ClaudeCommandBuilder {

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

        if (options != null) {
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

            if (options.maxTokens() != null) {
                command.add("--max-turns");
                command.add(options.maxTokens().toString());
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

        return List.copyOf(command);
    }
}
