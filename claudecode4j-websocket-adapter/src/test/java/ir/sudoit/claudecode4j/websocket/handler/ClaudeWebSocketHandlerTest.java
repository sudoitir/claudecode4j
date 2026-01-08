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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.websocket.session.SessionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class ClaudeWebSocketHandlerTest {

    @Mock
    private ClaudeClient claudeClient;

    private SessionRegistry sessionRegistry;
    private ClaudeWebSocketHandler handler;

    @BeforeEach
    void setUp() {
        sessionRegistry = new SessionRegistry(100);
        handler = new ClaudeWebSocketHandler(
                claudeClient, sessionRegistry, JsonMapper.builder().build());
    }

    @Test
    void shouldRegisterSessionOnConnection() throws Exception {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn("test-session-id");
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);

        assertThat(sessionRegistry.get("test-session-id")).isPresent();
    }

    @Test
    void shouldUnregisterSessionOnClose() throws Exception {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn("test-session-id");
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);
        handler.afterConnectionClosed(session, CloseStatus.NORMAL);

        assertThat(sessionRegistry.get("test-session-id")).isEmpty();
    }

    @Test
    void shouldSendConnectedMessageOnConnection() throws Exception {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn("test-session-id");
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(captor.capture());

        String payload = captor.getValue().getPayload();
        assertThat(payload).contains("\"sessionId\":\"test-session-id\"");
        assertThat(payload).contains("\"status\":\"connected\"");
    }

    @Test
    void shouldHandleUnknownMessageType() throws Exception {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn("test-session-id");
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);

        TextMessage message = new TextMessage("{\"type\":\"unknown\"}");
        handler.handleTextMessage(session, message);

        // Should send error message for unknown type
        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, org.mockito.Mockito.atLeast(1)).sendMessage(captor.capture());

        boolean hasErrorMessage = captor.getAllValues().stream()
                .map(TextMessage::getPayload)
                .anyMatch(p -> p.contains("UNKNOWN_MESSAGE_TYPE"));
        assertThat(hasErrorMessage).isTrue();
    }

    @Test
    void shouldUnregisterSessionOnTransportError() throws Exception {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn("test-session-id");
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);
        handler.handleTransportError(session, new RuntimeException("Test error"));

        assertThat(sessionRegistry.get("test-session-id")).isEmpty();
    }
}
