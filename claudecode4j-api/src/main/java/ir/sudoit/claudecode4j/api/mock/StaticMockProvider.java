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
package ir.sudoit.claudecode4j.api.mock;

import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.api.model.request.PromptOptions;
import ir.sudoit.claudecode4j.api.model.response.ClaudeResponse;
import ir.sudoit.claudecode4j.api.model.response.TextResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A simple mock provider that returns a static response.
 *
 * <p>Useful for unit tests where you need predictable responses.
 */
public final class StaticMockProvider implements MockResponseProvider {

    private final String staticResponse;
    private final List<RecordedRequest> recordedRequests = new CopyOnWriteArrayList<>();

    public StaticMockProvider(String staticResponse) {
        this.staticResponse = staticResponse;
    }

    /** Creates a provider with a default response. */
    public static StaticMockProvider withDefault() {
        return new StaticMockProvider("Mock response from Claude");
    }

    /** Creates a provider with a custom response. */
    public static StaticMockProvider withResponse(String response) {
        return new StaticMockProvider(response);
    }

    @Override
    public ClaudeResponse getMockResponse(Prompt prompt, PromptOptions options) {
        return new TextResponse(staticResponse, Instant.now(), Duration.ofMillis(10), "mock-model", 100, null);
    }

    @Override
    public void recordRequest(Prompt prompt, PromptOptions options) {
        recordedRequests.add(new RecordedRequest(prompt, options, Instant.now()));
    }

    @Override
    public List<RecordedRequest> getRecordedRequests() {
        return Collections.unmodifiableList(new ArrayList<>(recordedRequests));
    }

    @Override
    public void reset() {
        recordedRequests.clear();
    }

    @Override
    public int priority() {
        return 10;
    }
}
