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
 * Provides metadata for a tool parameter.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @ClaudeTool(description = "Search for files")
 * public List<String> search(
 *     @ToolParam(description = "Search pattern") String pattern,
 *     @ToolParam(description = "Max results", required = false) Integer limit) {
 *     // ...
 * }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ToolParam {

    /**
     * The parameter name. If empty, the parameter name from bytecode will be used (requires -parameters compiler flag).
     *
     * @return the parameter name
     */
    String name() default "";

    /**
     * A description of the parameter. This helps Claude understand what value to provide.
     *
     * @return the parameter description
     */
    String description() default "";

    /**
     * Whether this parameter is required.
     *
     * @return true if required
     */
    boolean required() default true;

    /**
     * The default value if the parameter is not provided.
     *
     * @return the default value as a string
     */
    String defaultValue() default "";
}
