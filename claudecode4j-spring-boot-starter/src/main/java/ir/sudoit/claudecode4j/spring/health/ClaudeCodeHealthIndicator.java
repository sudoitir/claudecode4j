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
package ir.sudoit.claudecode4j.spring.health;

import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.spring.properties.ClaudeCodeProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

public class ClaudeCodeHealthIndicator implements HealthIndicator {

    private final ClaudeClient claudeClient;
    private final Duration cacheTimeout;
    private final ReentrantLock lock = new ReentrantLock();

    private volatile Health cachedHealth;
    private volatile Instant lastCheck = Instant.EPOCH;

    public ClaudeCodeHealthIndicator(ClaudeClient claudeClient, ClaudeCodeProperties properties) {
        this.claudeClient = claudeClient;
        this.cacheTimeout = properties.getHealthTimeout();
    }

    @Override
    public Health health() {
        if (isCacheValid()) {
            return cachedHealth;
        }

        lock.lock();
        try {
            if (isCacheValid()) {
                return cachedHealth;
            }

            cachedHealth = checkHealth();
            lastCheck = Instant.now();
            return cachedHealth;
        } finally {
            lock.unlock();
        }
    }

    private boolean isCacheValid() {
        return cachedHealth != null
                && Duration.between(lastCheck, Instant.now()).compareTo(cacheTimeout) < 0;
    }

    private Health checkHealth() {
        try {
            if (claudeClient.isAvailable()) {
                var version = claudeClient.getCliVersion();
                return Health.up()
                        .withDetail("version", version)
                        .withDetail("available", true)
                        .build();
            } else {
                return Health.down()
                        .withDetail("available", false)
                        .withDetail("reason", "Claude CLI not responding")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("available", false)
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
