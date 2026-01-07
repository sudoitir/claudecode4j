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

import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.api.client.ClaudeClientFactory;
import ir.sudoit.claudecode4j.api.config.ClaudeConfig;
import ir.sudoit.claudecode4j.api.exception.ClaudeBinaryNotFoundException;
import ir.sudoit.claudecode4j.api.spi.BinaryResolver;
import ir.sudoit.claudecode4j.api.spi.InputSanitizer;
import ir.sudoit.claudecode4j.api.spi.OutputParser;
import ir.sudoit.claudecode4j.api.spi.ProcessExecutor;
import ir.sudoit.claudecode4j.core.parser.JacksonStreamParser;
import ir.sudoit.claudecode4j.core.process.VirtualThreadExecutor;
import ir.sudoit.claudecode4j.core.resolver.CompositeBinaryResolver;
import ir.sudoit.claudecode4j.core.security.DefaultInputSanitizer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.ServiceLoader;

public final class DefaultClaudeClientFactory implements ClaudeClientFactory {

    @Override
    public ClaudeClient createClient() {
        return createClient(ClaudeConfig.defaults());
    }

    @Override
    public ClaudeClient createClient(ClaudeConfig config) {
        var binaryPath = resolveBinaryPath(config);
        var sanitizer = loadSanitizer();
        var parser = loadParser();
        var executor = loadExecutor();

        return new DefaultClaudeClient(config, binaryPath, sanitizer, parser, executor);
    }

    @Override
    public int priority() {
        return 0;
    }

    private Path resolveBinaryPath(ClaudeConfig config) {
        if (config.binaryPath() != null) {
            return config.binaryPath();
        }

        var resolvers = new ArrayList<BinaryResolver>();
        ServiceLoader.load(BinaryResolver.class).forEach(resolvers::add);

        if (resolvers.isEmpty()) {
            resolvers.add(new CompositeBinaryResolver());
        }

        var searchedPaths = new ArrayList<String>();

        for (var resolver : resolvers.stream()
                .filter(BinaryResolver::isApplicable)
                .sorted((a, b) -> Integer.compare(b.priority(), a.priority()))
                .toList()) {
            var path = resolver.resolve();
            if (path.isPresent()) {
                return path.get();
            }
            searchedPaths.add(resolver.name());
        }

        throw new ClaudeBinaryNotFoundException(searchedPaths);
    }

    private InputSanitizer loadSanitizer() {
        return ServiceLoader.load(InputSanitizer.class).stream()
                .map(ServiceLoader.Provider::get)
                .max((a, b) -> Integer.compare(a.priority(), b.priority()))
                .orElseGet(DefaultInputSanitizer::new);
    }

    private OutputParser loadParser() {
        return ServiceLoader.load(OutputParser.class).findFirst().orElseGet(JacksonStreamParser::new);
    }

    private ProcessExecutor loadExecutor() {
        return ServiceLoader.load(ProcessExecutor.class).findFirst().orElseGet(VirtualThreadExecutor::new);
    }
}
