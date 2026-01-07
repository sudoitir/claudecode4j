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
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jspecify.annotations.Nullable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;

/**
 * Kafka listener that processes Claude prompt requests.
 *
 * <p>Uses Spring Kafka's {@code @SendTo} annotation for automatic reply routing based on the reply topic header set by
 * {@link org.springframework.kafka.requestreply.ReplyingKafkaTemplate}.
 */
public class ClaudeKafkaListener {

    private final ClaudeClient claudeClient;

    public ClaudeKafkaListener(ClaudeClient claudeClient) {
        this.claudeClient = claudeClient;
    }

    /**
     * Handles incoming prompt requests and returns the response.
     *
     * <p>The {@code @SendTo} annotation automatically sends the return value to the reply topic specified in the
     * request's reply topic header.
     *
     * @param record the incoming Kafka record containing the prompt text
     * @return the response JSON string
     */
    @KafkaListener(
            topics = "${claude.code.kafka.request-topic:claude-requests}",
            groupId = "${claude.code.kafka.group-id:claude-processor}",
            concurrency = "${claude.code.kafka.concurrency:1}")
    @SendTo
    public String handleRequest(ConsumerRecord<String, String> record) {
        try {
            var prompt = Prompt.of(record.value());
            var response = claudeClient.execute(prompt, PromptOptions.defaults());
            return buildSuccessReply(response);
        } catch (Exception e) {
            return buildErrorReply(e);
        }
    }

    private String buildSuccessReply(ClaudeResponse response) {
        return "{\"content\":\"" + escapeJson(response.content()) + "\",\"success\":"
                + response.isSuccess() + ",\"durationMillis\":"
                + response.duration().toMillis() + "}";
    }

    private String buildErrorReply(Exception error) {
        var message = error.getMessage() != null ? error.getMessage() : "Unknown error";
        return "{\"error\":\"" + escapeJson(message) + "\"}";
    }

    private String escapeJson(@Nullable String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
