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

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import tools.jackson.databind.ObjectMapper;

/** Handles invocation of registered tools. */
public final class ToolInvocationHandler {

    private static final Logger LOG = System.getLogger(ToolInvocationHandler.class.getName());

    private final ObjectMapper objectMapper;

    public ToolInvocationHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Invokes a tool with the given arguments.
     *
     * @param definition the tool definition
     * @param arguments the input arguments as a map
     * @return the result of the invocation
     * @throws ToolInvocationException if invocation fails
     */
    public Object invoke(ToolDefinition definition, Map<String, Object> arguments) throws ToolInvocationException {
        try {
            var method = definition.method();
            var parameters = definition.parameters();
            var args = new Object[parameters.size()];

            for (int i = 0; i < parameters.size(); i++) {
                var param = parameters.get(i);
                Object value = arguments.get(param.name());

                if (value == null) {
                    if (param.required()
                            && (param.defaultValue() == null
                                    || param.defaultValue().isEmpty())) {
                        throw new ToolInvocationException(
                                "Missing required parameter: " + param.name(), definition.name());
                    }
                    if (param.defaultValue() != null && !param.defaultValue().isEmpty()) {
                        value = convertValue(param.defaultValue(), param.javaType());
                    }
                } else {
                    value = convertValue(value, param.javaType());
                }

                args[i] = value;
            }

            LOG.log(Level.DEBUG, "Invoking tool {0} with args: {1}", definition.name(), arguments);
            return method.invoke(definition.target(), args);

        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new ToolInvocationException("Failed to invoke tool: " + e.getMessage(), definition.name(), e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new ToolInvocationException("Tool execution failed: " + cause.getMessage(), definition.name(), cause);
        }
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        if (targetType.isInstance(value)) {
            return value;
        }

        // Handle string to primitive conversions
        if (value instanceof String strValue) {
            if (targetType == Integer.class || targetType == int.class) {
                return Integer.parseInt(strValue);
            } else if (targetType == Long.class || targetType == long.class) {
                return Long.parseLong(strValue);
            } else if (targetType == Double.class || targetType == double.class) {
                return Double.parseDouble(strValue);
            } else if (targetType == Boolean.class || targetType == boolean.class) {
                return Boolean.parseBoolean(strValue);
            }
        }

        // Use Jackson for complex conversions
        return objectMapper.convertValue(value, targetType);
    }

    /** Exception thrown when tool invocation fails. */
    public static class ToolInvocationException extends Exception {
        private final String toolName;

        public ToolInvocationException(String message, String toolName) {
            super(message);
            this.toolName = toolName;
        }

        public ToolInvocationException(String message, String toolName, Throwable cause) {
            super(message, cause);
            this.toolName = toolName;
        }

        public String getToolName() {
            return toolName;
        }
    }
}
