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
package ir.sudoit.claudecode4j.spring.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ClaudeCodeMetrics {

    private final AtomicInteger activeRequests = new AtomicInteger(0);
    private final AtomicInteger queuedRequests = new AtomicInteger(0);

    private final Timer executionTimer;
    private final Counter successCounter;
    private final Counter failureCounter;
    private final Counter timeoutCounter;

    public ClaudeCodeMetrics(MeterRegistry registry, String prefix) {
        this.executionTimer = Timer.builder(prefix + ".execution.duration")
                .description("Duration of Claude CLI executions")
                .publishPercentileHistogram()
                .register(registry);

        this.successCounter = Counter.builder(prefix + ".executions.success")
                .description("Number of successful executions")
                .register(registry);

        this.failureCounter = Counter.builder(prefix + ".executions.failure")
                .description("Number of failed executions")
                .register(registry);

        this.timeoutCounter = Counter.builder(prefix + ".executions.timeout")
                .description("Number of timed out executions")
                .register(registry);

        Gauge.builder(prefix + ".requests.active", activeRequests, AtomicInteger::get)
                .description("Number of currently active requests")
                .register(registry);

        Gauge.builder(prefix + ".requests.queued", queuedRequests, AtomicInteger::get)
                .description("Number of queued requests waiting for execution")
                .register(registry);
    }

    public void incrementActive() {
        activeRequests.incrementAndGet();
    }

    public void decrementActive() {
        activeRequests.decrementAndGet();
    }

    public void incrementQueued() {
        queuedRequests.incrementAndGet();
    }

    public void decrementQueued() {
        queuedRequests.decrementAndGet();
    }

    public void recordSuccess() {
        successCounter.increment();
    }

    public void recordFailure() {
        failureCounter.increment();
    }

    public void recordTimeout() {
        timeoutCounter.increment();
    }

    public <T> T recordExecution(Supplier<T> execution) {
        return executionTimer.record(execution);
    }

    public Timer.Sample startTimer() {
        return Timer.start();
    }

    public void stopTimer(Timer.Sample sample) {
        sample.stop(executionTimer);
    }

    public int getActiveRequests() {
        return activeRequests.get();
    }

    public int getQueuedRequests() {
        return queuedRequests.get();
    }
}
