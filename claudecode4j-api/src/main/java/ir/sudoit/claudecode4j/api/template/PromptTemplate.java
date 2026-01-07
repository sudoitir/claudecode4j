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
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Represents a compiled prompt template that can be rendered with variables.
 *
 * <p>Templates support variable substitution using the syntax {@code {{variableName}}}.
 */
public interface PromptTemplate {

    /** Returns the template name. */
    String name();

    /** Returns the raw template string. */
    String template();

    /** Returns the list of variable names used in this template. */
    List<String> variables();

    /** Returns the optional system prompt template. */
    @Nullable
    String systemPromptTemplate();

    /**
     * Renders the template with the given variables.
     *
     * @param variables map of variable name to value
     * @return the rendered prompt
     * @throws IllegalArgumentException if required variables are missing
     */
    Prompt render(Map<String, Object> variables);

    /**
     * Renders the template with the given variables and additional prompt settings.
     *
     * @param variables map of variable name to value
     * @param systemPrompt optional system prompt override
     * @return the rendered prompt
     */
    default Prompt render(Map<String, Object> variables, @Nullable String systemPrompt) {
        var rendered = render(variables);
        if (systemPrompt != null) {
            return Prompt.builder()
                    .text(rendered.text())
                    .systemPrompt(systemPrompt)
                    .contextFiles(rendered.contextFiles())
                    .workingDirectory(rendered.workingDirectory())
                    .agentName(rendered.agentName())
                    .build();
        }
        return rendered;
    }

    /**
     * Validates that all required variables are present.
     *
     * @param variables the variables to validate
     * @return list of missing variable names, empty if all present
     */
    default List<String> validateVariables(Map<String, Object> variables) {
        return variables().stream().filter(v -> !variables.containsKey(v)).toList();
    }
}
