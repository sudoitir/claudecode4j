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
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jspecify.annotations.Nullable;

/**
 * Manages correlation IDs for Kafka request-reply pattern.
 *
 * <p>Uses a {@link DelayQueue} for efficient expiry-based cleanup instead of polling. Each registered request is
 * tracked and automatically expired after the timeout.
 */
public class CorrelationIdManager {

    private static final System.Logger log = System.getLogger(CorrelationIdManager.class.getName());

    public static final String CORRELATION_ID_HEADER = "claude-correlation-id";
    public static final String REPLY_TOPIC_HEADER = "claude-reply-topic";

    private final Map<String, PendingRequest<?>> pendingRequests = new ConcurrentHashMap<>();
    private final DelayQueue<ExpiringEntry> expiryQueue = new DelayQueue<>();
    private final Duration defaultTimeout;
    private final Thread cleanupThread;
    private volatile boolean running = true;

    public CorrelationIdManager(Duration defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
        this.cleanupThread = Thread.ofVirtual().name("correlation-cleanup").start(this::cleanupLoop);
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

        // Add to delay queue for efficient expiry-based cleanup
        expiryQueue.add(new ExpiringEntry(correlationId, timeout));

        // Remove from map when completed by any means (success, failure, or timeout)
        future.whenComplete((result, error) -> pendingRequests.remove(correlationId));

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

    /**
     * Cleanup loop that processes expired entries from the delay queue.
     *
     * <p>Uses blocking poll with 1-second timeout to allow checking the running flag.
     */
    private void cleanupLoop() {
        while (running) {
            try {
                ExpiringEntry entry = expiryQueue.poll(1, TimeUnit.SECONDS);
                if (entry != null) {
                    var pending = pendingRequests.remove(entry.correlationId());
                    if (pending != null && !pending.future().isDone()) {
                        log.log(System.Logger.Level.DEBUG, "Request timed out: {0}", entry.correlationId());
                        pending.future()
                                .completeExceptionally(
                                        new TimeoutException("Request timed out: " + entry.correlationId()));
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Shuts down the manager, canceling all pending requests.
     *
     * <p>Waits up to 5 seconds for the cleanup thread to terminate.
     */
    public void shutdown() {
        running = false;
        cleanupThread.interrupt();

        try {
            cleanupThread.join(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.log(System.Logger.Level.WARNING, "Interrupted while waiting for cleanup thread to terminate");
        }

        // Complete all pending requests exceptionally
        pendingRequests.values().forEach(request -> request.future()
                .completeExceptionally(new IllegalStateException("Manager shutdown")));
        pendingRequests.clear();
        expiryQueue.clear();
    }

    private record PendingRequest<T>(CompletableFuture<T> future, Class<T> responseType, Instant expiry) {}

    /** Entry in the delay queue for tracking request expiry. */
    private static final class ExpiringEntry implements Delayed {
        private final String correlationId;
        private final long expiryTimeNanos;

        ExpiringEntry(String correlationId, Duration timeout) {
            this.correlationId = correlationId;
            this.expiryTimeNanos = System.nanoTime() + timeout.toNanos();
        }

        String correlationId() {
            return correlationId;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(expiryTimeNanos - System.nanoTime(), TimeUnit.NANOSECONDS);
        }

        @Override
        public int compareTo(Delayed other) {
            if (other instanceof ExpiringEntry e) {
                return Long.compare(expiryTimeNanos, e.expiryTimeNanos);
            }
            return Long.compare(getDelay(TimeUnit.NANOSECONDS), other.getDelay(TimeUnit.NANOSECONDS));
        }
    }
}
