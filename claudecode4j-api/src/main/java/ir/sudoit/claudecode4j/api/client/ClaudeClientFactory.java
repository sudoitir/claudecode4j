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
package ir.sudoit.claudecode4j.api.client;

import ir.sudoit.claudecode4j.api.config.ClaudeConfig;
import java.util.ServiceLoader;

public interface ClaudeClientFactory {

    ClaudeClient createClient();

    ClaudeClient createClient(ClaudeConfig config);

    default int priority() {
        return 0;
    }

    static ClaudeClient create() {
        return findFactory().createClient();
    }

    static ClaudeClient create(ClaudeConfig config) {
        return findFactory().createClient(config);
    }

    private static ClaudeClientFactory findFactory() {
        return ServiceLoader.load(ClaudeClientFactory.class).stream()
                .map(ServiceLoader.Provider::get)
                .max((a, b) -> Integer.compare(a.priority(), b.priority()))
                .orElseThrow(() ->
                        new IllegalStateException("No ClaudeClientFactory found. Add claudecode4j-core to classpath."));
    }
}
