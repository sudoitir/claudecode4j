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
package ir.sudoit.claudecode4j.spring.properties;

import ir.sudoit.claudecode4j.api.config.ClaudeConfig;
import java.nio.file.Path;
import java.time.Duration;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "claude.code")
public record ClaudeCodeProperties(
        @Nullable String binaryPath,
        @DefaultValue("4") int concurrencyLimit,
        @DefaultValue("5m") Duration defaultTimeout,
        @DefaultValue("false") boolean dangerouslySkipPermissions,
        @DefaultValue("true") boolean enabled,
        @Nullable Health health,
        @Nullable Metrics metrics,
        @Nullable Mock mock,
        @Nullable Resilience resilience) {
    public record Health(
            @DefaultValue("true") boolean enabled,
            @DefaultValue("10s") Duration timeout,
            @Nullable String minimumVersion) {}

    public record Metrics(
            @DefaultValue("true") boolean enabled,
            @DefaultValue("claude.code") String prefix) {}

    public record Mock(
            @DefaultValue("false") boolean enabled,
            @Nullable String response,
            @Nullable Duration delay) {}

    public record Resilience(
            @DefaultValue("false") boolean enabled,
            @DefaultValue("3") int maxRetries,
            @DefaultValue("1s") Duration initialDelay,
            @DefaultValue("2.0") double multiplier,
            @DefaultValue("30s") Duration maxDelay) {}

    public ClaudeConfig toClaudeConfig() {
        return ClaudeConfig.builder()
                .binaryPath(binaryPath != null ? Path.of(binaryPath) : null)
                .concurrencyLimit(concurrencyLimit)
                .defaultTimeout(defaultTimeout)
                .dangerouslySkipPermissions(dangerouslySkipPermissions)
                .build();
    }

    public boolean isHealthEnabled() {
        return health == null || health.enabled();
    }

    public boolean isMetricsEnabled() {
        return metrics == null || metrics.enabled();
    }

    public String getMetricsPrefix() {
        return metrics != null ? metrics.prefix() : "claude.code";
    }

    public Duration getHealthTimeout() {
        return health != null ? health.timeout() : Duration.ofSeconds(10);
    }

    public boolean isMockEnabled() {
        return mock != null && mock.enabled();
    }

    public @Nullable String getMockResponse() {
        return mock != null ? mock.response() : null;
    }

    public @Nullable Duration getMockDelay() {
        return mock != null ? mock.delay() : null;
    }

    public boolean isResilienceEnabled() {
        return resilience != null && resilience.enabled();
    }

    public int getResilienceMaxRetries() {
        return resilience != null ? resilience.maxRetries() : 3;
    }

    public Duration getResilienceInitialDelay() {
        return resilience != null ? resilience.initialDelay() : Duration.ofSeconds(1);
    }

    public double getResilienceMultiplier() {
        return resilience != null ? resilience.multiplier() : 2.0;
    }

    public Duration getResilienceMaxDelay() {
        return resilience != null ? resilience.maxDelay() : Duration.ofSeconds(30);
    }
}
