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
package ir.sudoit.claudecode4j.spring.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import ir.sudoit.claudecode4j.api.client.ClaudeClient;
import ir.sudoit.claudecode4j.spring.properties.ClaudeCodeProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ClaudeCodeAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(ClaudeCodeAutoConfiguration.class));

    @Test
    void shouldCreateClaudeClientBean() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ClaudeClient.class);
        });
    }

    @Test
    void shouldCreatePropertiesBean() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ClaudeCodeProperties.class);
        });
    }

    @Test
    void shouldBindProperties() {
        contextRunner
                .withPropertyValues(
                        "claude.code.concurrency-limit=8",
                        "claude.code.default-timeout=10m",
                        "claude.code.dangerously-skip-permissions=true")
                .run(context -> {
                    ClaudeCodeProperties properties = context.getBean(ClaudeCodeProperties.class);
                    assertThat(properties.concurrencyLimit()).isEqualTo(8);
                    assertThat(properties.dangerouslySkipPermissions()).isTrue();
                });
    }

    @Test
    void shouldUseDefaultProperties() {
        contextRunner.run(context -> {
            ClaudeCodeProperties properties = context.getBean(ClaudeCodeProperties.class);
            assertThat(properties.concurrencyLimit()).isEqualTo(4);
            assertThat(properties.dangerouslySkipPermissions()).isFalse();
        });
    }

    @Test
    void shouldNotCreateBeanWhenDisabled() {
        contextRunner.withPropertyValues("claude.code.enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(ClaudeClient.class);
        });
    }

    @Test
    void shouldRespectCustomBinaryPath() {
        contextRunner
                .withPropertyValues("claude.code.binary-path=/custom/path/claude")
                .run(context -> {
                    ClaudeCodeProperties properties = context.getBean(ClaudeCodeProperties.class);
                    assertThat(properties.binaryPath()).isEqualTo("/custom/path/claude");
                });
    }
}
