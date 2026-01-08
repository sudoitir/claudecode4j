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
package ir.sudoit.claudecode4j.context.optimizer;

import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.context.model.ContextBudget;
import ir.sudoit.claudecode4j.context.model.OptimizationResult;
import ir.sudoit.claudecode4j.context.spi.ContextOptimizer;
import ir.sudoit.claudecode4j.context.spi.TokenCounter;
import ir.sudoit.claudecode4j.context.tokenizer.JTokkitTokenCounter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of ContextOptimizer.
 *
 * <p>This optimizer processes files in priority order (based on the ranking strategy) and includes as many files as
 * possible within the token budget. Files that don't fit are excluded.
 */
public final class DefaultContextOptimizer implements ContextOptimizer {

    private final TokenCounter tokenCounter;
    private final RankingStrategy rankingStrategy;
    private final boolean warnOnExclusion;

    /** Creates a new optimizer with default settings. */
    public DefaultContextOptimizer() {
        this(new JTokkitTokenCounter(), RankingStrategy.recency(), true);
    }

    /**
     * Creates a new optimizer with the specified token counter.
     *
     * @param tokenCounter the token counter to use
     */
    public DefaultContextOptimizer(TokenCounter tokenCounter) {
        this(tokenCounter, RankingStrategy.recency(), true);
    }

    /**
     * Creates a new optimizer with the specified settings.
     *
     * @param tokenCounter the token counter to use
     * @param rankingStrategy the strategy for ranking files
     * @param warnOnExclusion whether to generate warnings when files are excluded
     */
    public DefaultContextOptimizer(
            TokenCounter tokenCounter, RankingStrategy rankingStrategy, boolean warnOnExclusion) {
        this.tokenCounter = tokenCounter;
        this.rankingStrategy = rankingStrategy;
        this.warnOnExclusion = warnOnExclusion;
    }

    @Override
    public OptimizationResult optimize(Prompt prompt, ContextBudget budget) {
        List<Path> contextFiles = prompt.contextFiles();
        if (contextFiles == null || contextFiles.isEmpty()) {
            return OptimizationResult.empty();
        }

        // Filter to existing regular files
        List<Path> paths = contextFiles.stream()
                .filter(Files::exists)
                .filter(Files::isRegularFile)
                .toList();

        if (paths.isEmpty()) {
            return OptimizationResult.empty();
        }

        // Rank files by priority
        List<Path> rankedPaths = rankingStrategy.rank(paths);

        // Process files and track what fits
        List<Path> included = new ArrayList<>();
        List<Path> excluded = new ArrayList<>();
        int totalTokens = 0;
        int remainingBudget = budget.totalTokens();

        for (Path path : rankedPaths) {
            try {
                int fileTokens = tokenCounter.count(path);

                if (fileTokens <= remainingBudget) {
                    included.add(path);
                    totalTokens += fileTokens;
                    remainingBudget -= fileTokens;
                } else {
                    excluded.add(path);
                }
            } catch (IOException e) {
                // Skip files that can't be read
                excluded.add(path);
            }
        }

        // Build result
        String warning = null;
        if (warnOnExclusion && !excluded.isEmpty()) {
            warning = String.format(
                    "Excluded %d file(s) from context due to token budget constraints. "
                            + "Total budget: %d tokens, used: %d tokens. "
                            + "Excluded files: %s",
                    excluded.size(),
                    budget.totalTokens(),
                    totalTokens,
                    excluded.stream().map(Path::getFileName).map(Path::toString).toList());
        }

        return OptimizationResult.builder()
                .includedFiles(included)
                .excludedFiles(excluded)
                .truncatedFiles(List.of())
                .totalTokensUsed(totalTokens)
                .filesProcessed(paths.size())
                .warning(warning)
                .build();
    }

    @Override
    public int estimateTokens(String content) {
        return tokenCounter.count(content);
    }

    @Override
    public int priority() {
        return 10;
    }

    /**
     * Estimates the total tokens for a list of files.
     *
     * @param files the files to estimate
     * @return the total estimated tokens
     */
    public int estimateTotalTokens(List<Path> files) {
        return files.stream()
                .filter(Files::exists)
                .filter(Files::isRegularFile)
                .mapToInt(path -> {
                    try {
                        return tokenCounter.count(path);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .sum();
    }

    /**
     * Returns the ranking strategy used by this optimizer.
     *
     * @return the ranking strategy
     */
    public RankingStrategy getRankingStrategy() {
        return rankingStrategy;
    }

    /**
     * Creates a builder for constructing a DefaultContextOptimizer.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder for DefaultContextOptimizer. */
    public static final class Builder {
        private TokenCounter tokenCounter = new JTokkitTokenCounter();
        private RankingStrategy rankingStrategy = RankingStrategy.recency();
        private boolean warnOnExclusion = true;

        private Builder() {}

        public Builder tokenCounter(TokenCounter tokenCounter) {
            this.tokenCounter = tokenCounter;
            return this;
        }

        public Builder rankingStrategy(RankingStrategy strategy) {
            this.rankingStrategy = strategy;
            return this;
        }

        public Builder warnOnExclusion(boolean warn) {
            this.warnOnExclusion = warn;
            return this;
        }

        public DefaultContextOptimizer build() {
            return new DefaultContextOptimizer(tokenCounter, rankingStrategy, warnOnExclusion);
        }
    }
}
