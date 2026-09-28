/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A declared minimum size that the element domain cannot reach must fail instead of silently
 * producing a smaller container.
 */
class UnreachableCollectionSizeTest {

    public static class ThreeFlags {
        @Size(min = 3, max = 3)
        public Set<Boolean> flags;
    }

    public static class ThreeFlagKeys {
        @Size(min = 3)
        public Map<Boolean, String> byFlag;
    }

    public static class FlexibleFlags {
        @Size(min = 1, max = 5)
        public Set<Boolean> flags;
    }

    /** Distinct by {@code equals}, but sorted by a {@code toString()} with two values. */
    public record Parity(String text) {
        @Override
        public String toString() {
            return text.length() % 2 == 0 ? "even" : "odd";
        }
    }

    public static class TwoParities {
        @Size(min = 2, max = 2)
        public SortedSet<Parity> parities;
    }

    /** Every value sorts as equal, so a sorted set holds at most one. */
    public record Constant(String text) {
        @Override
        public String toString() {
            return "constant";
        }
    }

    public static class TwoConstants {
        @Size(min = 2)
        public SortedSet<Constant> constants;
    }

    private static GeneratorConfig seeded() {
        return GeneratorConfig.builder().seed(42L).build();
    }

    @Test
    @DisplayName("a Set that cannot reach its declared minimum fails with the field path")
    void unreachableSetMinimumFails() {
        ObjectGenerator<ThreeFlags> generator = new ObjectGenerator<>(ThreeFlags.class, seeded());
        ObjectGenerationException error = assertThrows(ObjectGenerationException.class, generator::generate);
        assertTrue(error.getMessage().contains("ThreeFlags.flags"), error.getMessage());
    }

    @Test
    @DisplayName("a Map that cannot reach its declared minimum fails with the field path")
    void unreachableMapMinimumFails() {
        ObjectGenerator<ThreeFlagKeys> generator = new ObjectGenerator<>(ThreeFlagKeys.class, seeded());
        ObjectGenerationException error = assertThrows(ObjectGenerationException.class, generator::generate);
        assertTrue(error.getMessage().contains("ThreeFlagKeys.byFlag"), error.getMessage());
    }

    @Test
    @DisplayName("a sorted set replaces elements its order treats as equal")
    void sortedSetTopsUpOrderCollisions() {
        for (long seed = 0; seed < 30; seed++) {
            SortedSet<Parity> parities = new ObjectGenerator<>(
                TwoParities.class, GeneratorConfig.builder().seed(seed).build()).generate().parities;
            assertEquals(2, parities.size(), "seed " + seed + ": " + parities);
        }
    }

    @Test
    @DisplayName("a sorted set whose order collapses it below the declared minimum fails")
    void sortedSetCollapsedBelowMinimumFails() {
        ObjectGenerator<TwoConstants> generator = new ObjectGenerator<>(TwoConstants.class, seeded());
        ObjectGenerationException error = assertThrows(ObjectGenerationException.class, generator::generate);
        assertTrue(error.getMessage().contains("TwoConstants.constants"), error.getMessage());
    }

    @Test
    @DisplayName("ignored errors leave the unreachable container unset")
    void ignoredErrorsLeaveContainerUnset() {
        GeneratorConfig lenient = seeded().toBuilder().objectIgnoreErrors(true).build();
        assertNull(new ObjectGenerator<>(ThreeFlags.class, lenient).generate().flags);
        assertNull(new ObjectGenerator<>(ThreeFlagKeys.class, lenient).generate().byFlag);
    }

    @Test
    @DisplayName("a smaller container is fine while it still meets the declared minimum")
    void smallerContainerWithinDeclaredRangeIsKept() {
        for (long seed = 0; seed < 20; seed++) {
            Set<Boolean> flags = new ObjectGenerator<>(
                FlexibleFlags.class, GeneratorConfig.builder().seed(seed).build()).generate().flags;
            assertTrue(flags.size() >= 1 && flags.size() <= 2, "unexpected size " + flags.size());
        }
    }
}
