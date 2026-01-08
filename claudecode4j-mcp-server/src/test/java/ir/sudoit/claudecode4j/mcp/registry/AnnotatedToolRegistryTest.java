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
package ir.sudoit.claudecode4j.mcp.registry;

import static org.assertj.core.api.Assertions.assertThat;

import ir.sudoit.claudecode4j.mcp.annotation.ClaudeTool;
import ir.sudoit.claudecode4j.mcp.annotation.ToolParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnnotatedToolRegistryTest {

    private AnnotatedToolRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new AnnotatedToolRegistry();
    }

    @Test
    void shouldRegisterAnnotatedTool() {
        TestToolBean bean = new TestToolBean();

        registry.postProcessAfterInitialization(bean, "testToolBean");

        assertThat(registry.size()).isEqualTo(1);
        assertThat(registry.contains("greet")).isTrue();
    }

    @Test
    void shouldRegisterToolWithCustomName() {
        TestToolBeanWithCustomName bean = new TestToolBeanWithCustomName();

        registry.postProcessAfterInitialization(bean, "testToolBean");

        assertThat(registry.contains("say_hello")).isTrue();
        assertThat(registry.contains("hello")).isFalse();
    }

    @Test
    void shouldNotRegisterNonAnnotatedMethods() {
        NonAnnotatedBean bean = new NonAnnotatedBean();

        registry.postProcessAfterInitialization(bean, "nonAnnotatedBean");

        assertThat(registry.size()).isZero();
    }

    @Test
    void shouldSkipDuplicateToolNames() {
        TestToolBean bean1 = new TestToolBean();
        TestToolBean bean2 = new TestToolBean();

        registry.postProcessAfterInitialization(bean1, "bean1");
        registry.postProcessAfterInitialization(bean2, "bean2");

        assertThat(registry.size()).isEqualTo(1);
    }

    @Test
    void shouldExtractParameterDefinitions() {
        TestToolBeanWithParams bean = new TestToolBeanWithParams();

        registry.postProcessAfterInitialization(bean, "testToolBean");

        var tool = registry.getTool("process").orElseThrow();
        assertThat(tool.parameters()).hasSize(2);
    }

    @Test
    void shouldReturnAllTools() {
        TestToolBean bean1 = new TestToolBean();
        TestToolBeanWithParams bean2 = new TestToolBeanWithParams();

        registry.postProcessAfterInitialization(bean1, "bean1");
        registry.postProcessAfterInitialization(bean2, "bean2");

        assertThat(registry.getAllTools()).hasSize(2);
    }

    // Test beans

    static class TestToolBean {
        @ClaudeTool(name = "greet", description = "Greets a person")
        public String greet(String name) {
            return "Hello, " + name;
        }
    }

    static class TestToolBeanWithCustomName {
        @ClaudeTool(name = "say_hello", description = "Says hello")
        public String hello() {
            return "Hello!";
        }
    }

    static class TestToolBeanWithParams {
        @ClaudeTool(name = "process", description = "Processes data")
        public String process(
                @ToolParam(name = "input", description = "The input data") String input,
                @ToolParam(name = "count", description = "Number of iterations", required = false) int count) {
            return input.repeat(count);
        }
    }

    static class NonAnnotatedBean {
        public String normalMethod() {
            return "not a tool";
        }
    }
}
