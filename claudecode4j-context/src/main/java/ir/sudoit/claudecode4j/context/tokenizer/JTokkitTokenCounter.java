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
package ir.sudoit.claudecode4j.context.tokenizer;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import ir.sudoit.claudecode4j.context.spi.TokenCounter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Token counter using JTokkit library with cl100k_base encoding.
 *
 * <p>The cl100k_base encoding is used by GPT-4 and closely matches Claude's tokenization. This provides a reasonable
 * approximation for token counting.
 */
public final class JTokkitTokenCounter implements TokenCounter {

    private static final String ENCODING_NAME = "cl100k_base";

    private final Encoding encoding;

    /** Creates a new JTokkit-based token counter using cl100k_base encoding. */
    public JTokkitTokenCounter() {
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        this.encoding = registry.getEncoding(EncodingType.CL100K_BASE);
    }

    @Override
    public int count(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return encoding.countTokens(text);
    }

    @Override
    public int count(Path file) throws IOException {
        if (!Files.exists(file)) {
            throw new IOException("File does not exist: " + file);
        }
        if (!Files.isRegularFile(file)) {
            throw new IOException("Not a regular file: " + file);
        }
        String content = Files.readString(file);
        return count(content);
    }

    @Override
    public String getEncodingName() {
        return ENCODING_NAME;
    }

    @Override
    public int priority() {
        return 10; // Higher priority than default implementations
    }
}
