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
package ir.sudoit.claudecode4j.context.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token limits for different Claude models.
 *
 * @param model the model identifier
 * @param contextWindow total context window size in tokens
 * @param outputLimit maximum output tokens
 * @param reservedForPrompt tokens reserved for the prompt itself
 */
public record ModelTokenLimits(String model, int contextWindow, int outputLimit, int reservedForPrompt) {

    /** Claude 3 Opus with 200K context */
    public static final ModelTokenLimits CLAUDE_3_OPUS = new ModelTokenLimits("claude-3-opus", 200_000, 4_096, 10_000);

    /** Claude 3 Sonnet with 200K context */
    public static final ModelTokenLimits CLAUDE_3_SONNET =
            new ModelTokenLimits("claude-3-sonnet", 200_000, 4_096, 10_000);

    /** Claude 3 Haiku with 200K context */
    public static final ModelTokenLimits CLAUDE_3_HAIKU =
            new ModelTokenLimits("claude-3-haiku", 200_000, 4_096, 10_000);

    /** Claude 3.5 Sonnet with 200K context and 8K output */
    public static final ModelTokenLimits CLAUDE_3_5_SONNET =
            new ModelTokenLimits("claude-3.5-sonnet", 200_000, 8_192, 10_000);

    /** Claude 3.5 Haiku with 200K context and 8K output */
    public static final ModelTokenLimits CLAUDE_3_5_HAIKU =
            new ModelTokenLimits("claude-3.5-haiku", 200_000, 8_192, 10_000);

    /** Claude Sonnet 4 with 200K context and 16K output */
    public static final ModelTokenLimits CLAUDE_SONNET_4 =
            new ModelTokenLimits("claude-sonnet-4", 200_000, 16_000, 10_000);

    /** Claude Opus 4.5 with 200K context and 16K output */
    public static final ModelTokenLimits CLAUDE_OPUS_4_5 =
            new ModelTokenLimits("claude-opus-4.5", 200_000, 16_000, 10_000);

    /** Default limits for unknown models */
    public static final ModelTokenLimits DEFAULT = new ModelTokenLimits("default", 200_000, 4_096, 10_000);

    private static final Map<String, ModelTokenLimits> REGISTRY = new ConcurrentHashMap<>();

    static {
        register(CLAUDE_3_OPUS);
        register(CLAUDE_3_SONNET);
        register(CLAUDE_3_HAIKU);
        register(CLAUDE_3_5_SONNET);
        register(CLAUDE_3_5_HAIKU);
        register(CLAUDE_SONNET_4);
        register(CLAUDE_OPUS_4_5);
        // Register with common aliases
        REGISTRY.put("claude-3-opus-20240229", CLAUDE_3_OPUS);
        REGISTRY.put("claude-3-sonnet-20240229", CLAUDE_3_SONNET);
        REGISTRY.put("claude-3-haiku-20240307", CLAUDE_3_HAIKU);
        REGISTRY.put("claude-3-5-sonnet-20241022", CLAUDE_3_5_SONNET);
        REGISTRY.put("claude-sonnet-4-20250514", CLAUDE_SONNET_4);
        REGISTRY.put("claude-opus-4-5-20251101", CLAUDE_OPUS_4_5);
        REGISTRY.put("sonnet", CLAUDE_SONNET_4);
        REGISTRY.put("opus", CLAUDE_OPUS_4_5);
        REGISTRY.put("haiku", CLAUDE_3_5_HAIKU);
    }

    /**
     * Registers a model's token limits.
     *
     * @param limits the limits to register
     */
    public static void register(ModelTokenLimits limits) {
        REGISTRY.put(limits.model(), limits);
    }

    /**
     * Gets the token limits for a model.
     *
     * @param model the model identifier
     * @return the token limits, or DEFAULT if not found
     */
    public static ModelTokenLimits forModel(String model) {
        if (model == null) {
            return DEFAULT;
        }
        return REGISTRY.getOrDefault(model.toLowerCase(), DEFAULT);
    }

    /**
     * Returns the available tokens for context files after reserving for prompt and output.
     *
     * @return available tokens for context
     */
    public int availableForContext() {
        return contextWindow - outputLimit - reservedForPrompt;
    }

    /**
     * Returns the available tokens for context with a custom ratio.
     *
     * @param ratio the ratio of available context to use (0.0 to 1.0)
     * @return available tokens for context
     */
    public int availableForContext(double ratio) {
        if (ratio < 0.0 || ratio > 1.0) {
            throw new IllegalArgumentException("Ratio must be between 0.0 and 1.0");
        }
        return (int) (availableForContext() * ratio);
    }
}
