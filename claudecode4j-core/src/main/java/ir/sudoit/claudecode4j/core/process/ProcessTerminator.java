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
 * <p>Attempts graceful shutdown (SIGTERM) before forcing termination (SIGKILL).
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
     * Terminates the process gracefully.
     *
     * <p>First attempts graceful termination (SIGTERM on Unix), waits for the grace period, then forces termination
     * (SIGKILL) if the process is still alive.
     *
     * @param process the process to terminate
     * @param gracePeriod the time to wait for graceful shutdown before force killing
     */
    public static void terminate(Process process, Duration gracePeriod) {
        if (process == null || !process.isAlive()) {
            return;
        }

        log.log(System.Logger.Level.DEBUG, "Attempting graceful termination of process (PID: {0})", process.pid());

        // First, try graceful termination (SIGTERM on Unix)
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
                // Force kill if graceful shutdown failed
                process.destroyForcibly();
                // Wait briefly for force kill to complete
                process.waitFor(1, TimeUnit.SECONDS);
            } else {
                log.log(System.Logger.Level.DEBUG, "Process (PID: {0}) terminated gracefully", process.pid());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.log(System.Logger.Level.WARNING, "Interrupted while waiting for process termination, forcing kill");
            // Force kill on interrupt
            process.destroyForcibly();
        }
    }
}
