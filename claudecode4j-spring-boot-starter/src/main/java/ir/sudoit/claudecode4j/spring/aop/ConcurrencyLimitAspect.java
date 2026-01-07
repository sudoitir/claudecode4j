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
package ir.sudoit.claudecode4j.spring.aop;

import ir.sudoit.claudecode4j.api.annotation.ConcurrencyLimit;
import ir.sudoit.claudecode4j.spring.properties.ClaudeCodeProperties;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.jspecify.annotations.Nullable;
import org.springframework.core.env.Environment;

@Aspect
public class ConcurrencyLimitAspect {

    private final Environment environment;
    private final ClaudeCodeProperties properties;
    private final Map<String, Semaphore> semaphores = new ConcurrentHashMap<>();

    public ConcurrencyLimitAspect(Environment environment, ClaudeCodeProperties properties) {
        this.environment = environment;
        this.properties = properties;
    }

    @Around("@annotation(concurrencyLimit)")
    public @Nullable Object limitConcurrency(ProceedingJoinPoint joinPoint, ConcurrencyLimit concurrencyLimit)
            throws Throwable {
        var semaphore = getSemaphore(concurrencyLimit);
        semaphore.acquire();
        try {
            return joinPoint.proceed();
        } finally {
            semaphore.release();
        }
    }

    @Around("@within(concurrencyLimit)")
    public @Nullable Object limitConcurrencyClass(ProceedingJoinPoint joinPoint, ConcurrencyLimit concurrencyLimit)
            throws Throwable {
        var semaphore = getSemaphore(concurrencyLimit);
        semaphore.acquire();
        try {
            return joinPoint.proceed();
        } finally {
            semaphore.release();
        }
    }

    private Semaphore getSemaphore(ConcurrencyLimit annotation) {
        var key = annotation.configKey();
        return semaphores.computeIfAbsent(key, k -> {
            int limit = resolveLimit(annotation);
            return new Semaphore(limit, true);
        });
    }

    private int resolveLimit(ConcurrencyLimit annotation) {
        if (annotation.value() > 0) {
            return annotation.value();
        }

        if (annotation.useConfig()) {
            var configValue = environment.getProperty(annotation.configKey(), Integer.class);
            if (configValue != null && configValue > 0) {
                return configValue;
            }
        }

        return properties.concurrencyLimit();
    }
}
