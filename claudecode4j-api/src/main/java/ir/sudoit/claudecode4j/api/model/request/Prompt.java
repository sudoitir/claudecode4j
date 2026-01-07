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
package ir.sudoit.claudecode4j.api.model.request;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public record Prompt(
        String text,
        @Nullable String systemPrompt,
        List<Path> contextFiles,
        @Nullable Path workingDirectory,
        @Nullable String agentName) {
    public Prompt {
        Objects.requireNonNull(text, "text");
        if (text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
        contextFiles = contextFiles == null ? List.of() : List.copyOf(contextFiles);
    }

    public static Prompt of(String text) {
        return new Prompt(text, null, List.of(), null, null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private @Nullable String text;
        private @Nullable String systemPrompt;
        private List<Path> contextFiles = List.of();
        private @Nullable Path workingDirectory;
        private @Nullable String agentName;

        private Builder() {}

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder systemPrompt(@Nullable String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        public Builder contextFiles(List<Path> contextFiles) {
            this.contextFiles = contextFiles;
            return this;
        }

        public Builder workingDirectory(@Nullable Path workingDirectory) {
            this.workingDirectory = workingDirectory;
            return this;
        }

        public Builder agentName(@Nullable String agentName) {
            this.agentName = agentName;
            return this;
        }

        public Prompt build() {
            if (text == null) {
                throw new IllegalStateException("text is required");
            }
            return new Prompt(text, systemPrompt, contextFiles, workingDirectory, agentName);
        }
    }
}
