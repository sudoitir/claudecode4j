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
package ir.sudoit.claudecode4j.context.spi;

import java.io.IOException;
import java.nio.file.Path;

/**
 * SPI for counting tokens in text content.
 *
 * <p>Implementations should use an appropriate tokenizer for Claude models. The cl100k_base encoding is recommended as
 * it closely matches Claude's tokenization.
 */
public interface TokenCounter {

    /**
     * Counts the number of tokens in the given text.
     *
     * @param text the text to tokenize
     * @return the token count
     */
    int count(String text);

    /**
     * Counts the number of tokens in a file.
     *
     * @param file the file to tokenize
     * @return the token count
     * @throws IOException if the file cannot be read
     */
    int count(Path file) throws IOException;

    /**
     * Returns the name of the encoding used by this counter.
     *
     * @return the encoding name (e.g., "cl100k_base")
     */
    String getEncodingName();

    /**
     * Returns the priority of this implementation for SPI selection. Higher priority implementations are preferred.
     *
     * @return the priority (default: 0)
     */
    default int priority() {
        return 0;
    }
}
