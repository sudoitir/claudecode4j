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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Resolves Claude CLI binary path by searching the system PATH. Supports macOS, Linux, and Windows platforms. */
public final class PathBinaryResolver implements BinaryResolver {

    private static final System.Logger log = System.getLogger(PathBinaryResolver.class.getName());

    private static final String OS_NAME = System.getProperty("os.name", "").toLowerCase();
    private static final boolean IS_WINDOWS = OS_NAME.contains("win");
    private static final String USER_HOME = System.getProperty("user.home", "");

    private static final List<String> WINDOWS_EXTENSIONS = List.of(".cmd", ".exe", ".bat", "");
    private static final List<String> UNIX_EXTENSIONS = List.of("");

    @Override
    public Optional<Path> resolve() {
        log.log(System.Logger.Level.DEBUG, "Starting PATH binary resolution for platform: {0}", OS_NAME);

        // Step 1: Search in PATH environment variable
        var pathResult = searchInPath();
        if (pathResult.isPresent()) {
            log.log(System.Logger.Level.INFO, "Found claude in PATH: {0}", pathResult.get());
            return pathResult;
        }

        // Step 2: Check common installation locations
        var commonResult = searchCommonLocations();
        if (commonResult.isPresent()) {
            log.log(System.Logger.Level.INFO, "Found claude in common location: {0}", commonResult.get());
            return commonResult;
        }

        log.log(System.Logger.Level.DEBUG, "Claude binary not found in PATH or common locations");
        return Optional.empty();
    }

    private Optional<Path> searchInPath() {
        var pathEnv = System.getenv("PATH");
        if (pathEnv == null || pathEnv.isBlank()) {
            log.log(System.Logger.Level.DEBUG, "PATH environment variable is empty");
            return Optional.empty();
        }

        var pathSeparator = System.getProperty("path.separator", ":");
        var extensions = IS_WINDOWS ? WINDOWS_EXTENSIONS : UNIX_EXTENSIONS;

        for (var dir : pathEnv.split(pathSeparator)) {
            if (dir == null || dir.isBlank()) {
                continue;
            }

            try {
                var dirPath = Path.of(dir);
                if (!Files.isDirectory(dirPath)) {
                    continue;
                }

                for (var ext : extensions) {
                    var claudePath = dirPath.resolve("claude" + ext);
                    if (isValidExecutable(claudePath)) {
                        return Optional.of(claudePath);
                    }
                }
            } catch (Exception e) {
                log.log(System.Logger.Level.DEBUG, "Error checking PATH entry: {0}", dir);
            }
        }

        return Optional.empty();
    }

    private Optional<Path> searchCommonLocations() {
        var commonPaths = buildCommonPathsList();
        var extensions = IS_WINDOWS ? WINDOWS_EXTENSIONS : UNIX_EXTENSIONS;

        for (var pathStr : commonPaths) {
            if (pathStr == null || pathStr.isBlank()) {
                continue;
            }

            try {
                for (var ext : extensions) {
                    var fullPath = pathStr.endsWith(ext) ? pathStr : pathStr + ext;
                    var claudePath = Path.of(fullPath);
                    if (isValidExecutable(claudePath)) {
                        return Optional.of(claudePath);
                    }
                }
            } catch (Exception e) {
                log.log(System.Logger.Level.DEBUG, "Error checking common path: {0}", pathStr);
            }
        }

        return Optional.empty();
    }

    private List<String> buildCommonPathsList() {
        var paths = new ArrayList<String>();

        if (IS_WINDOWS) {
            // Windows common locations
            var appData = System.getenv("APPDATA");
            var localAppData = System.getenv("LOCALAPPDATA");
            var programFiles = System.getenv("PROGRAMFILES");
            var programFilesX86 = System.getenv("PROGRAMFILES(X86)");

            if (appData != null && !appData.isBlank()) {
                paths.add(appData + "\\npm\\claude.cmd");
                paths.add(appData + "\\npm\\claude.exe");
            }
            if (localAppData != null && !localAppData.isBlank()) {
                paths.add(localAppData + "\\npm\\claude.cmd");
                paths.add(localAppData + "\\npm\\claude.exe");
            }
            if (!USER_HOME.isBlank()) {
                paths.add(USER_HOME + "\\AppData\\Roaming\\npm\\claude.cmd");
                paths.add(USER_HOME + "\\.npm-global\\claude.cmd");
            }
            if (programFiles != null && !programFiles.isBlank()) {
                paths.add(programFiles + "\\nodejs\\claude.cmd");
            }
        } else {
            // Unix/macOS common locations
            paths.add("/usr/local/bin/claude");
            paths.add("/usr/bin/claude");
            paths.add("/opt/homebrew/bin/claude"); // macOS Homebrew (Apple Silicon)
            paths.add("/usr/local/Homebrew/bin/claude"); // macOS Homebrew (Intel)
            paths.add("/home/linuxbrew/.linuxbrew/bin/claude"); // Linuxbrew

            if (!USER_HOME.isBlank()) {
                paths.add(USER_HOME + "/.local/bin/claude");
                paths.add(USER_HOME + "/.npm-global/bin/claude");
                paths.add(USER_HOME + "/node_modules/.bin/claude");
                paths.add(USER_HOME + "/.nvm/versions/node/default/bin/claude"); // nvm
                paths.add(USER_HOME + "/.volta/bin/claude"); // Volta
                paths.add(USER_HOME + "/.asdf/shims/claude"); // asdf
                paths.add(USER_HOME + "/.fnm/aliases/default/bin/claude"); // fnm
            }
        }

        return paths;
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

    @Override
    public int priority() {
        return 50;
    }

    @Override
    public String name() {
        return "PATH";
    }
}
