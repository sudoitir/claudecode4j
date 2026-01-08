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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Definition of a tool registered with the MCP server.
 *
 * @param name the tool name
 * @param description the tool description
 * @param method the method to invoke
 * @param target the object instance containing the method
 * @param parameters the parameter definitions
 * @param categories the tool categories
 * @param requiresPermission whether permission is required
 */
public record ToolDefinition(
        String name,
        String description,
        Method method,
        Object target,
        List<ParameterDefinition> parameters,
        List<String> categories,
        boolean requiresPermission) {

    /**
     * Creates a builder for ToolDefinition.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Converts this definition to a JSON schema for the input parameters.
     *
     * @return the input schema as a map
     */
    public Map<String, Object> toInputSchema() {
        var properties = new java.util.LinkedHashMap<String, Object>();
        var required = new java.util.ArrayList<String>();

        for (ParameterDefinition param : parameters) {
            var propDef = new java.util.LinkedHashMap<String, Object>();
            propDef.put("type", param.jsonType());
            if (param.description() != null && !param.description().isEmpty()) {
                propDef.put("description", param.description());
            }
            properties.put(param.name(), propDef);

            if (param.required()) {
                required.add(param.name());
            }
        }

        return Map.of("type", "object", "properties", properties, "required", required);
    }

    /**
     * Definition of a tool parameter.
     *
     * @param name the parameter name
     * @param description the parameter description
     * @param javaType the Java type
     * @param jsonType the JSON schema type
     * @param required whether the parameter is required
     * @param defaultValue the default value
     */
    public record ParameterDefinition(
            String name,
            @Nullable String description,
            Class<?> javaType,
            String jsonType,
            boolean required,
            @Nullable String defaultValue) {

        /**
         * Converts a Java type to a JSON schema type.
         *
         * @param javaType the Java type
         * @return the JSON schema type
         */
        public static String toJsonType(Class<?> javaType) {
            if (javaType == String.class) {
                return "string";
            } else if (javaType == Integer.class
                    || javaType == int.class
                    || javaType == Long.class
                    || javaType == long.class) {
                return "integer";
            } else if (javaType == Double.class
                    || javaType == double.class
                    || javaType == Float.class
                    || javaType == float.class) {
                return "number";
            } else if (javaType == Boolean.class || javaType == boolean.class) {
                return "boolean";
            } else if (javaType.isArray() || java.util.Collection.class.isAssignableFrom(javaType)) {
                return "array";
            } else {
                return "object";
            }
        }
    }

    /** Builder for ToolDefinition. */
    public static final class Builder {
        private String name;
        private String description = "";
        private Method method;
        private Object target;
        private List<ParameterDefinition> parameters = List.of();
        private List<String> categories = List.of();
        private boolean requiresPermission;

        private Builder() {}

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder method(Method method) {
            this.method = method;
            return this;
        }

        public Builder target(Object target) {
            this.target = target;
            return this;
        }

        public Builder parameters(List<ParameterDefinition> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder categories(List<String> categories) {
            this.categories = categories;
            return this;
        }

        public Builder requiresPermission(boolean requiresPermission) {
            this.requiresPermission = requiresPermission;
            return this;
        }

        public ToolDefinition build() {
            return new ToolDefinition(name, description, method, target, parameters, categories, requiresPermission);
        }
    }
}
