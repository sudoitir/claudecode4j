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
package ir.sudoit.claudecode4j.core.process;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Utility for graceful process termination.
 *
 * <p>Attempts graceful shutdown (SIGTERM) before forcing termination (SIGKILL). Also handles descendant processes to
 * prevent orphaned child processes.
 */
public final class ProcessTerminator {

    private static final System.Logger log = System.getLogger(ProcessTerminator.class.getName());
    private static final Duration DEFAULT_GRACE_PERIOD = Duration.ofSeconds(5);

    private ProcessTerminator() {}

    /**
     * Terminates the process gracefully with default 5-second grace period.
     *
     * @param process the process to terminate
     */
    public static void terminate(Process process) {
        terminate(process, DEFAULT_GRACE_PERIOD);
    }

    /**
     * Terminates the process and all its descendants gracefully.
     *
     * <p>First attempts graceful termination (SIGTERM on Unix), waits for the grace period, then forces termination
     * (SIGKILL) if the process is still alive. Descendant processes are terminated before the main process.
     *
     * @param process the process to terminate
     * @param gracePeriod the time to wait for graceful shutdown before force killing
     */
    public static void terminate(Process process, Duration gracePeriod) {
        if (process == null || !process.isAlive()) {
            return;
        }

        log.log(
                System.Logger.Level.DEBUG,
                "Attempting graceful termination of process (PID: {0}) and descendants",
                process.pid());

        // First, gracefully terminate all descendant processes
        destroyDescendants(process, false);

        // Then, try graceful termination of main process (SIGTERM on Unix)
        process.destroy();

        try {
            // Wait for graceful shutdown
            boolean exited = process.waitFor(gracePeriod.toMillis(), TimeUnit.MILLISECONDS);
            if (!exited) {
                log.log(
                        System.Logger.Level.WARNING,
                        "Process (PID: {0}) did not exit gracefully within {1}s, forcing termination",
                        process.pid(),
                        gracePeriod.toSeconds());

                // Force kill all remaining descendants
                destroyDescendants(process, true);

                // Force kill main process
                process.destroyForcibly();

                // Wait briefly for force kill to complete
                process.waitFor(1, TimeUnit.SECONDS);
            } else {
                log.log(System.Logger.Level.DEBUG, "Process (PID: {0}) terminated gracefully", process.pid());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.log(System.Logger.Level.WARNING, "Interrupted while waiting for process termination, forcing kill");
            // Force kill everything on interrupt
            destroyDescendants(process, true);
            process.destroyForcibly();
        }
    }

    /**
     * Destroys all descendant processes.
     *
     * @param process the parent process
     * @param forcibly if true, uses destroyForcibly(); otherwise uses destroy()
     */
    private static void destroyDescendants(Process process, boolean forcibly) {
        process.toHandle().descendants().forEach(descendant -> {
            if (descendant.isAlive()) {
                log.log(
                        System.Logger.Level.DEBUG,
                        "Terminating descendant process (PID: {0}){1}",
                        descendant.pid(),
                        forcibly ? " forcibly" : "");
                if (forcibly) {
                    descendant.destroyForcibly();
                } else {
                    descendant.destroy();
                }
            }
        });
    }
}
