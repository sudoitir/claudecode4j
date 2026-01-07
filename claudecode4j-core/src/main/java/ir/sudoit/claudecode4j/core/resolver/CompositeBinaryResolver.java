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
package ir.sudoit.claudecode4j.core.resolver;

import ir.sudoit.claudecode4j.api.spi.BinaryResolver;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class CompositeBinaryResolver implements BinaryResolver {

    private final List<BinaryResolver> resolvers;

    public CompositeBinaryResolver() {
        this.resolvers = List.of(new NpmBinaryResolver(), new PathBinaryResolver());
    }

    public CompositeBinaryResolver(List<BinaryResolver> resolvers) {
        this.resolvers = List.copyOf(resolvers);
    }

    @Override
    public Optional<Path> resolve() {
        return resolvers.stream()
                .filter(BinaryResolver::isApplicable)
                .sorted((a, b) -> Integer.compare(b.priority(), a.priority()))
                .map(BinaryResolver::resolve)
                .flatMap(Optional::stream)
                .findFirst();
    }

    @Override
    public int priority() {
        return resolvers.stream().mapToInt(BinaryResolver::priority).max().orElse(0);
    }

    @Override
    public String name() {
        return "composite["
                + String.join(",", resolvers.stream().map(BinaryResolver::name).toList()) + "]";
    }
}
