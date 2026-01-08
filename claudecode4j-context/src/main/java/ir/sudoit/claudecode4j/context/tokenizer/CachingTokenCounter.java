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

import ir.sudoit.claudecode4j.context.spi.TokenCounter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A caching wrapper around a TokenCounter that caches file token counts.
 *
 * <p>Cache entries are invalidated when the file's last modified time changes. Text-based counting is not cached as
 * it's typically not repeated.
 */
public final class CachingTokenCounter implements TokenCounter {

    private final TokenCounter delegate;
    private final ConcurrentMap<Path, CacheEntry> cache;

    /**
     * Creates a new caching token counter wrapping the given delegate.
     *
     * @param delegate the underlying token counter
     */
    public CachingTokenCounter(TokenCounter delegate) {
        this.delegate = delegate;
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public int count(String text) {
        // Don't cache text-based counting
        return delegate.count(text);
    }

    @Override
    public int count(Path file) throws IOException {
        Path absolutePath = file.toAbsolutePath().normalize();
        FileTime currentModTime = Files.getLastModifiedTime(absolutePath);

        CacheEntry cached = cache.get(absolutePath);
        if (cached != null && cached.lastModified().equals(currentModTime)) {
            return cached.tokenCount();
        }

        int tokenCount = delegate.count(absolutePath);
        cache.put(absolutePath, new CacheEntry(tokenCount, currentModTime));
        return tokenCount;
    }

    @Override
    public String getEncodingName() {
        return delegate.getEncodingName();
    }

    @Override
    public int priority() {
        return delegate.priority() + 1; // Slightly higher priority than delegate
    }

    /** Clears all cached token counts. */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Removes a specific file from the cache.
     *
     * @param file the file to remove from cache
     */
    public void invalidate(Path file) {
        cache.remove(file.toAbsolutePath().normalize());
    }

    /**
     * Returns the number of cached entries.
     *
     * @return the cache size
     */
    public int cacheSize() {
        return cache.size();
    }

    private record CacheEntry(int tokenCount, FileTime lastModified) {}
}
