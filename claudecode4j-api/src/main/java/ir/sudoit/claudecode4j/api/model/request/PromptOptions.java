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

import java.time.Duration;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public record PromptOptions(
        @Nullable Duration timeout,
        OutputFormat outputFormat,
        @Nullable String model,
        boolean dangerouslySkipPermissions,
        boolean printMode,
        @Nullable Integer maxTokens) {
    public static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(5);

    public PromptOptions {
        Objects.requireNonNull(outputFormat, "outputFormat");
    }

    public static PromptOptions defaults() {
        return new PromptOptions(DEFAULT_TIMEOUT, OutputFormat.STREAM_JSON, null, false, false, null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private @Nullable Duration timeout = DEFAULT_TIMEOUT;
        private OutputFormat outputFormat = OutputFormat.STREAM_JSON;
        private @Nullable String model;
        private boolean dangerouslySkipPermissions;
        private boolean printMode;
        private @Nullable Integer maxTokens;

        private Builder() {}

        public Builder timeout(@Nullable Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder outputFormat(OutputFormat outputFormat) {
            this.outputFormat = outputFormat;
            return this;
        }

        public Builder model(@Nullable String model) {
            this.model = model;
            return this;
        }

        public Builder dangerouslySkipPermissions(boolean skip) {
            this.dangerouslySkipPermissions = skip;
            return this;
        }

        public Builder printMode(boolean print) {
            this.printMode = print;
            return this;
        }

        public Builder maxTokens(@Nullable Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public PromptOptions build() {
            return new PromptOptions(timeout, outputFormat, model, dangerouslySkipPermissions, printMode, maxTokens);
        }
    }
}
