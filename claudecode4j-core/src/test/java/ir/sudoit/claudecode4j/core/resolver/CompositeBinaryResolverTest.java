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
package ir.sudoit.claudecode4j.core.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ir.sudoit.claudecode4j.api.spi.BinaryResolver;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CompositeBinaryResolver")
class CompositeBinaryResolverTest {

    @Nested
    @DisplayName("resolve")
    class Resolve {

        @Test
        @DisplayName("should return first successful resolution from applicable resolvers sorted by priority")
        void shouldReturnFirstSuccessfulResolution() {
            var lowPriorityResolver = createMockResolver("low", 10, true, Optional.of(Path.of("/low/claude")));
            var highPriorityResolver = createMockResolver("high", 100, true, Optional.of(Path.of("/high/claude")));
            var midPriorityResolver = createMockResolver("mid", 50, true, Optional.of(Path.of("/mid/claude")));

            var composite = new CompositeBinaryResolver(
                    List.of(lowPriorityResolver, highPriorityResolver, midPriorityResolver));
            var result = composite.resolve();

            assertThat(result).isPresent().contains(Path.of("/high/claude"));
        }

        @Test
        @DisplayName("should skip non-applicable resolvers")
        void shouldSkipNonApplicableResolvers() {
            var applicableResolver =
                    createMockResolver("applicable", 50, true, Optional.of(Path.of("/applicable/claude")));
            var nonApplicableResolver =
                    createMockResolver("non-applicable", 100, false, Optional.of(Path.of("/non-applicable/claude")));

            var composite = new CompositeBinaryResolver(List.of(applicableResolver, nonApplicableResolver));
            var result = composite.resolve();

            assertThat(result).isPresent().contains(Path.of("/applicable/claude"));
            verify(nonApplicableResolver, never()).resolve();
        }

        @Test
        @DisplayName("should return empty when all resolvers return empty")
        void shouldReturnEmptyWhenAllResolversReturnEmpty() {
            var resolver1 = createMockResolver("r1", 100, true, Optional.empty());
            var resolver2 = createMockResolver("r2", 50, true, Optional.empty());

            var composite = new CompositeBinaryResolver(List.of(resolver1, resolver2));
            var result = composite.resolve();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty when no resolvers are provided")
        void shouldReturnEmptyWhenNoResolvers() {
            var composite = new CompositeBinaryResolver(List.of());
            var result = composite.resolve();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should fallback to next resolver when higher priority fails")
        void shouldFallbackToNextResolver() {
            var failingHighPriority = createMockResolver("failing-high", 100, true, Optional.empty());
            var successfulLowPriority =
                    createMockResolver("successful-low", 50, true, Optional.of(Path.of("/low/claude")));

            var composite = new CompositeBinaryResolver(List.of(failingHighPriority, successfulLowPriority));
            var result = composite.resolve();

            assertThat(result).isPresent().contains(Path.of("/low/claude"));
        }

        @Test
        @DisplayName("should work with default constructor")
        void shouldWorkWithDefaultConstructor() {
            var composite = new CompositeBinaryResolver();
            // Just verify it doesn't throw and has a valid name
            assertThat(composite.name()).startsWith("composite[");
        }
    }

    @Nested
    @DisplayName("priority")
    class Priority {

        @Test
        @DisplayName("should return max priority of all resolvers")
        void shouldReturnMaxPriority() {
            var lowPriority = createMockResolver("low", 10, true, Optional.empty());
            var highPriority = createMockResolver("high", 100, true, Optional.empty());

            var composite = new CompositeBinaryResolver(List.of(lowPriority, highPriority));

            assertThat(composite.priority()).isEqualTo(100);
        }

        @Test
        @DisplayName("should return 0 when no resolvers")
        void shouldReturnZeroWhenNoResolvers() {
            var composite = new CompositeBinaryResolver(List.of());
            assertThat(composite.priority()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("name")
    class Name {

        @Test
        @DisplayName("should return composite name with all resolver names")
        void shouldReturnCompositeNameWithAllResolverNames() {
            var resolver1 = createMockResolver("npm-global", 100, true, Optional.empty());
            var resolver2 = createMockResolver("PATH", 50, true, Optional.empty());

            var composite = new CompositeBinaryResolver(List.of(resolver1, resolver2));

            assertThat(composite.name()).isEqualTo("composite[npm-global,PATH]");
        }

        @Test
        @DisplayName("should handle empty resolvers list")
        void shouldHandleEmptyResolversList() {
            var composite = new CompositeBinaryResolver(List.of());
            assertThat(composite.name()).isEqualTo("composite[]");
        }
    }

    private BinaryResolver createMockResolver(String name, int priority, boolean applicable, Optional<Path> result) {
        var resolver = mock(BinaryResolver.class);
        when(resolver.name()).thenReturn(name);
        when(resolver.priority()).thenReturn(priority);
        when(resolver.isApplicable()).thenReturn(applicable);
        when(resolver.resolve()).thenReturn(result);
        return resolver;
    }
}
