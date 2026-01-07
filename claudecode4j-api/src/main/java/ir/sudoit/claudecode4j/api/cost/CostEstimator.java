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
package ir.sudoit.claudecode4j.api.cost;

import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.api.model.request.PromptOptions;
import ir.sudoit.claudecode4j.api.model.response.ClaudeResponse;

/**
 * SPI for estimating and calculating costs for Claude API calls.
 *
 * <p>Implementations can provide different pricing models based on the model used, region, or custom pricing
 * agreements.
 */
public interface CostEstimator {

    /**
     * Estimates the cost before making an API call.
     *
     * <p>Uses heuristics to estimate token count from the prompt text.
     *
     * @param prompt the prompt to estimate
     * @param options the prompt options (may affect pricing based on model)
     * @return an estimated cost
     */
    CostEstimate estimate(Prompt prompt, PromptOptions options);

    /**
     * Calculates the actual cost from a response.
     *
     * <p>Uses actual token counts from the response if available.
     *
     * @param response the response containing actual token usage
     * @param options the prompt options used
     * @return the actual cost
     */
    CostEstimate calculate(ClaudeResponse response, PromptOptions options);

    /**
     * Estimates token count for a given text.
     *
     * <p>Uses a simple heuristic: approximately 4 characters per token.
     *
     * @param text the text to estimate
     * @return estimated token count
     */
    default long estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        // Simple heuristic: ~4 characters per token
        return (text.length() + 3) / 4;
    }

    /** Priority for SPI selection (higher = preferred). */
    default int priority() {
        return 0;
    }
}
