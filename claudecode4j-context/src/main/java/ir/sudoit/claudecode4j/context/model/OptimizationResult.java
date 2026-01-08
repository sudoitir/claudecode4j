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
package ir.sudoit.claudecode4j.context.model;

import java.nio.file.Path;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Result of context optimization.
 *
 * @param includedFiles files that will be included in context
 * @param excludedFiles files that were excluded due to budget constraints
 * @param truncatedFiles files that were truncated to fit the budget
 * @param totalTokensUsed total tokens consumed by included files
 * @param filesProcessed number of files processed
 * @param warning optional warning message about optimization decisions
 */
public record OptimizationResult(
        List<Path> includedFiles,
        List<Path> excludedFiles,
        List<Path> truncatedFiles,
        int totalTokensUsed,
        int filesProcessed,
        @Nullable String warning) {

    public OptimizationResult {
        includedFiles = List.copyOf(includedFiles);
        excludedFiles = List.copyOf(excludedFiles);
        truncatedFiles = List.copyOf(truncatedFiles);
    }

    /**
     * Creates a successful result with all files included.
     *
     * @param files the files included
     * @param tokensUsed the tokens used
     * @return the optimization result
     */
    public static OptimizationResult allIncluded(List<Path> files, int tokensUsed) {
        return new OptimizationResult(files, List.of(), List.of(), tokensUsed, files.size(), null);
    }

    /**
     * Creates an empty result (no files to process).
     *
     * @return the optimization result
     */
    public static OptimizationResult empty() {
        return new OptimizationResult(List.of(), List.of(), List.of(), 0, 0, null);
    }

    /**
     * Returns true if any files were excluded.
     *
     * @return true if files were excluded
     */
    public boolean hasExclusions() {
        return !excludedFiles.isEmpty();
    }

    /**
     * Returns true if any files were truncated.
     *
     * @return true if files were truncated
     */
    public boolean hasTruncations() {
        return !truncatedFiles.isEmpty();
    }

    /**
     * Returns true if optimization made changes (exclusions or truncations).
     *
     * @return true if optimization was applied
     */
    public boolean wasOptimized() {
        return hasExclusions() || hasTruncations();
    }

    /**
     * Creates a builder for constructing optimization results.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder for OptimizationResult. */
    public static final class Builder {
        private List<Path> includedFiles = List.of();
        private List<Path> excludedFiles = List.of();
        private List<Path> truncatedFiles = List.of();
        private int totalTokensUsed;
        private int filesProcessed;
        private @Nullable String warning;

        private Builder() {}

        public Builder includedFiles(List<Path> files) {
            this.includedFiles = files;
            return this;
        }

        public Builder excludedFiles(List<Path> files) {
            this.excludedFiles = files;
            return this;
        }

        public Builder truncatedFiles(List<Path> files) {
            this.truncatedFiles = files;
            return this;
        }

        public Builder totalTokensUsed(int tokens) {
            this.totalTokensUsed = tokens;
            return this;
        }

        public Builder filesProcessed(int count) {
            this.filesProcessed = count;
            return this;
        }

        public Builder warning(String warning) {
            this.warning = warning;
            return this;
        }

        public OptimizationResult build() {
            return new OptimizationResult(
                    includedFiles, excludedFiles, truncatedFiles, totalTokensUsed, filesProcessed, warning);
        }
    }
}
