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
package ir.sudoit.claudecode4j.kafka.autoconfigure;

import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.kafka.config.ClaudeKafkaProperties;
import ir.sudoit.claudecode4j.kafka.correlation.CorrelationIdManager;
import ir.sudoit.claudecode4j.kafka.listener.ClaudeKafkaListener;
import ir.sudoit.claudecode4j.kafka.producer.ClaudeKafkaProducer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;

@AutoConfiguration
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnBean(ClaudeClient.class)
@ConditionalOnProperty(prefix = "claude.code.kafka", name = "enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties(ClaudeKafkaProperties.class)
@EnableKafka
public class ClaudeKafkaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CorrelationIdManager correlationIdManager(ClaudeKafkaProperties properties) {
        return new CorrelationIdManager(properties.replyTimeout());
    }

    @Bean
    @ConditionalOnMissingBean
    public ClaudeKafkaListener claudeKafkaListener(
            ClaudeClient claudeClient, KafkaTemplate<String, String> kafkaTemplate, ClaudeKafkaProperties properties) {
        return new ClaudeKafkaListener(claudeClient, kafkaTemplate, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ClaudeKafkaProducer claudeKafkaProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            CorrelationIdManager correlationManager,
            ClaudeKafkaProperties properties) {
        return new ClaudeKafkaProducer(kafkaTemplate, correlationManager, properties);
    }
}
