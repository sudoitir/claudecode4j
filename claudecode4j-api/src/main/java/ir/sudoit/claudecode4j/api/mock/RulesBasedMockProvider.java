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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * A mock provider that matches prompts against rules and returns corresponding responses.
 *
 * <p>Rules are evaluated in order; the first matching rule's response is returned.
 */
public final class RulesBasedMockProvider implements MockResponseProvider {

    private final List<Rule> rules = new CopyOnWriteArrayList<>();
    private final List<RecordedRequest> recordedRequests = new CopyOnWriteArrayList<>();
    private final String defaultResponse;

    public RulesBasedMockProvider() {
        this("Default mock response");
    }

    public RulesBasedMockProvider(String defaultResponse) {
        this.defaultResponse = defaultResponse;
    }

    /** Adds a rule that matches prompts containing the given substring. */
    public RulesBasedMockProvider whenContains(String substring, String response) {
        rules.add(new Rule(prompt -> prompt.text().contains(substring), _ -> response));
        return this;
    }

    /** Adds a rule that matches prompts matching the given regex pattern. */
    public RulesBasedMockProvider whenMatches(String regex, String response) {
        var pattern = Pattern.compile(regex);
        rules.add(new Rule(prompt -> pattern.matcher(prompt.text()).find(), _ -> response));
        return this;
    }

    /** Adds a rule that matches prompts matching the given regex and uses a dynamic response. */
    public RulesBasedMockProvider whenMatches(String regex, Function<Prompt, String> responseGenerator) {
        var pattern = Pattern.compile(regex);
        rules.add(new Rule(prompt -> pattern.matcher(prompt.text()).find(), responseGenerator));
        return this;
    }

    /** Adds a rule with a custom predicate. */
    public RulesBasedMockProvider when(Predicate<Prompt> matcher, String response) {
        rules.add(new Rule(matcher, _ -> response));
        return this;
    }

    /** Adds a rule with a custom predicate and dynamic response. */
    public RulesBasedMockProvider when(Predicate<Prompt> matcher, Function<Prompt, String> responseGenerator) {
        rules.add(new Rule(matcher, responseGenerator));
        return this;
    }

    @Override
    public ClaudeResponse getMockResponse(Prompt prompt, PromptOptions options) {
        for (var rule : rules) {
            if (rule.matcher().test(prompt)) {
                var response = rule.responseGenerator().apply(prompt);
                return new TextResponse(response, Instant.now(), Duration.ofMillis(10), "mock-model", 100, null);
            }
        }
        return new TextResponse(defaultResponse, Instant.now(), Duration.ofMillis(10), "mock-model", 100, null);
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

    public void clearRules() {
        rules.clear();
    }

    @Override
    public int priority() {
        return 20;
    }

    private record Rule(Predicate<Prompt> matcher, Function<Prompt, String> responseGenerator) {}
}
