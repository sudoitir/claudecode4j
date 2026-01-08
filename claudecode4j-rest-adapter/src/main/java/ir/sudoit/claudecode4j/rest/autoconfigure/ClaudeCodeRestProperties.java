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
package ir.sudoit.claudecode4j.rest.autoconfigure;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for ClaudeCode4J REST adapters.
 *
 * <p>Configured via {@code claude.code.rest.*} properties.
 */
@ConfigurationProperties(prefix = "claude.code.rest")
public class ClaudeCodeRestProperties {

    private String basePath = "/api/claude";
    private boolean enabled = true;

    private OpenAi openai = new OpenAi();
    private Anthropic anthropic = new Anthropic();

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public OpenAi getOpenai() {
        return openai;
    }

    public void setOpenai(OpenAi openai) {
        this.openai = openai;
    }

    public Anthropic getAnthropic() {
        return anthropic;
    }

    public void setAnthropic(Anthropic anthropic) {
        this.anthropic = anthropic;
    }

    /** OpenAI-compatible API configuration. */
    public record OpenAi(
            @Nullable String basePath, @Nullable Boolean enabled) {

        public OpenAi() {
            this("/v1", true);
        }

        public String getBasePath() {
            return basePath != null ? basePath : "/v1";
        }

        public boolean isEnabled() {
            return enabled != null ? enabled : true;
        }
    }

    /** Anthropic-compatible API configuration. */
    public record Anthropic(
            @Nullable String basePath, @Nullable Boolean enabled) {

        public Anthropic() {
            this("/v1", true);
        }

        public String getBasePath() {
            return basePath != null ? basePath : "/v1";
        }

        public boolean isEnabled() {
            return enabled != null ? enabled : true;
        }
    }
}
