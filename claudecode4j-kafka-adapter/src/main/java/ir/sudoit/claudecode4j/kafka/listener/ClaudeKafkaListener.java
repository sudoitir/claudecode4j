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
package ir.sudoit.claudecode4j.kafka.listener;

import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.api.model.request.PromptOptions;
import ir.sudoit.claudecode4j.api.model.response.ClaudeResponse;
import ir.sudoit.claudecode4j.kafka.config.ClaudeKafkaProperties;
import ir.sudoit.claudecode4j.kafka.correlation.CorrelationIdManager;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jspecify.annotations.Nullable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Header;

public class ClaudeKafkaListener {

    private final ClaudeClient claudeClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ClaudeKafkaProperties properties;

    public ClaudeKafkaListener(
            ClaudeClient claudeClient, KafkaTemplate<String, String> kafkaTemplate, ClaudeKafkaProperties properties) {
        this.claudeClient = claudeClient;
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    @KafkaListener(
            topics = "${claude.code.kafka.request-topic:claude-requests}",
            groupId = "${claude.code.kafka.group-id:claude-processor}",
            concurrency = "${claude.code.kafka.concurrency:1}")
    public void handleRequest(
            ConsumerRecord<String, String> record,
            @Header(name = CorrelationIdManager.CORRELATION_ID_HEADER, required = false) @Nullable
                    byte[] correlationIdBytes,
            @Header(name = CorrelationIdManager.REPLY_TOPIC_HEADER, required = false) @Nullable
                    byte[] replyTopicBytes) {

        var correlationId = correlationIdBytes != null ? new String(correlationIdBytes, StandardCharsets.UTF_8) : null;
        var replyTopic =
                replyTopicBytes != null ? new String(replyTopicBytes, StandardCharsets.UTF_8) : properties.replyTopic();

        try {
            var prompt = parsePrompt(record.value());
            var response = claudeClient.execute(prompt, PromptOptions.defaults());
            sendReply(replyTopic, correlationId, response, null);
        } catch (Exception e) {
            sendReply(replyTopic, correlationId, null, e);
        }
    }

    private Prompt parsePrompt(String value) {
        return Prompt.of(value);
    }

    private void sendReply(
            String replyTopic,
            @Nullable String correlationId,
            @Nullable ClaudeResponse response,
            @Nullable Exception error) {

        var replyValue = buildReplyValue(response, error);
        var producerRecord = new ProducerRecord<String, String>(replyTopic, replyValue);

        if (correlationId != null) {
            producerRecord
                    .headers()
                    .add(CorrelationIdManager.CORRELATION_ID_HEADER, correlationId.getBytes(StandardCharsets.UTF_8));
        }

        if (error != null) {
            producerRecord.headers().add("error", "true".getBytes(StandardCharsets.UTF_8));
            producerRecord.headers().add("error-message", error.getMessage().getBytes(StandardCharsets.UTF_8));
        }

        kafkaTemplate.send(producerRecord);
    }

    private String buildReplyValue(@Nullable ClaudeResponse response, @Nullable Exception error) {
        if (error != null) {
            return "{\"error\":\"" + escapeJson(error.getMessage()) + "\"}";
        }
        if (response != null) {
            return "{\"content\":\"" + escapeJson(response.content()) + "\",\"success\":"
                    + response.isSuccess() + ",\"durationMillis\":"
                    + response.duration().toMillis() + "}";
        }
        return "{\"error\":\"No response\"}";
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
