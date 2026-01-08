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
package ir.sudoit.claudecode4j.websocket.autoconfigure;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration properties for WebSocket terminal.
 *
 * @param enabled whether WebSocket is enabled
 * @param endpoint the WebSocket endpoint path
 * @param sessionTimeout session idle timeout
 * @param maxSessions maximum concurrent sessions
 * @param heartbeatInterval heartbeat ping interval
 * @param permissionTimeout timeout for permission responses
 */
@ConfigurationProperties(prefix = "claude.code.websocket")
public record ClaudeWebSocketProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("/ws/cli") String endpoint,
        @DefaultValue("30m") Duration sessionTimeout,
        @DefaultValue("100") int maxSessions,
        @DefaultValue("30s") Duration heartbeatInterval,
        @DefaultValue("5m") Duration permissionTimeout) {}
