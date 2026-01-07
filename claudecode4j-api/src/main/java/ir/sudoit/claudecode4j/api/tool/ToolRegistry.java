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
import java.util.Optional;

/**
 * SPI for registering and managing tools available to Claude CLI.
 *
 * <p>Implementations can provide different storage mechanisms (in-memory, file-based, database).
 */
public interface ToolRegistry {

    /**
     * Registers a tool in the registry.
     *
     * @param tool the tool to register
     */
    void register(Tool tool);

    /**
     * Unregisters a tool from the registry.
     *
     * @param name the tool name
     * @return true if the tool was removed, false if not found
     */
    boolean unregister(String name);

    /**
     * Returns a tool by name.
     *
     * @param name the tool name
     * @return the tool if found
     */
    Optional<Tool> get(String name);

    /**
     * Returns all registered tools.
     *
     * @return list of all tools
     */
    List<Tool> getAll();

    /**
     * Returns tools of a specific type.
     *
     * @param type the tool type
     * @return list of matching tools
     */
    List<Tool> getByType(Tool.ToolType type);

    /**
     * Checks if a tool is registered.
     *
     * @param name the tool name
     * @return true if registered
     */
    default boolean isRegistered(String name) {
        return get(name).isPresent();
    }

    /** Clears all registered tools. */
    void clear();

    /** Priority for SPI selection (higher = preferred). */
    default int priority() {
        return 0;
    }
}
