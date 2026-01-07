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

import ir.sudoit.claudecode4j.api.client.ClaudeSession;
import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.api.model.request.PromptOptions;
import ir.sudoit.claudecode4j.api.model.response.ClaudeResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import org.jspecify.annotations.Nullable;

public final class DefaultClaudeSession implements ClaudeSession {

    private final DefaultClaudeClient client;
    private final String sessionId;
    private final List<ClaudeResponse> conversationHistory;
    private final ReentrantLock lock = new ReentrantLock();
    private @Nullable String systemPrompt;
    private volatile boolean closed = false;

    DefaultClaudeSession(DefaultClaudeClient client) {
        this.client = client;
        this.sessionId = UUID.randomUUID().toString();
        this.conversationHistory = new ArrayList<>();
    }

    @Override
    public String sessionId() {
        return sessionId;
    }

    @Override
    public ClaudeResponse send(String message) {
        return send(message, PromptOptions.defaults());
    }

    @Override
    public ClaudeResponse send(String message, PromptOptions options) {
        ensureOpen();
        var prompt = Prompt.builder().text(message).systemPrompt(systemPrompt).build();

        var response = client.execute(prompt, options);

        lock.lock();
        try {
            conversationHistory.add(response);
        } finally {
            lock.unlock();
        }

        return response;
    }

    @Override
    public CompletableFuture<ClaudeResponse> sendAsync(String message) {
        return CompletableFuture.supplyAsync(
                () -> send(message), runnable -> Thread.ofVirtual().start(runnable));
    }

    @Override
    public List<ClaudeResponse> history() {
        lock.lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(conversationHistory));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clearHistory() {
        lock.lock();
        try {
            conversationHistory.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public @Nullable String getSystemPrompt() {
        return systemPrompt;
    }

    @Override
    public void setSystemPrompt(@Nullable String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    @Override
    public void close() {
        closed = true;
        clearHistory();
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Session is closed");
        }
    }
}
