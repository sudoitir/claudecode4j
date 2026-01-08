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

import ir.sudoit.claudecode4j.context.optimizer.DefaultContextOptimizer;
import ir.sudoit.claudecode4j.context.optimizer.RankingStrategy;
import ir.sudoit.claudecode4j.context.spi.ContextOptimizer;
import ir.sudoit.claudecode4j.context.spi.TokenCounter;
import ir.sudoit.claudecode4j.context.tokenizer.CachingTokenCounter;
import ir.sudoit.claudecode4j.context.tokenizer.JTokkitTokenCounter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Auto-configuration for context optimization. */
@AutoConfiguration
@ConditionalOnClass(ContextOptimizer.class)
@ConditionalOnProperty(prefix = "claude.code.context", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ClaudeContextProperties.class)
public class ClaudeContextAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TokenCounter tokenCounter(ClaudeContextProperties properties) {
        TokenCounter baseCounter = new JTokkitTokenCounter();
        if (properties.cacheTokenCounts()) {
            return new CachingTokenCounter(baseCounter);
        }
        return baseCounter;
    }

    @Bean
    @ConditionalOnMissingBean
    public RankingStrategy rankingStrategy(ClaudeContextProperties properties) {
        return switch (properties.getRankingStrategyName()) {
            case "size" -> RankingStrategy.size();
            case "preserve_order" -> RankingStrategy.preserveOrder();
            default -> RankingStrategy.recency();
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public ContextOptimizer contextOptimizer(
            TokenCounter tokenCounter, RankingStrategy rankingStrategy, ClaudeContextProperties properties) {
        return DefaultContextOptimizer.builder()
                .tokenCounter(tokenCounter)
                .rankingStrategy(rankingStrategy)
                .warnOnExclusion(properties.warnOnTruncation())
                .build();
    }
}
