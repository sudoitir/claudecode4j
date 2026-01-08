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
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a token budget for context files.
 *
 * @param totalTokens total tokens available for context
 * @param usedTokens tokens already allocated
 * @param fileAllocations token allocations per file
 */
public record ContextBudget(int totalTokens, int usedTokens, Map<Path, Integer> fileAllocations) {

    public ContextBudget {
        fileAllocations = Map.copyOf(fileAllocations);
    }

    /**
     * Creates a budget for the given model with default ratio.
     *
     * @param model the model identifier
     * @return a new context budget
     */
    public static ContextBudget forModel(String model) {
        return forModel(model, 0.8);
    }

    /**
     * Creates a budget for the given model with custom ratio.
     *
     * @param model the model identifier
     * @param ratio the ratio of available context to use (0.0 to 1.0)
     * @return a new context budget
     */
    public static ContextBudget forModel(String model, double ratio) {
        var limits = ModelTokenLimits.forModel(model);
        int available = limits.availableForContext(ratio);
        return new ContextBudget(available, 0, Map.of());
    }

    /**
     * Creates a budget with a fixed token limit.
     *
     * @param totalTokens the total tokens available
     * @return a new context budget
     */
    public static ContextBudget withLimit(int totalTokens) {
        return new ContextBudget(totalTokens, 0, Map.of());
    }

    /**
     * Returns the remaining tokens available.
     *
     * @return available tokens
     */
    public int availableTokens() {
        return totalTokens - usedTokens;
    }

    /**
     * Checks if the specified number of tokens can fit in the budget.
     *
     * @param tokens the tokens to check
     * @return true if tokens fit within available budget
     */
    public boolean canFit(int tokens) {
        return tokens <= availableTokens();
    }

    /**
     * Allocates tokens for a file and returns a new budget.
     *
     * @param file the file to allocate tokens for
     * @param tokens the number of tokens
     * @return a new budget with the allocation
     * @throws IllegalArgumentException if tokens exceed available budget
     */
    public ContextBudget allocate(Path file, int tokens) {
        if (!canFit(tokens)) {
            throw new IllegalArgumentException(
                    "Cannot allocate %d tokens, only %d available".formatted(tokens, availableTokens()));
        }
        var newAllocations = new HashMap<>(fileAllocations);
        newAllocations.put(file, tokens);
        return new ContextBudget(totalTokens, usedTokens + tokens, newAllocations);
    }

    /**
     * Returns the allocation for a specific file.
     *
     * @param file the file
     * @return the allocated tokens, or 0 if not allocated
     */
    public int getAllocation(Path file) {
        return fileAllocations.getOrDefault(file, 0);
    }

    /**
     * Returns the utilization ratio (0.0 to 1.0).
     *
     * @return the utilization ratio
     */
    public double utilization() {
        return totalTokens > 0 ? (double) usedTokens / totalTokens : 0.0;
    }
}
