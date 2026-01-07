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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Simple template engine using {{variable}} syntax.
 *
 * <p>Templates can be loaded from files with .prompt extension. File format:
 *
 * <pre>
 * ---
 * system: You are a helpful assistant.
 * ---
 * {{userPrompt}}
 * </pre>
 */
public final class SimpleTemplateEngine implements TemplateEngine {

    private final Map<String, PromptTemplate> templates = new ConcurrentHashMap<>();

    @Override
    public PromptTemplate compile(String name, String template) {
        return new SimplePromptTemplate(name, template);
    }

    @Override
    public PromptTemplate compile(String name, String template, String systemPrompt) {
        return new SimplePromptTemplate(name, template, systemPrompt);
    }

    @Override
    public PromptTemplate load(Path path) throws IOException {
        var content = Files.readString(path);
        var name = extractName(path);
        return parseTemplate(name, content);
    }

    @Override
    public List<PromptTemplate> loadFromDirectory(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            throw new IOException("Path is not a directory: " + directory);
        }

        var result = new ArrayList<PromptTemplate>();
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".prompt"))
                    .forEach(p -> {
                        try {
                            result.add(load(p));
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to load template: " + p, e);
                        }
                    });
        }
        return result;
    }

    @Override
    public void register(PromptTemplate template) {
        templates.put(template.name(), template);
    }

    @Override
    public Optional<PromptTemplate> get(String name) {
        return Optional.ofNullable(templates.get(name));
    }

    @Override
    public List<PromptTemplate> getAll() {
        return new ArrayList<>(templates.values());
    }

    @Override
    public void clear() {
        templates.clear();
    }

    @Override
    public int priority() {
        return 10;
    }

    private String extractName(Path path) {
        var fileName = path.getFileName().toString();
        var dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    private PromptTemplate parseTemplate(String name, String content) {
        // Check for frontmatter (YAML-like header)
        if (content.startsWith("---")) {
            var endIndex = content.indexOf("---", 3);
            if (endIndex > 0) {
                var frontmatter = content.substring(3, endIndex).trim();
                var body = content.substring(endIndex + 3).trim();
                var systemPrompt = extractSystemPrompt(frontmatter);
                return new SimplePromptTemplate(name, body, systemPrompt);
            }
        }
        return new SimplePromptTemplate(name, content.trim());
    }

    private String extractSystemPrompt(String frontmatter) {
        for (var line : frontmatter.split("\n")) {
            var trimmed = line.trim();
            if (trimmed.startsWith("system:")) {
                return trimmed.substring(7).trim();
            }
        }
        return null;
    }
}
