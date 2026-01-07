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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/** Resolves Claude CLI binary path using npm global installation. Supports macOS, Linux, and Windows platforms. */
public final class NpmBinaryResolver implements BinaryResolver {

    private static final System.Logger log = System.getLogger(NpmBinaryResolver.class.getName());

    private static final String OS_NAME = System.getProperty("os.name", "").toLowerCase();
    private static final boolean IS_WINDOWS = OS_NAME.contains("win");
    private static final boolean IS_MAC = OS_NAME.contains("mac");

    private static final long NPM_COMMAND_TIMEOUT_SECONDS = 10;
    private static final String CLAUDE_PACKAGE_PATH = "@anthropic-ai/claude-code";

    private static final List<String> WINDOWS_EXTENSIONS = List.of(".cmd", ".exe", ".bat", "");
    private static final List<String> UNIX_EXTENSIONS = List.of("");

    @Override
    public Optional<Path> resolve() {
        log.log(System.Logger.Level.DEBUG, "Starting npm binary resolution for platform: {0}", OS_NAME);

        try {
            // Step 1: Get npm global root path
            var npmRoot = getNpmGlobalRoot();
            if (npmRoot.isEmpty()) {
                log.log(System.Logger.Level.DEBUG, "Failed to get npm global root");
                return Optional.empty();
            }

            log.log(System.Logger.Level.DEBUG, "npm root: {0}", npmRoot.get());

            // Step 2: Try to find the claude binary in bin directories
            var binPath = findClaudeBinary(npmRoot.get());
            if (binPath.isPresent()) {
                log.log(System.Logger.Level.INFO, "Found claude binary: {0}", binPath.get());
                return binPath;
            }

            // Step 3: Fallback - check for cli.js (not recommended, may not work)
            var fallbackPath = findCliJsFallback(npmRoot.get());
            if (fallbackPath.isPresent()) {
                log.log(
                        System.Logger.Level.WARNING,
                        "Using cli.js fallback. This may not work correctly: {0}",
                        fallbackPath.get());
                return fallbackPath;
            }

            log.log(System.Logger.Level.DEBUG, "Claude binary not found via npm");
            return Optional.empty();

        } catch (Exception e) {
            log.log(System.Logger.Level.WARNING, "Error during npm binary resolution: {0}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<Path> getNpmGlobalRoot() {
        try {
            var processBuilder = new ProcessBuilder(getNpmCommand(), "root", "-g").redirectErrorStream(true);

            var process = processBuilder.start();

            String globalPath;
            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                globalPath = reader.readLine();
            }

            boolean completed = process.waitFor(NPM_COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                log.log(System.Logger.Level.WARNING, "npm root command timed out");
                return Optional.empty();
            }

            int exitCode = process.exitValue();
            if (exitCode != 0 || globalPath == null || globalPath.isBlank()) {
                log.log(System.Logger.Level.DEBUG, "npm root returned exit code {0}", exitCode);
                return Optional.empty();
            }

            return Optional.of(Path.of(globalPath.trim()));

        } catch (Exception e) {
            log.log(System.Logger.Level.DEBUG, "Failed to execute npm root: {0}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<Path> findClaudeBinary(Path npmRoot) {
        var possibleBinDirs = buildBinDirectoryList(npmRoot);
        var extensions = IS_WINDOWS ? WINDOWS_EXTENSIONS : UNIX_EXTENSIONS;

        for (var binDir : possibleBinDirs) {
            if (binDir == null || !Files.isDirectory(binDir)) {
                continue;
            }

            for (var ext : extensions) {
                var binPath = binDir.resolve("claude" + ext);
                if (isValidExecutable(binPath)) {
                    return Optional.of(binPath);
                }
            }
        }

        return Optional.empty();
    }

    private List<Path> buildBinDirectoryList(Path npmRoot) {
        var binDirs = new ArrayList<Path>();

        // Standard npm structure:
        // npmRoot = <prefix>/lib/node_modules
        // bin     = <prefix>/bin
        var parent = npmRoot.getParent(); // <prefix>/lib
        if (parent != null) {
            var grandparent = parent.getParent(); // <prefix>
            if (grandparent != null) {
                binDirs.add(grandparent.resolve("bin")); // Most common: /opt/homebrew/bin, /usr/local/bin
            }
            // Less common but possible
            binDirs.add(parent.resolve("bin")); // <prefix>/lib/bin
        }

        // Local node_modules/.bin (for local installs)
        binDirs.add(npmRoot.resolve(".bin"));

        // Windows-specific: npm prefix might be AppData
        if (IS_WINDOWS) {
            var appData = System.getenv("APPDATA");
            if (appData != null && !appData.isBlank()) {
                binDirs.add(Path.of(appData, "npm"));
            }
            var localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData != null && !localAppData.isBlank()) {
                binDirs.add(Path.of(localAppData, "npm"));
            }
        }

        return binDirs;
    }

    private Optional<Path> findCliJsFallback(Path npmRoot) {
        var claudePath = npmRoot.resolve("@anthropic-ai").resolve("claude-code").resolve("cli.js");

        if (Files.exists(claudePath) && Files.isReadable(claudePath)) {
            return Optional.of(claudePath);
        }

        return Optional.empty();
    }

    private boolean isValidExecutable(Path path) {
        if (path == null) {
            return false;
        }
        try {
            return Files.exists(path) && Files.isExecutable(path);
        } catch (SecurityException e) {
            log.log(System.Logger.Level.DEBUG, "Security exception checking path: {0}", path);
            return false;
        }
    }

    private String getNpmCommand() {
        // On Windows, npm is typically npm.cmd
        // ProcessBuilder can usually find it via PATH, but we provide the extension explicitly
        return IS_WINDOWS ? "npm.cmd" : "npm";
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public boolean isApplicable() {
        try {
            var process = new ProcessBuilder(getNpmCommand(), "--version")
                    .redirectErrorStream(true)
                    .start();

            boolean completed = process.waitFor(NPM_COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                return false;
            }

            return process.exitValue() == 0;
        } catch (Exception e) {
            log.log(System.Logger.Level.DEBUG, "npm not available: {0}", e.getMessage());
            return false;
        }
    }

    @Override
    public String name() {
        return "npm-global";
    }
}
