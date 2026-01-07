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
package ir.sudoit.claudecode4j.api.template;

import ir.sudoit.claudecode4j.api.model.request.Prompt;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/** Simple implementation of PromptTemplate using {{variable}} syntax. */
public final class SimplePromptTemplate implements PromptTemplate {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*(\\w+)\\s*}}");

    private final String name;
    private final String template;
    private final @Nullable String systemPromptTemplate;
    private final List<String> variables;

    public SimplePromptTemplate(String name, String template) {
        this(name, template, null);
    }

    public SimplePromptTemplate(String name, String template, @Nullable String systemPromptTemplate) {
        this.name = name;
        this.template = template;
        this.systemPromptTemplate = systemPromptTemplate;
        this.variables = extractVariables(template, systemPromptTemplate);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String template() {
        return template;
    }

    @Override
    public List<String> variables() {
        return variables;
    }

    @Override
    public @Nullable String systemPromptTemplate() {
        return systemPromptTemplate;
    }

    @Override
    public Prompt render(Map<String, Object> variables) {
        var missing = validateVariables(variables);
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Missing required variables: " + String.join(", ", missing));
        }

        var renderedText = substituteVariables(template, variables);
        var builder = Prompt.builder().text(renderedText);

        if (systemPromptTemplate != null) {
            builder.systemPrompt(substituteVariables(systemPromptTemplate, variables));
        }

        return builder.build();
    }

    private String substituteVariables(String text, Map<String, Object> variables) {
        var result = text;
        for (var entry : variables.entrySet()) {
            var pattern = "\\{\\{\\s*" + Pattern.quote(entry.getKey()) + "\\s*}}";
            var value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replaceAll(pattern, value);
        }
        return result;
    }

    private static List<String> extractVariables(String template, @Nullable String systemPromptTemplate) {
        var vars = new ArrayList<String>();
        extractVariablesFromText(template, vars);
        if (systemPromptTemplate != null) {
            extractVariablesFromText(systemPromptTemplate, vars);
        }
        return List.copyOf(vars);
    }

    private static void extractVariablesFromText(String text, List<String> vars) {
        var matcher = VARIABLE_PATTERN.matcher(text);
        while (matcher.find()) {
            var varName = matcher.group(1);
            if (!vars.contains(varName)) {
                vars.add(varName);
            }
        }
    }
}
