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
package ir.sudoit.claudecode4j.api.tool;

import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Represents a tool that can be enabled for Claude CLI execution.
 *
 * <p>Tools extend Claude's capabilities by allowing it to perform specific actions like reading files, executing
 * commands, or accessing external services.
 */
public record Tool(
        String name,
        @Nullable String description,
        ToolType type,
        @Nullable Map<String, Object> config,
        List<String> permissions) {

    /** Creates a new Tool with the given name and type. */
    public static Tool of(String name, ToolType type) {
        return new Tool(name, null, type, null, List.of());
    }

    /** Creates a new Tool with the given name, type, and description. */
    public static Tool of(String name, ToolType type, String description) {
        return new Tool(name, description, type, null, List.of());
    }

    /** Creates a builder for constructing a Tool. */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    /** Tool types supported by Claude CLI. */
    public enum ToolType {
        /** Built-in tool (Bash, Read, Write, etc.) */
        BUILTIN,
        /** MCP (Model Context Protocol) server tool */
        MCP,
        /** Custom tool defined by the user */
        CUSTOM
    }

    public static final class Builder {
        private final String name;
        private @Nullable String description;
        private ToolType type = ToolType.BUILTIN;
        private @Nullable Map<String, Object> config;
        private List<String> permissions = List.of();

        private Builder(String name) {
            this.name = name;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder type(ToolType type) {
            this.type = type;
            return this;
        }

        public Builder config(Map<String, Object> config) {
            this.config = config;
            return this;
        }

        public Builder permissions(List<String> permissions) {
            this.permissions = permissions;
            return this;
        }

        public Builder permissions(String... permissions) {
            this.permissions = List.of(permissions);
            return this;
        }

        public Tool build() {
            return new Tool(name, description, type, config, permissions);
        }
    }
}
