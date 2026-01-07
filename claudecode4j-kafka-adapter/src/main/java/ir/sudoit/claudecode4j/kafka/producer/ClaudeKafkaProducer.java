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
package ir.sudoit.claudecode4j.kafka.producer;

import ir.sudoit.claudecode4j.kafka.config.ClaudeKafkaProperties;
import ir.sudoit.claudecode4j.kafka.correlation.CorrelationIdManager;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

public class ClaudeKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final CorrelationIdManager correlationManager;
    private final ClaudeKafkaProperties properties;

    public ClaudeKafkaProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            CorrelationIdManager correlationManager,
            ClaudeKafkaProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.correlationManager = correlationManager;
        this.properties = properties;
    }

    public CompletableFuture<String> sendRequest(String promptText) {
        var correlationId = correlationManager.generateCorrelationId();
        var future = correlationManager.registerRequest(correlationId, String.class, properties.replyTimeout());

        var record = new ProducerRecord<String, String>(properties.requestTopic(), promptText);
        record.headers()
                .add(CorrelationIdManager.CORRELATION_ID_HEADER, correlationId.getBytes(StandardCharsets.UTF_8));
        record.headers()
                .add(
                        CorrelationIdManager.REPLY_TOPIC_HEADER,
                        properties.replyTopic().getBytes(StandardCharsets.UTF_8));

        kafkaTemplate.send(record).whenComplete((result, error) -> {
            if (error != null) {
                correlationManager.failRequest(correlationId, error);
            }
        });

        return future;
    }

    @KafkaListener(
            topics = "${claude.code.kafka.reply-topic:claude-replies}",
            groupId = "${claude.code.kafka.group-id:claude-processor}-reply")
    public void handleReply(ConsumerRecord<String, String> record) {
        var correlationIdHeader = record.headers().lastHeader(CorrelationIdManager.CORRELATION_ID_HEADER);
        if (correlationIdHeader == null) {
            return;
        }

        var correlationId = new String(correlationIdHeader.value(), StandardCharsets.UTF_8);
        // Complete with the response body - it contains either success or error as JSON
        correlationManager.completeRequest(correlationId, record.value());
    }

    public int getPendingRequests() {
        return correlationManager.getPendingCount();
    }
}
