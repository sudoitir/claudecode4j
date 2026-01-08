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
package ir.sudoit.claudecode4j.spring.autoconfigure;

import ir.sudoit.claudecode4j.api.exception.ClaudeExecutionException;
import ir.sudoit.claudecode4j.api.exception.ClaudeTimeoutException;
import ir.sudoit.claudecode4j.api.spi.ProcessExecutor;
import ir.sudoit.claudecode4j.spring.properties.ClaudeCodeProperties;
import ir.sudoit.claudecode4j.spring.resilience.ResilientProcessExecutor;
import java.util.Map;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * Auto-configuration for resilience features.
 *
 * <p>This configuration sets up retry with exponential backoff for ProcessExecutor. It's only active when spring-retry
 * is on the classpath and resilience is enabled.
 */
@AutoConfiguration(after = ClaudeCodeAutoConfiguration.class)
@ConditionalOnClass(RetryTemplate.class)
@ConditionalOnProperty(prefix = "claude.code.resilience", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(ClaudeCodeProperties.class)
public class ClaudeCodeResilienceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "claudeRetryTemplate")
    public RetryTemplate claudeRetryTemplate(ClaudeCodeProperties properties) {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Configure retry policy for specific exceptions
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = Map.of(
                ClaudeTimeoutException.class, true,
                ClaudeExecutionException.class, true);

        SimpleRetryPolicy retryPolicy =
                new SimpleRetryPolicy(properties.getResilienceMaxRetries(), retryableExceptions);
        retryTemplate.setRetryPolicy(retryPolicy);

        // Configure exponential backoff
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(properties.getResilienceInitialDelay().toMillis());
        backOffPolicy.setMultiplier(properties.getResilienceMultiplier());
        backOffPolicy.setMaxInterval(properties.getResilienceMaxDelay().toMillis());
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

    @Bean
    @Primary
    @ConditionalOnBean(ProcessExecutor.class)
    public ProcessExecutor resilientProcessExecutor(ProcessExecutor delegate, RetryTemplate claudeRetryTemplate) {
        return new ResilientProcessExecutor(delegate, claudeRetryTemplate);
    }
}
