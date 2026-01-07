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
package ir.sudoit.claudecode4j.rest.dto;

import ir.sudoit.claudecode4j.api.model.response.ClaudeResponse;
import ir.sudoit.claudecode4j.api.model.response.ErrorResponse;
import ir.sudoit.claudecode4j.api.model.response.StreamResponse;
import ir.sudoit.claudecode4j.api.model.response.TextResponse;
import java.time.Instant;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record PromptResponse(
        String content,
        boolean success,
        Instant timestamp,
        long durationMillis,
        @Nullable String model,
        @Nullable Integer tokensUsed,
        @Nullable String errorCode,
        @Nullable String errorDetails,
        @Nullable List<EventDto> events) {
    public record EventDto(String type, String content, Instant timestamp, long sequenceNumber) {}

    public static PromptResponse from(ClaudeResponse response) {
        return switch (response) {
            case TextResponse text ->
                new PromptResponse(
                        text.content(),
                        true,
                        text.timestamp(),
                        text.duration().toMillis(),
                        text.model(),
                        text.tokensUsed(),
                        null,
                        null,
                        null);
            case StreamResponse stream ->
                new PromptResponse(
                        stream.content(),
                        true,
                        stream.timestamp(),
                        stream.duration().toMillis(),
                        stream.model(),
                        stream.tokensUsed(),
                        null,
                        null,
                        stream.events().stream()
                                .map(e -> new EventDto(e.type().name(), e.content(), e.timestamp(), e.sequenceNumber()))
                                .toList());
            case ErrorResponse error ->
                new PromptResponse(
                        error.content(),
                        false,
                        error.timestamp(),
                        error.duration().toMillis(),
                        null,
                        null,
                        error.errorCode(),
                        error.errorDetails(),
                        null);
        };
    }
}
