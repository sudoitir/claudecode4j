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
package ir.sudoit.claudecode4j.mcp.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ir.sudoit.claudecode4j.mcp.server.ToolInvocationHandler.ToolInvocationException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class ToolInvocationHandlerTest {

    private ToolInvocationHandler handler;
    private TestTool testTool;

    @BeforeEach
    void setUp() {
        handler = new ToolInvocationHandler(JsonMapper.builder().build());
        testTool = new TestTool();
    }

    @Test
    void shouldInvokeToolWithSimpleArguments() throws Exception {
        Method method = TestTool.class.getMethod("greet", String.class);
        ToolDefinition definition = createToolDefinition(
                "greet",
                method,
                testTool,
                List.of(new ToolDefinition.ParameterDefinition(
                        "name", "Name to greet", String.class, "string", true, null)));

        Object result = handler.invoke(definition, Map.of("name", "World"));

        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    void shouldInvokeToolWithMultipleArguments() throws Exception {
        Method method = TestTool.class.getMethod("add", int.class, int.class);
        ToolDefinition definition = createToolDefinition(
                "add",
                method,
                testTool,
                List.of(
                        new ToolDefinition.ParameterDefinition("a", "First number", int.class, "integer", true, null),
                        new ToolDefinition.ParameterDefinition(
                                "b", "Second number", int.class, "integer", true, null)));

        Object result = handler.invoke(definition, Map.of("a", 5, "b", 3));

        assertThat(result).isEqualTo(8);
    }

    @Test
    void shouldConvertStringToInteger() throws Exception {
        Method method = TestTool.class.getMethod("add", int.class, int.class);
        ToolDefinition definition = createToolDefinition(
                "add",
                method,
                testTool,
                List.of(
                        new ToolDefinition.ParameterDefinition("a", "First number", int.class, "integer", true, null),
                        new ToolDefinition.ParameterDefinition(
                                "b", "Second number", int.class, "integer", true, null)));

        Object result = handler.invoke(definition, Map.of("a", "10", "b", "20"));

        assertThat(result).isEqualTo(30);
    }

    @Test
    void shouldThrowForMissingRequiredParameter() throws Exception {
        Method method = TestTool.class.getMethod("greet", String.class);
        ToolDefinition definition = createToolDefinition(
                "greet",
                method,
                testTool,
                List.of(new ToolDefinition.ParameterDefinition(
                        "name", "Name to greet", String.class, "string", true, null)));

        assertThatThrownBy(() -> handler.invoke(definition, Map.of()))
                .isInstanceOf(ToolInvocationException.class)
                .hasMessageContaining("Missing required parameter");
    }

    @Test
    void shouldUseDefaultValueForOptionalParameter() throws Exception {
        Method method = TestTool.class.getMethod("greet", String.class);
        ToolDefinition definition = createToolDefinition(
                "greet",
                method,
                testTool,
                List.of(new ToolDefinition.ParameterDefinition(
                        "name", "Name to greet", String.class, "string", false, "Default")));

        Object result = handler.invoke(definition, Map.of());

        assertThat(result).isEqualTo("Hello, Default!");
    }

    private ToolDefinition createToolDefinition(
            String name, Method method, Object target, List<ToolDefinition.ParameterDefinition> params) {
        return ToolDefinition.builder()
                .name(name)
                .description("Test tool")
                .method(method)
                .target(target)
                .parameters(params)
                .build();
    }

    // Test tool class
    public static class TestTool {
        public String greet(String name) {
            return "Hello, " + name + "!";
        }

        public int add(int a, int b) {
            return a + b;
        }
    }
}
