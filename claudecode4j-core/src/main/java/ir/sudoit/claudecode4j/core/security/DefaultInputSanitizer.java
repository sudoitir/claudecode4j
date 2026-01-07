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
package ir.sudoit.claudecode4j.core.security;

import ir.sudoit.claudecode4j.api.model.request.Prompt;
import ir.sudoit.claudecode4j.api.spi.InputSanitizer;
import java.util.Set;
import java.util.regex.Pattern;

public final class DefaultInputSanitizer implements InputSanitizer {

    private static final int MAX_LENGTH = 256 * 1024;
    private static final Set<Character> DANGEROUS_CHARS = Set.of('`', '\0');
    private static final Pattern COMMAND_SUBSTITUTION = Pattern.compile("\\$\\([^)]*\\)");
    private static final Pattern BACKTICK_SUBSTITUTION = Pattern.compile("`[^`]*`");

    @Override
    public Prompt sanitize(Prompt prompt) throws SecurityException {
        validate(prompt.text(), "prompt text");
        if (prompt.systemPrompt() != null) {
            validate(prompt.systemPrompt(), "system prompt");
        }
        if (prompt.agentName() != null) {
            validateArgument(prompt.agentName(), "agent name");
        }
        return prompt;
    }

    @Override
    public boolean isValidArgument(String value) {
        if (value == null || value.length() > MAX_LENGTH) {
            return false;
        }
        for (char c : value.toCharArray()) {
            if (DANGEROUS_CHARS.contains(c)) {
                return false;
            }
        }
        return !containsCommandSubstitution(value);
    }

    private void validate(String text, String fieldName) {
        if (text.length() > MAX_LENGTH) {
            throw new SecurityException(fieldName + " exceeds maximum length of " + MAX_LENGTH);
        }
        for (char c : text.toCharArray()) {
            if (c == '\0') {
                throw new SecurityException("Null character detected in " + fieldName);
            }
        }
    }

    private void validateArgument(String value, String fieldName) {
        if (value.length() > 256) {
            throw new SecurityException(fieldName + " exceeds maximum length of 256");
        }
        for (char c : value.toCharArray()) {
            if (DANGEROUS_CHARS.contains(c)) {
                throw new SecurityException("Dangerous character '" + c + "' detected in " + fieldName);
            }
        }
        if (containsCommandSubstitution(value)) {
            throw new SecurityException("Command substitution pattern detected in " + fieldName);
        }
    }

    private boolean containsCommandSubstitution(String value) {
        return COMMAND_SUBSTITUTION.matcher(value).find()
                || BACKTICK_SUBSTITUTION.matcher(value).find();
    }
}
