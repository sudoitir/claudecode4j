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
package ir.sudoit.claudecode4j.context.spi;

import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.context.model.ContextBudget;
import ir.sudoit.claudecode4j.context.model.OptimizationResult;

/**
 * SPI for optimizing prompt context to fit within token limits.
 *
 * <p>Implementations should analyze the prompt's context files and optimize them to fit within the specified token
 * budget, using strategies like file ranking, truncation, or exclusion.
 */
public interface ContextOptimizer {

    /**
     * Optimizes the prompt's context files to fit within the token budget.
     *
     * @param prompt the prompt with context files to optimize
     * @param budget the token budget constraints
     * @return the optimization result with included/excluded/truncated files
     */
    OptimizationResult optimize(Prompt prompt, ContextBudget budget);

    /**
     * Estimates the token count for a piece of text.
     *
     * @param content the text content
     * @return the estimated token count
     */
    int estimateTokens(String content);

    /**
     * Returns the priority of this implementation for SPI selection. Higher priority implementations are preferred.
     *
     * @return the priority (default: 0)
     */
    default int priority() {
        return 0;
    }
}
