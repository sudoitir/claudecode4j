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
package ir.sudoit.claudecode4j.api.config;

import java.nio.file.Path;
import java.time.Duration;
import org.jspecify.annotations.Nullable;

public interface ClaudeConfig {

    int DEFAULT_CONCURRENCY_LIMIT = 4;
    Duration DEFAULT_TIMEOUT = Duration.ofMinutes(5);

    @Nullable
    Path binaryPath();

    default int concurrencyLimit() {
        return DEFAULT_CONCURRENCY_LIMIT;
    }

    default Duration defaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    default boolean dangerouslySkipPermissions() {
        return false;
    }

    static ClaudeConfig defaults() {
        return new ClaudeConfig() {
            @Override
            public @Nullable Path binaryPath() {
                return null;
            }
        };
    }

    static Builder builder() {
        return new Builder();
    }

    final class Builder {
        private @Nullable Path binaryPath;
        private int concurrencyLimit = DEFAULT_CONCURRENCY_LIMIT;
        private Duration defaultTimeout = DEFAULT_TIMEOUT;
        private boolean dangerouslySkipPermissions = false;

        private Builder() {}

        public Builder binaryPath(@Nullable Path binaryPath) {
            this.binaryPath = binaryPath;
            return this;
        }

        public Builder concurrencyLimit(int concurrencyLimit) {
            this.concurrencyLimit = concurrencyLimit;
            return this;
        }

        public Builder defaultTimeout(Duration defaultTimeout) {
            this.defaultTimeout = defaultTimeout;
            return this;
        }

        public Builder dangerouslySkipPermissions(boolean skip) {
            this.dangerouslySkipPermissions = skip;
            return this;
        }

        public ClaudeConfig build() {
            final var bp = this.binaryPath;
            final var cl = this.concurrencyLimit;
            final var dt = this.defaultTimeout;
            final var dsp = this.dangerouslySkipPermissions;

            return new ClaudeConfig() {
                @Override
                public @Nullable Path binaryPath() {
                    return bp;
                }

                @Override
                public int concurrencyLimit() {
                    return cl;
                }

                @Override
                public Duration defaultTimeout() {
                    return dt;
                }

                @Override
                public boolean dangerouslySkipPermissions() {
                    return dsp;
                }
            };
        }
    }
}
