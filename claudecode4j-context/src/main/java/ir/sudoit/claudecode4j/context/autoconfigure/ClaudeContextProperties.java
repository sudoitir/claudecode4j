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
package ir.sudoit.claudecode4j.context.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration properties for context optimization.
 *
 * @param enabled whether context optimization is enabled
 * @param model the default model for token limits
 * @param maxContextRatio the maximum ratio of context window to use (0.0 to 1.0)
 * @param rankingStrategy the strategy for ranking files (recency, size, preserve_order)
 * @param cacheTokenCounts whether to cache file token counts
 * @param warnOnTruncation whether to log warnings when files are excluded
 */
@ConfigurationProperties(prefix = "claude.code.context")
public record ClaudeContextProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("claude-sonnet-4") String model,
        @DefaultValue("0.8") double maxContextRatio,
        @DefaultValue("recency") String rankingStrategy,
        @DefaultValue("true") boolean cacheTokenCounts,
        @DefaultValue("true") boolean warnOnTruncation) {

    /**
     * Returns the ranking strategy name, validated.
     *
     * @return the ranking strategy name
     */
    public String getRankingStrategyName() {
        return switch (rankingStrategy.toLowerCase()) {
            case "recency", "size", "preserve_order" -> rankingStrategy.toLowerCase();
            default -> "recency";
        };
    }

    /**
     * Returns the max context ratio, clamped to valid range.
     *
     * @return the max context ratio between 0.0 and 1.0
     */
    public double getMaxContextRatioClamped() {
        return Math.max(0.0, Math.min(1.0, maxContextRatio));
    }
}
