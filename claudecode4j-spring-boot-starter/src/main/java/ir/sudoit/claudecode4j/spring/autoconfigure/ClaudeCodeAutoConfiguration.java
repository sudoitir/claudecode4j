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

import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.api.client.ClaudeClientFactory;
import ir.sudoit.claudecode4j.api.mock.MockClaudeClient;
import ir.sudoit.claudecode4j.api.mock.MockResponseProvider;
import ir.sudoit.claudecode4j.api.mock.StaticMockProvider;
import ir.sudoit.claudecode4j.spring.properties.ClaudeCodeProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(ClaudeClient.class)
@ConditionalOnProperty(prefix = "claude.code", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ClaudeCodeProperties.class)
public class ClaudeCodeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "claude.code.mock", name = "enabled", havingValue = "false", matchIfMissing = true)
    public ClaudeClient claudeClient(ClaudeCodeProperties properties) {
        return ClaudeClientFactory.create(properties.toClaudeConfig());
    }

    @Bean
    @ConditionalOnMissingBean(MockResponseProvider.class)
    @ConditionalOnProperty(prefix = "claude.code.mock", name = "enabled", havingValue = "true")
    public MockResponseProvider mockResponseProvider(ClaudeCodeProperties properties) {
        var response = properties.getMockResponse();
        return response != null ? StaticMockProvider.withResponse(response) : StaticMockProvider.withDefault();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "claude.code.mock", name = "enabled", havingValue = "true")
    public ClaudeClient mockClaudeClient(ClaudeCodeProperties properties, MockResponseProvider mockProvider) {
        var builder = MockClaudeClient.builder(mockProvider).mockEnabled(true);

        if (properties.getMockDelay() != null) {
            builder.mockDelay(properties.getMockDelay());
        }

        return builder.build();
    }
}
