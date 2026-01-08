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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JTokkitTokenCounterTest {

    private JTokkitTokenCounter tokenCounter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        tokenCounter = new JTokkitTokenCounter();
    }

    @Test
    void shouldCountTokensInString() {
        String text = "Hello, world!";

        int count = tokenCounter.count(text);

        assertThat(count).isPositive();
        assertThat(count).isLessThan(10);
    }

    @Test
    void shouldCountTokensInFile() throws IOException {
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "This is a test file with some content for token counting.");

        int count = tokenCounter.count(file);

        assertThat(count).isPositive();
        assertThat(count).isLessThan(20);
    }

    @Test
    void shouldReturnZeroForEmptyString() {
        int count = tokenCounter.count("");

        assertThat(count).isZero();
    }

    @Test
    void shouldReturnZeroForEmptyFile() throws IOException {
        Path file = tempDir.resolve("empty.txt");
        Files.writeString(file, "");

        int count = tokenCounter.count(file);

        assertThat(count).isZero();
    }

    @Test
    void shouldHandleLongText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("This is sentence number ").append(i).append(". ");
        }

        int count = tokenCounter.count(sb.toString());

        assertThat(count).isGreaterThan(1000);
    }

    @Test
    void shouldHaveDefaultPriority() {
        assertThat(tokenCounter.priority()).isEqualTo(10);
    }

    @Test
    void shouldReportEncodingName() {
        assertThat(tokenCounter.getEncodingName()).isEqualTo("cl100k_base");
    }
}
