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
import ir.sudoit.claudecode4j.api.model.response.TextResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Default cost estimator using standard Anthropic pricing.
 *
 * <p>Pricing as of 2025 (may need updates): - Claude 3.5 Sonnet: $3/1M input, $15/1M output - Claude 3.5 Haiku:
 * $0.25/1M input, $1.25/1M output - Claude 3 Opus: $15/1M input, $75/1M output
 */
public final class DefaultCostEstimator implements CostEstimator {

    private static final BigDecimal MILLION = new BigDecimal("1000000");

    // Default pricing (Claude 3.5 Sonnet)
    private static final BigDecimal DEFAULT_INPUT_COST_PER_MILLION = new BigDecimal("3.00");
    private static final BigDecimal DEFAULT_OUTPUT_COST_PER_MILLION = new BigDecimal("15.00");

    // Model-specific pricing
    private static final Map<String, BigDecimal[]> MODEL_PRICING = Map.of(
            "claude-3-5-sonnet", new BigDecimal[] {new BigDecimal("3.00"), new BigDecimal("15.00")},
            "claude-3-5-haiku", new BigDecimal[] {new BigDecimal("0.25"), new BigDecimal("1.25")},
            "claude-3-opus", new BigDecimal[] {new BigDecimal("15.00"), new BigDecimal("75.00")},
            "claude-3-sonnet", new BigDecimal[] {new BigDecimal("3.00"), new BigDecimal("15.00")},
            "claude-3-haiku", new BigDecimal[] {new BigDecimal("0.25"), new BigDecimal("1.25")});

    private final BigDecimal inputCostPerMillion;
    private final BigDecimal outputCostPerMillion;

    public DefaultCostEstimator() {
        this(DEFAULT_INPUT_COST_PER_MILLION, DEFAULT_OUTPUT_COST_PER_MILLION);
    }

    public DefaultCostEstimator(BigDecimal inputCostPerMillion, BigDecimal outputCostPerMillion) {
        this.inputCostPerMillion = inputCostPerMillion;
        this.outputCostPerMillion = outputCostPerMillion;
    }

    /** Creates an estimator for a specific model. */
    public static DefaultCostEstimator forModel(String model) {
        var pricing = MODEL_PRICING.get(normalizeModelName(model));
        if (pricing != null) {
            return new DefaultCostEstimator(pricing[0], pricing[1]);
        }
        return new DefaultCostEstimator();
    }

    @Override
    public CostEstimate estimate(Prompt prompt, PromptOptions options) {
        var inputTokens = estimateTokens(prompt.text());
        if (prompt.systemPrompt() != null) {
            inputTokens += estimateTokens(prompt.systemPrompt());
        }

        // Estimate output as roughly equal to input for a typical response
        var estimatedOutputTokens = Math.max(inputTokens / 2, 100);

        var pricing = getPricing(options.model());
        var cost = calculateCost(inputTokens, estimatedOutputTokens, pricing[0], pricing[1]);

        return new CostEstimate(inputTokens, estimatedOutputTokens, cost, "USD", options.model());
    }

    @Override
    public CostEstimate calculate(ClaudeResponse response, PromptOptions options) {
        long inputTokens = 0;
        long outputTokens = 0;

        if (response instanceof TextResponse textResponse) {
            // Use actual token count if available
            if (textResponse.tokensUsed() != null) {
                outputTokens = textResponse.tokensUsed();
            } else {
                outputTokens = estimateTokens(textResponse.content());
            }
            // Input tokens typically not returned, estimate from content length ratio
            inputTokens = outputTokens / 2;
        } else {
            // Estimate from content
            outputTokens = estimateTokens(response.content());
            inputTokens = outputTokens / 2;
        }

        var pricing = getPricing(options.model());
        var cost = calculateCost(inputTokens, outputTokens, pricing[0], pricing[1]);

        return new CostEstimate(inputTokens, outputTokens, cost, "USD", options.model());
    }

    private BigDecimal[] getPricing(String model) {
        if (model != null) {
            var pricing = MODEL_PRICING.get(normalizeModelName(model));
            if (pricing != null) {
                return pricing;
            }
        }
        return new BigDecimal[] {inputCostPerMillion, outputCostPerMillion};
    }

    private BigDecimal calculateCost(long inputTokens, long outputTokens, BigDecimal inputCost, BigDecimal outputCost) {
        var inputPrice = new BigDecimal(inputTokens).multiply(inputCost).divide(MILLION, 8, RoundingMode.HALF_UP);
        var outputPrice = new BigDecimal(outputTokens).multiply(outputCost).divide(MILLION, 8, RoundingMode.HALF_UP);
        return inputPrice.add(outputPrice).setScale(6, RoundingMode.HALF_UP);
    }

    private static String normalizeModelName(String model) {
        if (model == null) return "";
        // Remove version suffixes like -20241022
        return model.toLowerCase().replaceAll("-\\d{8}$", "");
    }

    @Override
    public int priority() {
        return 10;
    }
}
