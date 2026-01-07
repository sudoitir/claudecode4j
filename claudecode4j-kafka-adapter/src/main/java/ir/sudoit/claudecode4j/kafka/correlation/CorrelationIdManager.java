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

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jspecify.annotations.Nullable;

public class CorrelationIdManager {

    public static final String CORRELATION_ID_HEADER = "claude-correlation-id";
    public static final String REPLY_TOPIC_HEADER = "claude-reply-topic";

    private final Map<String, PendingRequest<?>> pendingRequests = new ConcurrentHashMap<>();
    private final Duration defaultTimeout;
    private final ScheduledExecutorService cleanupExecutor;

    public CorrelationIdManager(Duration defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            var thread = Thread.ofVirtual().unstarted(r);
            thread.setName("correlation-cleanup");
            return thread;
        });
        scheduleCleanup();
    }

    public String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    public <T> CompletableFuture<T> registerRequest(String correlationId, Class<T> responseType) {
        return registerRequest(correlationId, responseType, defaultTimeout);
    }

    public <T> CompletableFuture<T> registerRequest(String correlationId, Class<T> responseType, Duration timeout) {
        var future = new CompletableFuture<T>();
        var request = new PendingRequest<>(future, responseType, Instant.now().plus(timeout));
        pendingRequests.put(correlationId, request);

        future.orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .whenComplete((result, error) -> pendingRequests.remove(correlationId));

        return future;
    }

    @SuppressWarnings("unchecked")
    public <T> boolean completeRequest(String correlationId, T response) {
        var pending = (PendingRequest<T>) pendingRequests.remove(correlationId);
        if (pending != null) {
            return pending.future().complete(response);
        }
        return false;
    }

    public boolean failRequest(String correlationId, Throwable error) {
        var pending = pendingRequests.remove(correlationId);
        if (pending != null) {
            return pending.future().completeExceptionally(error);
        }
        return false;
    }

    public @Nullable Class<?> getResponseType(String correlationId) {
        var pending = pendingRequests.get(correlationId);
        return pending != null ? pending.responseType() : null;
    }

    public boolean hasPendingRequest(String correlationId) {
        return pendingRequests.containsKey(correlationId);
    }

    public int getPendingCount() {
        return pendingRequests.size();
    }

    private void scheduleCleanup() {
        cleanupExecutor.scheduleAtFixedRate(
                () -> {
                    var now = Instant.now();
                    pendingRequests.entrySet().removeIf(entry -> {
                        if (entry.getValue().expiry().isBefore(now)) {
                            entry.getValue()
                                    .future()
                                    .completeExceptionally(
                                            new TimeoutException("Request timed out: " + entry.getKey()));
                            return true;
                        }
                        return false;
                    });
                },
                1,
                1,
                TimeUnit.MINUTES);
    }

    public void shutdown() {
        cleanupExecutor.shutdown();
        pendingRequests.values().forEach(request -> request.future()
                .completeExceptionally(new IllegalStateException("Manager shutdown")));
        pendingRequests.clear();
    }

    private record PendingRequest<T>(CompletableFuture<T> future, Class<T> responseType, Instant expiry) {}
}
