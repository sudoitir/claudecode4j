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
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

/**
 * Producer for sending Claude prompt requests via Kafka with request-reply pattern.
 *
 * <p>Uses Spring Kafka's {@link ReplyingKafkaTemplate} for automatic correlation handling, timeout management, and
 * reply processing.
 */
public class ClaudeKafkaProducer {

    private final ReplyingKafkaTemplate<String, String, String> replyingTemplate;
    private final ClaudeKafkaProperties properties;

    public ClaudeKafkaProducer(
            ReplyingKafkaTemplate<String, String, String> replyingTemplate, ClaudeKafkaProperties properties) {
        this.replyingTemplate = replyingTemplate;
        this.properties = properties;
    }

    /**
     * Sends a prompt request and waits for the reply.
     *
     * @param promptText the prompt text to send
     * @return a CompletableFuture that completes with the response JSON
     */
    public CompletableFuture<String> sendRequest(String promptText) {
        var record = new ProducerRecord<String, String>(properties.requestTopic(), promptText);

        return replyingTemplate
                .sendAndReceive(record)
                .thenApply(consumerRecord -> consumerRecord.value())
                .toCompletableFuture();
    }
}
