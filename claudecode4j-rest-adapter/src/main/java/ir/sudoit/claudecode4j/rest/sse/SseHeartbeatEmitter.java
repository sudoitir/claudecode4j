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
package ir.sudoit.claudecode4j.rest.sse;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SseEmitter wrapper that sends periodic heartbeat comments to keep the connection alive.
 *
 * <p>Heartbeats are sent as SSE comments ({@code : keep-alive}) which are ignored by clients but prevent intermediaries
 * (load balancers, proxies) from closing the connection due to inactivity.
 *
 * <p>This is particularly useful for long-running Claude model executions where the model may "think" for extended
 * periods before emitting the first token.
 */
public class SseHeartbeatEmitter extends SseEmitter {

    private static final Duration DEFAULT_HEARTBEAT_INTERVAL = Duration.ofSeconds(15);
    private static final String HEARTBEAT_COMMENT = "keep-alive";

    private final ScheduledExecutorService scheduler;
    private final Duration heartbeatInterval;
    private final AtomicLong lastActivityTime = new AtomicLong(System.currentTimeMillis());
    private volatile ScheduledFuture<?> heartbeatTask;

    /**
     * Creates a new SseHeartbeatEmitter with the default 15-second heartbeat interval.
     *
     * @param timeout the SSE connection timeout in milliseconds
     * @param scheduler the scheduler to use for heartbeat tasks
     */
    public SseHeartbeatEmitter(Long timeout, ScheduledExecutorService scheduler) {
        this(timeout, scheduler, DEFAULT_HEARTBEAT_INTERVAL);
    }

    /**
     * Creates a new SseHeartbeatEmitter with a custom heartbeat interval.
     *
     * @param timeout the SSE connection timeout in milliseconds
     * @param scheduler the scheduler to use for heartbeat tasks
     * @param heartbeatInterval the interval between heartbeat signals
     */
    public SseHeartbeatEmitter(Long timeout, ScheduledExecutorService scheduler, Duration heartbeatInterval) {
        super(timeout);
        this.scheduler = scheduler;
        this.heartbeatInterval = heartbeatInterval;
        startHeartbeat();

        // Stop heartbeat on completion
        onCompletion(this::stopHeartbeat);
        onTimeout(this::stopHeartbeat);
        onError(e -> stopHeartbeat());
    }

    private void startHeartbeat() {
        heartbeatTask = scheduler.scheduleAtFixedRate(
                this::sendHeartbeatIfIdle,
                heartbeatInterval.toMillis(),
                heartbeatInterval.toMillis(),
                TimeUnit.MILLISECONDS);
    }

    private void stopHeartbeat() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel(false);
        }
    }

    private void sendHeartbeatIfIdle() {
        long elapsed = System.currentTimeMillis() - lastActivityTime.get();
        if (elapsed >= heartbeatInterval.toMillis()) {
            try {
                super.send(SseEmitter.event().comment(HEARTBEAT_COMMENT));
            } catch (IOException e) {
                // Connection likely closed, stop heartbeat
                stopHeartbeat();
            }
        }
    }

    @Override
    public void send(SseEventBuilder builder) throws IOException {
        lastActivityTime.set(System.currentTimeMillis());
        super.send(builder);
    }

    @Override
    public void send(Object object) throws IOException {
        lastActivityTime.set(System.currentTimeMillis());
        super.send(object);
    }

    @Override
    public void send(Object object, MediaType mediaType) throws IOException {
        lastActivityTime.set(System.currentTimeMillis());
        super.send(object, mediaType);
    }
}
