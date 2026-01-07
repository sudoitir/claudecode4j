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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

@DisplayName("ProcessTerminator")
class ProcessTerminatorTest {

    @Nested
    @DisplayName("terminate with default grace period")
    class TerminateWithDefaultGracePeriod {

        @Test
        @DisplayName("should handle null process gracefully")
        void shouldHandleNullProcessGracefully() {
            assertThatCode(() -> ProcessTerminator.terminate(null)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should not terminate already dead process")
        void shouldNotTerminateAlreadyDeadProcess() {
            var process = mock(Process.class);
            when(process.isAlive()).thenReturn(false);

            ProcessTerminator.terminate(process);

            verify(process, never()).destroy();
            verify(process, never()).destroyForcibly();
        }

        @Test
        @DisplayName("should terminate running process gracefully")
        @DisabledOnOs(OS.WINDOWS)
        void shouldTerminateRunningProcessGracefully() throws Exception {
            var process = new ProcessBuilder(List.of("sleep", "30")).start();

            assertThat(process.isAlive()).isTrue();

            ProcessTerminator.terminate(process);

            // Wait briefly for termination
            process.waitFor(2, TimeUnit.SECONDS);
            assertThat(process.isAlive()).isFalse();
        }
    }

    @Nested
    @DisplayName("terminate with custom grace period")
    class TerminateWithCustomGracePeriod {

        @Test
        @DisplayName("should handle null process with custom grace period")
        void shouldHandleNullProcessWithCustomGracePeriod() {
            assertThatCode(() -> ProcessTerminator.terminate(null, Duration.ofSeconds(1)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should terminate with short grace period")
        @DisabledOnOs(OS.WINDOWS)
        void shouldTerminateWithShortGracePeriod() throws Exception {
            var process = new ProcessBuilder(List.of("sleep", "30")).start();

            assertThat(process.isAlive()).isTrue();

            ProcessTerminator.terminate(process, Duration.ofMillis(100));

            // Wait briefly for termination
            process.waitFor(2, TimeUnit.SECONDS);
            assertThat(process.isAlive()).isFalse();
        }

        @Test
        @DisplayName("should force kill process that ignores SIGTERM")
        @DisabledOnOs(OS.WINDOWS)
        void shouldForceKillProcessThatIgnoresSigterm() throws Exception {
            // Use a process that might resist termination
            // Note: Most shell commands respond to SIGTERM, so this tests the timeout path
            var process = new ProcessBuilder(List.of("sleep", "30")).start();

            assertThat(process.isAlive()).isTrue();

            // Use very short grace period to force immediate kill
            ProcessTerminator.terminate(process, Duration.ofMillis(1));

            // Wait briefly for termination
            process.waitFor(2, TimeUnit.SECONDS);
            assertThat(process.isAlive()).isFalse();
        }
    }

    @Nested
    @DisplayName("terminate mocked process")
    class TerminateMockedProcess {

        @Test
        @DisplayName("should call destroy first then destroyForcibly on timeout")
        void shouldCallDestroyFirstThenDestroyForciblyOnTimeout() throws Exception {
            var process = mock(Process.class);
            var handle = mock(ProcessHandle.class);
            when(process.isAlive()).thenReturn(true);
            when(process.pid()).thenReturn(12345L);
            when(process.toHandle()).thenReturn(handle);
            // Return a fresh empty stream each time descendants() is called
            when(handle.descendants()).thenAnswer(invocation -> java.util.stream.Stream.empty());
            when(process.waitFor(100, TimeUnit.MILLISECONDS)).thenReturn(false);
            when(process.waitFor(1, TimeUnit.SECONDS)).thenReturn(true);

            ProcessTerminator.terminate(process, Duration.ofMillis(100));

            verify(process).destroy();
            verify(process).destroyForcibly();
        }

        @Test
        @DisplayName("should only call destroy when process exits gracefully")
        void shouldOnlyCallDestroyWhenProcessExitsGracefully() throws Exception {
            var process = mock(Process.class);
            var handle = mock(ProcessHandle.class);
            when(process.isAlive()).thenReturn(true);
            when(process.pid()).thenReturn(12345L);
            when(process.toHandle()).thenReturn(handle);
            // Return a fresh empty stream each time descendants() is called
            when(handle.descendants()).thenAnswer(invocation -> java.util.stream.Stream.empty());
            when(process.waitFor(100, TimeUnit.MILLISECONDS)).thenReturn(true);

            ProcessTerminator.terminate(process, Duration.ofMillis(100));

            verify(process).destroy();
            verify(process, never()).destroyForcibly();
        }
    }

    @Nested
    @DisplayName("terminate with descendant processes")
    class TerminateWithDescendants {

        @Test
        @DisplayName("should terminate descendant processes")
        @DisabledOnOs(OS.WINDOWS)
        void shouldTerminateDescendantProcesses() throws Exception {
            // Create a process that spawns children using bash
            var process = new ProcessBuilder(List.of("sh", "-c", "sleep 60 & sleep 60 & sleep 30")).start();

            // Allow children to spawn
            Thread.sleep(200);

            assertThat(process.isAlive()).isTrue();
            long descendantCount = process.toHandle().descendants().count();
            assertThat(descendantCount).isGreaterThan(0);

            ProcessTerminator.terminate(process, Duration.ofMillis(500));

            // Wait briefly for termination
            process.waitFor(2, TimeUnit.SECONDS);
            assertThat(process.isAlive()).isFalse();

            // All descendants should also be terminated
            assertThat(process.toHandle().descendants().count()).isZero();
        }

        @Test
        @DisplayName("should handle process with no descendants")
        @DisabledOnOs(OS.WINDOWS)
        void shouldHandleProcessWithNoDescendants() throws Exception {
            var process = new ProcessBuilder(List.of("sleep", "30")).start();

            assertThat(process.isAlive()).isTrue();
            assertThat(process.toHandle().descendants().count()).isZero();

            ProcessTerminator.terminate(process);

            process.waitFor(2, TimeUnit.SECONDS);
            assertThat(process.isAlive()).isFalse();
        }
    }
}
