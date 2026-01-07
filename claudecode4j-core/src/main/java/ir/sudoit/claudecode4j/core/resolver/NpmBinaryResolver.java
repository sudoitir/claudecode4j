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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class NpmBinaryResolver implements BinaryResolver {

    @Override
    public Optional<Path> resolve() {
        try {
            var process = new ProcessBuilder("npm", "root", "-g")
                    .redirectErrorStream(true)
                    .start();

            String globalPath;
            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                globalPath = reader.readLine();
            }

            int exitCode = process.waitFor();
            if (exitCode != 0 || globalPath == null || globalPath.isBlank()) {
                return Optional.empty();
            }

            var claudePath = Path.of(globalPath.trim())
                    .resolve("@anthropic-ai")
                    .resolve("claude-code")
                    .resolve("cli.js");

            if (Files.exists(claudePath) && Files.isReadable(claudePath)) {
                return Optional.of(claudePath);
            }

            var binPath = Path.of(globalPath.trim()).getParent().resolve("bin").resolve("claude");

            if (Files.exists(binPath) && Files.isExecutable(binPath)) {
                return Optional.of(binPath);
            }

            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public boolean isApplicable() {
        try {
            var process = new ProcessBuilder("npm", "--version")
                    .redirectErrorStream(true)
                    .start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String name() {
        return "npm-global";
    }
}
