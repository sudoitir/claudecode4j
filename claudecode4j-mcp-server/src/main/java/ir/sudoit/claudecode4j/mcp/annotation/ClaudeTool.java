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
package ir.sudoit.claudecode4j.mcp.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a Claude tool that can be invoked by Claude CLI.
 *
 * <p>Methods annotated with @ClaudeTool will be registered as MCP tools and made available to Claude when using the MCP
 * server adapter.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Component
 * public class FileTools {
 *     @ClaudeTool(name = "read_config", description = "Reads application configuration")
 *     public String readConfig(@ToolParam(description = "Config key") String key) {
 *         return configService.get(key);
 *     }
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ClaudeTool {

    /**
     * The tool name. If empty, the method name will be used.
     *
     * @return the tool name
     */
    String name() default "";

    /**
     * A description of what the tool does. This helps Claude understand when and how to use the tool.
     *
     * @return the tool description
     */
    String description() default "";

    /**
     * Categories or tags for the tool. Can be used for filtering.
     *
     * @return the tool categories
     */
    String[] categories() default {};

    /**
     * Whether this tool requires user permission before execution.
     *
     * @return true if permission is required
     */
    boolean requiresPermission() default false;
}
