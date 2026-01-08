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

import ir.sudoit.claudecode4j.mcp.annotation.ClaudeTool;
import ir.sudoit.claudecode4j.mcp.annotation.ToolParam;
import ir.sudoit.claudecode4j.mcp.server.ToolDefinition;
import ir.sudoit.claudecode4j.mcp.server.ToolDefinition.ParameterDefinition;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;

/** Registry that scans Spring beans for @ClaudeTool annotated methods and registers them as tool definitions. */
public final class AnnotatedToolRegistry implements BeanPostProcessor {

    private static final Logger LOG = System.getLogger(AnnotatedToolRegistry.class.getName());

    private final ConcurrentMap<String, ToolDefinition> tools = new ConcurrentHashMap<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();

        for (Method method : beanClass.getMethods()) {
            ClaudeTool annotation = AnnotationUtils.findAnnotation(method, ClaudeTool.class);
            if (annotation != null) {
                registerTool(bean, method, annotation);
            }
        }

        return bean;
    }

    private void registerTool(Object bean, Method method, ClaudeTool annotation) {
        String toolName = annotation.name().isEmpty() ? method.getName() : annotation.name();

        if (tools.containsKey(toolName)) {
            LOG.log(Level.WARNING, "Tool already registered with name: {0}. Skipping duplicate.", toolName);
            return;
        }

        List<ParameterDefinition> params = extractParameters(method);

        ToolDefinition definition = ToolDefinition.builder()
                .name(toolName)
                .description(annotation.description())
                .method(method)
                .target(bean)
                .parameters(params)
                .categories(List.of(annotation.categories()))
                .requiresPermission(annotation.requiresPermission())
                .build();

        tools.put(toolName, definition);
        LOG.log(
                Level.INFO,
                "Registered Claude tool: {0} from {1}.{2}",
                toolName,
                bean.getClass().getSimpleName(),
                method.getName());
    }

    private List<ParameterDefinition> extractParameters(Method method) {
        List<ParameterDefinition> params = new ArrayList<>();
        Parameter[] methodParams = method.getParameters();

        for (Parameter param : methodParams) {
            ToolParam annotation = param.getAnnotation(ToolParam.class);

            String paramName =
                    (annotation != null && !annotation.name().isEmpty()) ? annotation.name() : param.getName();

            String description = (annotation != null) ? annotation.description() : "";
            boolean required = (annotation == null) || annotation.required();
            String defaultValue = (annotation != null) ? annotation.defaultValue() : "";

            params.add(new ParameterDefinition(
                    paramName,
                    description,
                    param.getType(),
                    ParameterDefinition.toJsonType(param.getType()),
                    required,
                    defaultValue.isEmpty() ? null : defaultValue));
        }

        return params;
    }

    /**
     * Gets a tool definition by name.
     *
     * @param name the tool name
     * @return the tool definition if found
     */
    public Optional<ToolDefinition> getTool(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    /**
     * Gets all registered tool definitions.
     *
     * @return all tool definitions
     */
    public Collection<ToolDefinition> getAllTools() {
        return tools.values();
    }

    /**
     * Gets all tools as a map.
     *
     * @return map of tool name to definition
     */
    public Map<String, ToolDefinition> getToolsMap() {
        return Map.copyOf(tools);
    }

    /**
     * Returns the number of registered tools.
     *
     * @return the tool count
     */
    public int size() {
        return tools.size();
    }

    /**
     * Checks if a tool with the given name is registered.
     *
     * @param name the tool name
     * @return true if registered
     */
    public boolean contains(String name) {
        return tools.containsKey(name);
    }
}
