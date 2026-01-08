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
package ir.sudoit.claudecode4j.context.optimizer;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/** Strategy for ranking context files to determine priority for inclusion. */
public interface RankingStrategy {

    /**
     * Ranks the given files and returns them in priority order (highest priority first).
     *
     * @param files the files to rank
     * @return the files in priority order
     */
    List<Path> rank(List<Path> files);

    /**
     * Returns the name of this strategy.
     *
     * @return the strategy name
     */
    String name();

    /**
     * Creates a ranking strategy that sorts by file modification time (most recent first).
     *
     * @return a recency-based ranking strategy
     */
    static RankingStrategy recency() {
        return new RecencyRankingStrategy();
    }

    /**
     * Creates a ranking strategy that sorts by file size (smallest first).
     *
     * @return a size-based ranking strategy
     */
    static RankingStrategy size() {
        return new SizeRankingStrategy();
    }

    /**
     * Creates a ranking strategy that preserves the original order.
     *
     * @return an order-preserving ranking strategy
     */
    static RankingStrategy preserveOrder() {
        return new PreserveOrderStrategy();
    }

    /**
     * Creates a ranking strategy using a custom comparator.
     *
     * @param name the strategy name
     * @param comparator the comparator for ranking
     * @return a custom ranking strategy
     */
    static RankingStrategy custom(String name, Comparator<Path> comparator) {
        return new CustomRankingStrategy(name, comparator);
    }
}
