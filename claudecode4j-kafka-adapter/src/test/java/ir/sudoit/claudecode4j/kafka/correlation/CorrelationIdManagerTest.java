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
package ir.sudoit.claudecode4j.kafka.correlation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CorrelationIdManagerTest {

    private CorrelationIdManager manager;

    @BeforeEach
    void setUp() {
        manager = new CorrelationIdManager(Duration.ofSeconds(30));
    }

    @AfterEach
    void tearDown() {
        manager.shutdown();
    }

    @Test
    void shouldGenerateUniqueCorrelationIds() {
        var id1 = manager.generateCorrelationId();
        var id2 = manager.generateCorrelationId();

        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    void shouldRegisterAndCompleteRequest() throws Exception {
        var correlationId = manager.generateCorrelationId();
        var future = manager.registerRequest(correlationId, String.class);

        assertThat(manager.hasPendingRequest(correlationId)).isTrue();
        assertThat(manager.getPendingCount()).isEqualTo(1);

        manager.completeRequest(correlationId, "Test Response");

        assertThat(future.get(1, TimeUnit.SECONDS)).isEqualTo("Test Response");
        assertThat(manager.hasPendingRequest(correlationId)).isFalse();
    }

    @Test
    void shouldFailRequest() {
        var correlationId = manager.generateCorrelationId();
        var future = manager.registerRequest(correlationId, String.class);

        manager.failRequest(correlationId, new RuntimeException("Test Error"));

        assertThatThrownBy(() -> future.get(1, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(RuntimeException.class)
                .hasMessageContaining("Test Error");
    }

    @Test
    void shouldTimeoutRequest() {
        var correlationId = manager.generateCorrelationId();
        var future = manager.registerRequest(correlationId, String.class, Duration.ofMillis(100));

        assertThatThrownBy(() -> future.get(1, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(TimeoutException.class);
    }

    @Test
    void shouldReturnFalseForUnknownCorrelationId() {
        var result = manager.completeRequest("unknown-id", "response");

        assertThat(result).isFalse();
    }

    @Test
    void shouldTrackResponseType() {
        var correlationId = manager.generateCorrelationId();
        manager.registerRequest(correlationId, String.class);

        assertThat(manager.getResponseType(correlationId)).isEqualTo(String.class);
    }

    @Test
    void shouldReturnNullForUnknownResponseType() {
        assertThat(manager.getResponseType("unknown-id")).isNull();
    }

    @Test
    void shouldHandleMultiplePendingRequests() {
        var id1 = manager.generateCorrelationId();
        var id2 = manager.generateCorrelationId();
        var id3 = manager.generateCorrelationId();

        manager.registerRequest(id1, String.class);
        manager.registerRequest(id2, Integer.class);
        manager.registerRequest(id3, Boolean.class);

        assertThat(manager.getPendingCount()).isEqualTo(3);

        manager.completeRequest(id2, 42);

        assertThat(manager.getPendingCount()).isEqualTo(2);
        assertThat(manager.hasPendingRequest(id1)).isTrue();
        assertThat(manager.hasPendingRequest(id2)).isFalse();
        assertThat(manager.hasPendingRequest(id3)).isTrue();
    }

    @Test
    void shouldClearPendingRequestsOnShutdown() {
        var id1 = manager.generateCorrelationId();
        var id2 = manager.generateCorrelationId();

        var future1 = manager.registerRequest(id1, String.class);
        var future2 = manager.registerRequest(id2, String.class);

        manager.shutdown();

        assertThat(manager.getPendingCount()).isEqualTo(0);
        assertThat(future1).isCompletedExceptionally();
        assertThat(future2).isCompletedExceptionally();
    }
}
