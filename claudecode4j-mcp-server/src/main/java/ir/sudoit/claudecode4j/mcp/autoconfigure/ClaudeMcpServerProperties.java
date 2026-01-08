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

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration properties for MCP server.
 *
 * @param enabled whether MCP server is enabled
 * @param transport the transport type (stdio, sse, http)
 * @param serverName the server name
 * @param serverVersion the server version
 * @param scanPackages packages to scan for @ClaudeTool annotations
 */
@ConfigurationProperties(prefix = "claude.code.mcp")
public record ClaudeMcpServerProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("stdio") String transport,
        @DefaultValue("claudecode4j-mcp") String serverName,
        @DefaultValue("1.0.0") String serverVersion,
        @Nullable List<String> scanPackages) {

    /** Supported transport types. */
    public enum Transport {
        STDIO,
        SSE,
        HTTP
    }

    /**
     * Gets the transport as an enum.
     *
     * @return the transport type
     */
    public Transport getTransportType() {
        return switch (transport.toLowerCase()) {
            case "sse" -> Transport.SSE;
            case "http" -> Transport.HTTP;
            default -> Transport.STDIO;
        };
    }
}
