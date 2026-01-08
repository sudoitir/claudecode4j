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
package ir.sudoit.claudecode4j.mcp.autoconfigure;

import ir.sudoit.claudecode4j.mcp.registry.AnnotatedToolRegistry;
import ir.sudoit.claudecode4j.mcp.server.ToolInvocationHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.ObjectMapper;

/** Auto-configuration for MCP server. */
@AutoConfiguration
@ConditionalOnProperty(prefix = "claude.code.mcp", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ClaudeMcpServerProperties.class)
public class ClaudeMcpServerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AnnotatedToolRegistry annotatedToolRegistry() {
        return new AnnotatedToolRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public ToolInvocationHandler toolInvocationHandler(ObjectMapper objectMapper) {
        return new ToolInvocationHandler(objectMapper);
    }
}
