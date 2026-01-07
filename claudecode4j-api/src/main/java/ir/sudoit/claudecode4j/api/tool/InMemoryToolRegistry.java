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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of ToolRegistry.
 *
 * <p>Thread-safe using ConcurrentHashMap.
 */
public final class InMemoryToolRegistry implements ToolRegistry {

    private final Map<String, Tool> tools = new ConcurrentHashMap<>();

    /** Creates an empty registry. */
    public InMemoryToolRegistry() {}

    /** Creates a registry with pre-registered tools. */
    public InMemoryToolRegistry(List<Tool> initialTools) {
        for (var tool : initialTools) {
            tools.put(tool.name(), tool);
        }
    }

    /** Creates a registry with default built-in tools. */
    public static InMemoryToolRegistry withDefaults() {
        var registry = new InMemoryToolRegistry();
        // Register common built-in tools
        registry.register(Tool.of("Bash", Tool.ToolType.BUILTIN, "Execute shell commands"));
        registry.register(Tool.of("Read", Tool.ToolType.BUILTIN, "Read file contents"));
        registry.register(Tool.of("Write", Tool.ToolType.BUILTIN, "Write file contents"));
        registry.register(Tool.of("Edit", Tool.ToolType.BUILTIN, "Edit file contents"));
        registry.register(Tool.of("Glob", Tool.ToolType.BUILTIN, "Find files by pattern"));
        registry.register(Tool.of("Grep", Tool.ToolType.BUILTIN, "Search file contents"));
        registry.register(Tool.of("LS", Tool.ToolType.BUILTIN, "List directory contents"));
        registry.register(Tool.of("Task", Tool.ToolType.BUILTIN, "Create background tasks"));
        return registry;
    }

    @Override
    public void register(Tool tool) {
        tools.put(tool.name(), tool);
    }

    @Override
    public boolean unregister(String name) {
        return tools.remove(name) != null;
    }

    @Override
    public Optional<Tool> get(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    @Override
    public List<Tool> getAll() {
        return new ArrayList<>(tools.values());
    }

    @Override
    public List<Tool> getByType(Tool.ToolType type) {
        return tools.values().stream().filter(tool -> tool.type() == type).toList();
    }

    @Override
    public void clear() {
        tools.clear();
    }

    @Override
    public int priority() {
        return 10;
    }
}
