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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class PathBinaryResolver implements BinaryResolver {

    private static final List<String> COMMON_PATHS = List.of(
            "/usr/local/bin/claude",
            "/usr/bin/claude",
            "/opt/homebrew/bin/claude",
            System.getProperty("user.home") + "/.local/bin/claude",
            System.getProperty("user.home") + "/.npm-global/bin/claude",
            System.getProperty("user.home") + "/node_modules/.bin/claude");

    @Override
    public Optional<Path> resolve() {
        var pathEnv = System.getenv("PATH");
        if (pathEnv != null) {
            for (var dir : pathEnv.split(System.getProperty("path.separator"))) {
                var claudePath = Path.of(dir, "claude");
                if (Files.exists(claudePath) && Files.isExecutable(claudePath)) {
                    return Optional.of(claudePath);
                }
            }
        }

        for (var pathStr : COMMON_PATHS) {
            var path = Path.of(pathStr);
            if (Files.exists(path) && Files.isExecutable(path)) {
                return Optional.of(path);
            }
        }

        return Optional.empty();
    }

    @Override
    public int priority() {
        return 50;
    }

    @Override
    public String name() {
        return "PATH";
    }
}
