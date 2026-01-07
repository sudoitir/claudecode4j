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
package ir.sudoit.claudecode4j.spring.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.spring.properties.ClaudeCodeProperties;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

@ExtendWith(MockitoExtension.class)
class ClaudeCodeHealthIndicatorTest {

    @Mock
    private ClaudeClient claudeClient;

    private ClaudeCodeHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        ClaudeCodeProperties properties = new ClaudeCodeProperties(
                null,
                4,
                Duration.ofMinutes(5),
                false,
                true,
                new ClaudeCodeProperties.Health(true, Duration.ofSeconds(30), null),
                new ClaudeCodeProperties.Metrics(true, "claude.code"));
        healthIndicator = new ClaudeCodeHealthIndicator(claudeClient, properties);
    }

    @Test
    void shouldReturnUpWhenClientAvailable() {
        when(claudeClient.isAvailable()).thenReturn(true);
        when(claudeClient.getCliVersion()).thenReturn("1.0.0");

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsKey("version");
        assertThat(health.getDetails().get("version")).isEqualTo("1.0.0");
    }

    @Test
    void shouldReturnDownWhenClientNotAvailable() {
        when(claudeClient.isAvailable()).thenReturn(false);

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("available");
        assertThat(health.getDetails().get("available")).isEqualTo(false);
    }

    @Test
    void shouldReturnDownWhenClientThrowsException() {
        when(claudeClient.isAvailable()).thenThrow(new RuntimeException("Connection failed"));

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("error");
    }

    @Test
    void shouldCacheHealthResult() {
        when(claudeClient.isAvailable()).thenReturn(true);
        when(claudeClient.getCliVersion()).thenReturn("1.0.0");

        Health health1 = healthIndicator.health();
        Health health2 = healthIndicator.health();

        assertThat(health1.getStatus()).isEqualTo(health2.getStatus());
    }
}
