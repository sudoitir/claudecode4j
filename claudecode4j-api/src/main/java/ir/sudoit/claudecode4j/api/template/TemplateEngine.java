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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * SPI for compiling and managing prompt templates.
 *
 * <p>Implementations can provide different template syntaxes or storage mechanisms.
 */
public interface TemplateEngine {

    /**
     * Compiles a template string into a PromptTemplate.
     *
     * @param name the template name
     * @param template the template string
     * @return the compiled template
     */
    PromptTemplate compile(String name, String template);

    /**
     * Compiles a template with a system prompt.
     *
     * @param name the template name
     * @param template the template string
     * @param systemPrompt the system prompt template
     * @return the compiled template
     */
    PromptTemplate compile(String name, String template, String systemPrompt);

    /**
     * Loads a template from a file.
     *
     * @param path the file path
     * @return the compiled template
     * @throws IOException if the file cannot be read
     */
    PromptTemplate load(Path path) throws IOException;

    /**
     * Loads all templates from a directory.
     *
     * @param directory the directory path
     * @return list of compiled templates
     * @throws IOException if the directory cannot be read
     */
    List<PromptTemplate> loadFromDirectory(Path directory) throws IOException;

    /**
     * Registers a template in the engine's cache.
     *
     * @param template the template to register
     */
    void register(PromptTemplate template);

    /**
     * Gets a template by name from the cache.
     *
     * @param name the template name
     * @return the template if found
     */
    Optional<PromptTemplate> get(String name);

    /**
     * Returns all registered templates.
     *
     * @return list of all templates
     */
    List<PromptTemplate> getAll();

    /** Clears all registered templates. */
    void clear();

    /** Priority for SPI selection (higher = preferred). */
    default int priority() {
        return 0;
    }
}
