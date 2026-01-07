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
import ir.sudoit.claudecode4j.api.model.response.StreamEvent;
import java.util.List;
import java.util.stream.Stream;

/**
 * SPI for providing mock responses in test environments.
 *
 * <p>Implementations can provide static responses, pattern-matched responses, or record and playback real interactions.
 */
public interface MockResponseProvider {

    /**
     * Returns a mock response for the given prompt and options.
     *
     * @param prompt the prompt to respond to
     * @param options the prompt options
     * @return a mock response
     */
    ClaudeResponse getMockResponse(Prompt prompt, PromptOptions options);

    /**
     * Returns a stream of mock events for streaming mode.
     *
     * @param prompt the prompt to respond to
     * @param options the prompt options
     * @return a stream of mock events
     */
    default Stream<StreamEvent> getMockStream(Prompt prompt, PromptOptions options) {
        var response = getMockResponse(prompt, options);
        return Stream.of(StreamEvent.of(StreamEvent.EventType.RESULT, response.content(), 1));
    }

    /**
     * Records a request for later verification.
     *
     * @param prompt the prompt that was sent
     * @param options the options that were used
     */
    default void recordRequest(Prompt prompt, PromptOptions options) {
        // Default: no recording
    }

    /**
     * Returns all recorded requests.
     *
     * @return list of recorded requests
     */
    default List<RecordedRequest> getRecordedRequests() {
        return List.of();
    }

    /** Resets the provider state (clears recordings, etc.). */
    default void reset() {
        // Default: no-op
    }

    /** Priority for SPI selection (higher = preferred). */
    default int priority() {
        return 0;
    }

    /** A recorded request for verification in tests. */
    record RecordedRequest(Prompt prompt, PromptOptions options, java.time.Instant timestamp) {}
}
