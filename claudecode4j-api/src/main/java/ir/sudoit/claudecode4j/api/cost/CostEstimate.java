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

import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

/**
 * Represents a cost estimate or actual cost for a Claude API call.
 *
 * @param inputTokens number of input tokens
 * @param outputTokens number of output tokens
 * @param totalCost total cost in the specified currency
 * @param currency the currency code (e.g., "USD")
 * @param model the model used for this estimate
 */
public record CostEstimate(
        long inputTokens,
        long outputTokens,
        BigDecimal totalCost,
        String currency,
        @Nullable String model) {

    /** Creates a new CostEstimate with USD currency. */
    public static CostEstimate ofUSD(long inputTokens, long outputTokens, BigDecimal totalCost) {
        return new CostEstimate(inputTokens, outputTokens, totalCost, "USD", null);
    }

    /** Creates a new CostEstimate with USD currency and model. */
    public static CostEstimate ofUSD(long inputTokens, long outputTokens, BigDecimal totalCost, String model) {
        return new CostEstimate(inputTokens, outputTokens, totalCost, "USD", model);
    }

    /** Creates a builder for CostEstimate. */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the total number of tokens (input + output). */
    public long totalTokens() {
        return inputTokens + outputTokens;
    }

    public static final class Builder {
        private long inputTokens;
        private long outputTokens;
        private BigDecimal totalCost = BigDecimal.ZERO;
        private String currency = "USD";
        private @Nullable String model;

        private Builder() {}

        public Builder inputTokens(long inputTokens) {
            this.inputTokens = inputTokens;
            return this;
        }

        public Builder outputTokens(long outputTokens) {
            this.outputTokens = outputTokens;
            return this;
        }

        public Builder totalCost(BigDecimal totalCost) {
            this.totalCost = totalCost;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public CostEstimate build() {
            return new CostEstimate(inputTokens, outputTokens, totalCost, currency, model);
        }
    }
}
