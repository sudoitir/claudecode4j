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
package ir.sudoit.claudecode4j.rest.dto;

import ir.sudoit.claudecode4j.api.model.request.OutputFormat;
import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.api.model.request.PromptOptions;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record PromptRequest(
        @NotBlank String text,
        @Nullable String systemPrompt,
        @Nullable List<String> contextFiles,
        @Nullable String workingDirectory,
        @Nullable String agentName,
        @Nullable Long timeoutSeconds,
        @Nullable String outputFormat,
        @Nullable String model,
        @Nullable Boolean dangerouslySkipPermissions,
        @Nullable Boolean printMode,
        @Nullable Integer maxTokens) {
    public Prompt toPrompt() {
        var builder = Prompt.builder().text(text);
        if (systemPrompt != null) builder.systemPrompt(systemPrompt);
        if (contextFiles != null)
            builder.contextFiles(
                    contextFiles.stream().map(java.nio.file.Path::of).toList());
        if (workingDirectory != null) builder.workingDirectory(java.nio.file.Path.of(workingDirectory));
        if (agentName != null) builder.agentName(agentName);
        return builder.build();
    }

    public PromptOptions toOptions() {
        var builder = PromptOptions.builder();
        if (timeoutSeconds != null) builder.timeout(Duration.ofSeconds(timeoutSeconds));
        if (outputFormat != null)
            builder.outputFormat(OutputFormat.valueOf(outputFormat.toUpperCase().replace("-", "_")));
        if (model != null) builder.model(model);
        if (dangerouslySkipPermissions != null) builder.dangerouslySkipPermissions(dangerouslySkipPermissions);
        if (printMode != null) builder.printMode(printMode);
        if (maxTokens != null) builder.maxTokens(maxTokens);
        return builder.build();
    }
}
