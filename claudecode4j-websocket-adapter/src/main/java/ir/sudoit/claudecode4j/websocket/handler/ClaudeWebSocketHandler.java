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
package ir.sudoit.claudecode4j.websocket.handler;

import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.api.model.request.OutputFormat;
import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.api.model.request.PromptOptions;
import ir.sudoit.claudecode4j.api.model.response.StreamEvent;
import ir.sudoit.claudecode4j.websocket.message.ErrorMessage;
import ir.sudoit.claudecode4j.websocket.message.PromptMessage;
import ir.sudoit.claudecode4j.websocket.message.ResponseMessage;
import ir.sudoit.claudecode4j.websocket.message.SessionMessage;
import ir.sudoit.claudecode4j.websocket.message.StreamChunkMessage;
import ir.sudoit.claudecode4j.websocket.message.WebSocketMessage;
import ir.sudoit.claudecode4j.websocket.session.SessionRegistry;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

/**
 * WebSocket handler for interactive Claude CLI sessions.
 *
 * <p>This handler enables human-in-the-loop workflows by allowing bidirectional communication between the client and
 * Claude CLI through WebSocket.
 */
public class ClaudeWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOG = System.getLogger(ClaudeWebSocketHandler.class.getName());

    private final ClaudeClient claudeClient;
    private final SessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;

    public ClaudeWebSocketHandler(
            ClaudeClient claudeClient, SessionRegistry sessionRegistry, ObjectMapper objectMapper) {
        this.claudeClient = claudeClient;
        this.sessionRegistry = sessionRegistry;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = sessionRegistry.register(session);
        LOG.log(Level.INFO, "WebSocket session established: {0}", sessionId);
        sendMessage(session, SessionMessage.connected(sessionId));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessionRegistry.unregister(sessionId);
        LOG.log(Level.INFO, "WebSocket session closed: {0} with status: {1}", sessionId, status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        String payload = message.getPayload();

        try {
            // Parse the incoming message to determine type
            var node = objectMapper.readTree(payload);
            String type = node.has("type") ? node.get("type").asText() : "prompt";

            switch (type) {
                case "prompt" -> handlePromptMessage(session, sessionId, payload);
                case "permission_response" -> handlePermissionResponse(session, sessionId, payload);
                default ->
                    sendMessage(
                            session,
                            ErrorMessage.of(sessionId, "UNKNOWN_MESSAGE_TYPE", "Unknown message type: " + type));
            }
        } catch (Exception e) {
            LOG.log(Level.ERROR, "Error handling message for session {0}: {1}", sessionId, e.getMessage());
            sendMessage(session, ErrorMessage.executionError(sessionId, e.getMessage()));
        }
    }

    private void handlePromptMessage(WebSocketSession session, String sessionId, String payload) throws IOException {
        PromptMessage promptMsg = objectMapper.readValue(payload, PromptMessage.class);

        // Convert context file strings to Paths
        List<Path> contextPaths = promptMsg.contextFiles() != null
                ? promptMsg.contextFiles().stream().map(Path::of).toList()
                : List.of();

        // Build the prompt
        Prompt prompt = Prompt.builder()
                .text(promptMsg.text())
                .systemPrompt(promptMsg.systemPrompt())
                .contextFiles(contextPaths)
                .build();

        // Execute with streaming to send chunks as they arrive
        PromptOptions options =
                PromptOptions.builder().outputFormat(OutputFormat.STREAM_JSON).build();

        Thread.ofVirtual().start(() -> {
            try {
                var publisher = claudeClient.stream(prompt, options);
                AtomicLong sequence = new AtomicLong(0);

                publisher.subscribe(new Flow.Subscriber<StreamEvent>() {
                    private Flow.Subscription subscription;

                    @Override
                    public void onSubscribe(Flow.Subscription subscription) {
                        this.subscription = subscription;
                        subscription.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(StreamEvent event) {
                        try {
                            if (event.content() != null && !event.content().isEmpty()) {
                                sendMessage(
                                        session,
                                        StreamChunkMessage.of(sessionId, event.content(), sequence.incrementAndGet()));
                            }
                        } catch (IOException e) {
                            LOG.log(Level.ERROR, "Error sending stream chunk: {0}", e.getMessage());
                            subscription.cancel();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        try {
                            sendMessage(session, ErrorMessage.executionError(sessionId, throwable.getMessage()));
                        } catch (IOException e) {
                            LOG.log(Level.ERROR, "Error sending error message: {0}", e.getMessage());
                        }
                    }

                    @Override
                    public void onComplete() {
                        try {
                            sendMessage(session, ResponseMessage.of(sessionId, "", true));
                        } catch (IOException e) {
                            LOG.log(Level.ERROR, "Error sending completion message: {0}", e.getMessage());
                        }
                    }
                });
            } catch (Exception e) {
                try {
                    sendMessage(session, ErrorMessage.executionError(sessionId, e.getMessage()));
                } catch (IOException ex) {
                    LOG.log(Level.ERROR, "Error sending error message: {0}", ex.getMessage());
                }
            }
        });
    }

    private void handlePermissionResponse(WebSocketSession session, String sessionId, String payload)
            throws IOException {
        // For now, log the permission response
        // Full implementation would require process stdin integration
        LOG.log(Level.INFO, "Received permission response for session {0}: {1}", sessionId, payload);
        sendMessage(session, ResponseMessage.of(sessionId, "Permission response received", false));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        LOG.log(Level.ERROR, "Transport error for session {0}: {1}", sessionId, exception.getMessage());
        sessionRegistry.unregister(sessionId);
    }

    private void sendMessage(WebSocketSession session, WebSocketMessage message) throws IOException {
        if (session.isOpen()) {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        }
    }
}
