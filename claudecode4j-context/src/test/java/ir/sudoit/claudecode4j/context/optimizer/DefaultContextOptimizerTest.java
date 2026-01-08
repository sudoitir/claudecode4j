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

import static org.assertj.core.api.Assertions.assertThat;

import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.context.model.ContextBudget;
import ir.sudoit.claudecode4j.context.model.OptimizationResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DefaultContextOptimizerTest {

    private DefaultContextOptimizer optimizer;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        optimizer = new DefaultContextOptimizer();
    }

    @Test
    void shouldReturnEmptyResultForNoContextFiles() {
        Prompt prompt = Prompt.builder().text("test").build();
        ContextBudget budget = ContextBudget.withLimit(10000);

        OptimizationResult result = optimizer.optimize(prompt, budget);

        assertThat(result.includedFiles()).isEmpty();
        assertThat(result.excludedFiles()).isEmpty();
        assertThat(result.totalTokensUsed()).isZero();
    }

    @Test
    void shouldIncludeFilesWithinBudget() throws IOException {
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");
        Files.writeString(file1, "Short content");
        Files.writeString(file2, "Another short file");

        Prompt prompt = Prompt.builder()
                .text("test")
                .contextFiles(List.of(file1, file2))
                .build();
        ContextBudget budget = ContextBudget.withLimit(1000);

        OptimizationResult result = optimizer.optimize(prompt, budget);

        assertThat(result.includedFiles()).hasSize(2);
        assertThat(result.excludedFiles()).isEmpty();
        assertThat(result.totalTokensUsed()).isPositive();
    }

    @Test
    void shouldExcludeFilesExceedingBudget() throws IOException {
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");
        Files.writeString(file1, "Short");
        Files.writeString(file2, "A".repeat(10000)); // Large file

        Prompt prompt = Prompt.builder()
                .text("test")
                .contextFiles(List.of(file1, file2))
                .build();
        ContextBudget budget = ContextBudget.withLimit(50);

        OptimizationResult result = optimizer.optimize(prompt, budget);

        assertThat(result.hasExclusions()).isTrue();
        assertThat(result.excludedFiles()).isNotEmpty();
    }

    @Test
    void shouldEstimateTokensForContent() {
        int tokens = optimizer.estimateTokens("This is a test sentence for token estimation.");

        assertThat(tokens).isPositive();
        assertThat(tokens).isLessThan(20);
    }

    @Test
    void shouldHaveBuilderPattern() {
        DefaultContextOptimizer customOptimizer = DefaultContextOptimizer.builder()
                .rankingStrategy(RankingStrategy.recency())
                .warnOnExclusion(false)
                .build();

        assertThat(customOptimizer).isNotNull();
        assertThat(customOptimizer.getRankingStrategy()).isNotNull();
    }

    @Test
    void shouldSkipNonExistentFiles() {
        Path nonExistent = tempDir.resolve("does-not-exist.txt");

        Prompt prompt =
                Prompt.builder().text("test").contextFiles(List.of(nonExistent)).build();
        ContextBudget budget = ContextBudget.withLimit(1000);

        OptimizationResult result = optimizer.optimize(prompt, budget);

        assertThat(result.includedFiles()).isEmpty();
        assertThat(result.filesProcessed()).isZero();
    }

    @Test
    void shouldHaveDefaultPriority() {
        assertThat(optimizer.priority()).isEqualTo(10);
    }
}
