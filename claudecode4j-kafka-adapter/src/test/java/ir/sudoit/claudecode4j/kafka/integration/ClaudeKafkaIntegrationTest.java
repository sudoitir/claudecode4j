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
package ir.sudoit.claudecode4j.kafka.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.api.model.request.PromptOptions;
import ir.sudoit.claudecode4j.api.model.response.TextResponse;
import ir.sudoit.claudecode4j.kafka.config.ClaudeKafkaProperties;
import ir.sudoit.claudecode4j.kafka.listener.ClaudeKafkaListener;
import ir.sudoit.claudecode4j.kafka.producer.ClaudeKafkaProducer;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

/**
 * Integration tests for Kafka request-reply messaging using ReplyingKafkaTemplate. Tests the full message flow from
 * producer to listener and back.
 */
@SpringBootTest(
        classes = {ClaudeKafkaIntegrationTest.BootConfig.class, ClaudeKafkaIntegrationTest.KafkaTestConfig.class})
@Testcontainers
class ClaudeKafkaIntegrationTest {

    private static final String REQUEST_TOPIC = "test-requests";
    private static final String REPLY_TOPIC = "test-replies";

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableKafka
    static class BootConfig {}

    @Container
    static KafkaContainer kafka = new KafkaContainer("apache/kafka:3.8.0");

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("test.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("claude.code.kafka.enabled", () -> "true");
        registry.add("claude.code.kafka.request-topic", () -> REQUEST_TOPIC);
        registry.add("claude.code.kafka.reply-topic", () -> REPLY_TOPIC);
        registry.add("claude.code.kafka.group-id", () -> "test-processor");
    }

    @MockitoBean
    private ClaudeClient claudeClient;

    @Autowired
    private ClaudeKafkaProducer producer;

    @Autowired
    private ClaudeKafkaProperties properties;

    @Autowired
    private org.springframework.kafka.config.KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @BeforeEach
    void setUp() throws Exception {
        // Wait for all listener containers to be fully assigned partitions
        for (var container : kafkaListenerEndpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, 1);
        }
    }

    @Test
    void shouldSendRequestAndReceiveReply() throws Exception {
        // Given: mock ClaudeClient returns expected response
        var expectedResponse =
                new TextResponse("Hello from Claude!", Instant.now(), Duration.ofMillis(500), "haiku", 42, null);
        when(claudeClient.execute(any(Prompt.class), any(PromptOptions.class))).thenReturn(expectedResponse);

        // When: send a request via Kafka
        var future = producer.sendRequest("What is 2+2?");

        // Then: receive the reply within timeout
        var result = future.get(30, TimeUnit.SECONDS);

        assertThat(result).isNotNull();
        assertThat(result).contains("Hello from Claude!");
        assertThat(result).contains("\"success\":true");
    }

    @Test
    void shouldHandleClaudeClientError() throws Exception {
        // Given: mock ClaudeClient throws an exception
        when(claudeClient.execute(any(Prompt.class), any(PromptOptions.class)))
                .thenThrow(new RuntimeException("Claude is unavailable"));

        // When: send a request via Kafka
        var future = producer.sendRequest("This will fail");

        // Then: receive error response
        var result = future.get(30, TimeUnit.SECONDS);

        assertThat(result).isNotNull();
        assertThat(result).contains("error");
        assertThat(result).contains("Claude is unavailable");
    }

    @Test
    void shouldUseConfiguredTopics() {
        assertThat(properties.requestTopic()).isEqualTo(REQUEST_TOPIC);
        assertThat(properties.replyTopic()).isEqualTo(REPLY_TOPIC);
        assertThat(properties.enabled()).isTrue();
    }

    @TestConfiguration
    @EnableKafka
    static class KafkaTestConfig {

        @Value("${test.kafka.bootstrap-servers}")
        private String bootstrapServers;

        @Bean
        ClaudeKafkaProperties claudeKafkaProperties() {
            return new ClaudeKafkaProperties(
                    true, REQUEST_TOPIC, REPLY_TOPIC, "test-processor", Duration.ofSeconds(30), 1);
        }

        @Bean
        ProducerFactory<String, String> producerFactory() {
            var props = new HashMap<String, Object>();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            return new DefaultKafkaProducerFactory<>(props);
        }

        @Bean
        ConsumerFactory<String, String> consumerFactory() {
            var props = new HashMap<String, Object>();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-processor");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            return new DefaultKafkaConsumerFactory<>(props);
        }

        @Bean
        ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
                ConsumerFactory<String, String> consumerFactory) {
            var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
            factory.setConsumerFactory(consumerFactory);
            factory.setReplyTemplate(kafkaTemplate(producerFactory()));
            return factory;
        }

        @Bean
        org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate(
                ProducerFactory<String, String> producerFactory) {
            return new org.springframework.kafka.core.KafkaTemplate<>(producerFactory);
        }

        @Bean
        ConcurrentMessageListenerContainer<String, String> repliesContainer(
                ConcurrentKafkaListenerContainerFactory<String, String> containerFactory) {
            ConcurrentMessageListenerContainer<String, String> container =
                    containerFactory.createContainer(REPLY_TOPIC);
            container.getContainerProperties().setGroupId("test-processor-reply");
            container.setAutoStartup(false);
            return container;
        }

        @Bean
        ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate(
                ProducerFactory<String, String> producerFactory,
                ConcurrentMessageListenerContainer<String, String> repliesContainer,
                ClaudeKafkaProperties properties) {
            ReplyingKafkaTemplate<String, String, String> template =
                    new ReplyingKafkaTemplate<>(producerFactory, repliesContainer);
            template.setDefaultReplyTimeout(properties.replyTimeout());
            template.setSharedReplyTopic(true);
            return template;
        }

        @Bean
        ClaudeKafkaListener claudeKafkaListener(ClaudeClient claudeClient) {
            return new ClaudeKafkaListener(claudeClient);
        }

        @Bean
        ClaudeKafkaProducer claudeKafkaProducer(
                ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate, ClaudeKafkaProperties properties) {
            return new ClaudeKafkaProducer(replyingKafkaTemplate, properties);
        }

        @Bean
        org.springframework.kafka.core.KafkaAdmin kafkaAdmin() {
            Map<String, Object> configs = new HashMap<>();
            configs.put(org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            return new org.springframework.kafka.core.KafkaAdmin(configs);
        }

        @Bean
        NewTopic requestTopic() {
            return new NewTopic(REQUEST_TOPIC, 1, (short) 1);
        }

        @Bean
        NewTopic replyTopic() {
            return new NewTopic(REPLY_TOPIC, 1, (short) 1);
        }
    }
}
