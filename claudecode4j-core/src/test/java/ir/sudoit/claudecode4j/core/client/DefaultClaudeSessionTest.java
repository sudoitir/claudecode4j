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
package ir.sudoit.claudecode4j.core.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.api.model.request.PromptOptions;
import ir.sudoit.claudecode4j.api.model.response.TextResponse;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("DefaultClaudeSession")
@ExtendWith(MockitoExtension.class)
class DefaultClaudeSessionTest {

    @Mock
    private DefaultClaudeClient client;

    private DefaultClaudeSession session;

    @BeforeEach
    void setUp() {
        session = new DefaultClaudeSession(client);
    }

    @Nested
    @DisplayName("sessionId")
    class SessionId {

        @Test
        @DisplayName("should generate unique session ID")
        void shouldGenerateUniqueSessionId() {
            assertThat(session.sessionId()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("should generate different IDs for different sessions")
        void shouldGenerateDifferentIdsForDifferentSessions() {
            var session2 = new DefaultClaudeSession(client);
            assertThat(session.sessionId()).isNotEqualTo(session2.sessionId());
        }
    }

    @Nested
    @DisplayName("send")
    class Send {

        @Test
        @DisplayName("should send message and return response")
        void shouldSendMessageAndReturnResponse() {
            var expectedResponse = new TextResponse("Hello!", Instant.now(), Duration.ofMillis(100), null, null, null);
            when(client.execute(any(Prompt.class), any(PromptOptions.class))).thenReturn(expectedResponse);

            var response = session.send("Hello");

            assertThat(response).isEqualTo(expectedResponse);
        }

        @Test
        @DisplayName("should add response to history")
        void shouldAddResponseToHistory() {
            var expectedResponse = new TextResponse("Response", Instant.now(), Duration.ofMillis(50), null, null, null);
            when(client.execute(any(Prompt.class), any(PromptOptions.class))).thenReturn(expectedResponse);

            session.send("Message");

            assertThat(session.history()).hasSize(1).containsExactly(expectedResponse);
        }

        @Test
        @DisplayName("should include system prompt in request when set")
        void shouldIncludeSystemPromptInRequest() {
            var expectedResponse = new TextResponse("Response", Instant.now(), Duration.ofMillis(50), null, null, null);
            when(client.execute(any(Prompt.class), any(PromptOptions.class))).thenReturn(expectedResponse);

            session.setSystemPrompt("You are helpful");
            session.send("Hello");

            assertThat(session.getSystemPrompt()).isEqualTo("You are helpful");
        }

        @Test
        @DisplayName("should throw exception when session is closed")
        void shouldThrowExceptionWhenSessionIsClosed() {
            session.close();

            assertThatThrownBy(() -> session.send("Hello"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Session is closed");
        }

        @Test
        @DisplayName("should use custom options when provided")
        void shouldUseCustomOptionsWhenProvided() {
            var expectedResponse = new TextResponse("Response", Instant.now(), Duration.ofMillis(50), null, null, null);
            var options = PromptOptions.builder().maxTurns(5).build();
            when(client.execute(any(Prompt.class), any(PromptOptions.class))).thenReturn(expectedResponse);

            var response = session.send("Hello", options);

            assertThat(response).isEqualTo(expectedResponse);
        }
    }

    @Nested
    @DisplayName("sendAsync")
    class SendAsync {

        @Test
        @DisplayName("should send message asynchronously")
        void shouldSendMessageAsynchronously() throws Exception {
            var expectedResponse =
                    new TextResponse("Async Response", Instant.now(), Duration.ofMillis(100), null, null, null);
            when(client.execute(any(Prompt.class), any(PromptOptions.class))).thenReturn(expectedResponse);

            var future = session.sendAsync("Async message");
            var response = future.get();

            assertThat(response).isEqualTo(expectedResponse);
        }

        @Test
        @DisplayName("should throw exception when session is closed")
        void shouldThrowExceptionWhenSessionIsClosed() {
            session.close();

            assertThatThrownBy(() -> session.sendAsync("Hello"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Session is closed");
        }
    }

    @Nested
    @DisplayName("history")
    class History {

        @Test
        @DisplayName("should return empty history initially")
        void shouldReturnEmptyHistoryInitially() {
            assertThat(session.history()).isEmpty();
        }

        @Test
        @DisplayName("should return unmodifiable history list")
        void shouldReturnUnmodifiableHistoryList() {
            var response = new TextResponse("Response", Instant.now(), Duration.ofMillis(50), null, null, null);
            when(client.execute(any(Prompt.class), any(PromptOptions.class))).thenReturn(response);

            session.send("Message");
            var history = session.history();

            assertThatThrownBy(() -> history.add(response)).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("should preserve order of responses")
        void shouldPreserveOrderOfResponses() {
            var response1 = new TextResponse("First", Instant.now(), Duration.ofMillis(50), null, null, null);
            var response2 = new TextResponse("Second", Instant.now(), Duration.ofMillis(50), null, null, null);
            when(client.execute(any(Prompt.class), any(PromptOptions.class)))
                    .thenReturn(response1)
                    .thenReturn(response2);

            session.send("First message");
            session.send("Second message");

            assertThat(session.history()).containsExactly(response1, response2);
        }
    }

    @Nested
    @DisplayName("clearHistory")
    class ClearHistory {

        @Test
        @DisplayName("should clear conversation history")
        void shouldClearConversationHistory() {
            var response = new TextResponse("Response", Instant.now(), Duration.ofMillis(50), null, null, null);
            when(client.execute(any(Prompt.class), any(PromptOptions.class))).thenReturn(response);

            session.send("Message");
            assertThat(session.history()).hasSize(1);

            session.clearHistory();

            assertThat(session.history()).isEmpty();
        }
    }

    @Nested
    @DisplayName("systemPrompt")
    class SystemPrompt {

        @Test
        @DisplayName("should return null initially")
        void shouldReturnNullInitially() {
            assertThat(session.getSystemPrompt()).isNull();
        }

        @Test
        @DisplayName("should set and get system prompt")
        void shouldSetAndGetSystemPrompt() {
            session.setSystemPrompt("You are a helpful assistant");

            assertThat(session.getSystemPrompt()).isEqualTo("You are a helpful assistant");
        }

        @Test
        @DisplayName("should allow clearing system prompt")
        void shouldAllowClearingSystemPrompt() {
            session.setSystemPrompt("Some prompt");
            session.setSystemPrompt(null);

            assertThat(session.getSystemPrompt()).isNull();
        }
    }

    @Nested
    @DisplayName("close")
    class Close {

        @Test
        @DisplayName("should clear history on close")
        void shouldClearHistoryOnClose() {
            var response = new TextResponse("Response", Instant.now(), Duration.ofMillis(50), null, null, null);
            when(client.execute(any(Prompt.class), any(PromptOptions.class))).thenReturn(response);

            session.send("Message");
            session.close();

            assertThat(session.history()).isEmpty();
        }

        @Test
        @DisplayName("should prevent further operations after close")
        void shouldPreventFurtherOperationsAfterClose() {
            session.close();

            assertThatThrownBy(() -> session.send("Message"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Session is closed");
        }
    }
}
