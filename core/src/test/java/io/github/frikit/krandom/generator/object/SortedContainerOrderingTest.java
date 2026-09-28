/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.PriorityQueue;
import java.util.SortedMap;
import java.util.SortedSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sorted containers of comparable elements must use the elements' natural ordering, not their
 * text form: a {@code SortedSet<Integer>} sorts numerically.
 */
class SortedContainerOrderingTest {

    public record Point(int x, int y) {
    }

    public static class Scores {
        public SortedSet<Integer> scores;
        public NavigableMap<Integer, String> byScore;
        public PriorityQueue<Integer> queue;
        public SortedSet<Point> points;
    }

    /** Inherits {@link Object#toString()}, so a textual order would depend on identity hash codes. */
    public static class Tag {
        public String label;
    }

    public static class TagSet {
        public SortedSet<Tag> tags;
    }

    public static class TagQueue {
        public PriorityQueue<Tag> tags;
    }

    public static class TagKeys {
        public SortedMap<Tag, String> byTag;
    }

    public static class Tasks {
        public SortedSet<Runnable> tasks;
    }

    private static <T> void assertUnsupported(Class<T> type) {
        ObjectGenerator<T> generator = new ObjectGenerator<>(type, GeneratorConfig.builder().seed(1L).build());
        ObjectGenerationException error = assertThrows(ObjectGenerationException.class, generator::generate);
        assertTrue(error.getMessage().startsWith("Unsupported type"), error.getMessage());
    }

    private static Scores generate(long seed) {
        GeneratorConfig config = GeneratorConfig.builder().seed(seed).collectionSize(5, 5).build();
        return new ObjectGenerator<>(Scores.class, config).generate();
    }

    private static List<Integer> sorted(Iterable<Integer> values) {
        List<Integer> copy = new ArrayList<>();
        values.forEach(copy::add);
        copy.sort(null);
        return copy;
    }

    @Test
    @DisplayName("SortedSet<Integer> and NavigableMap<Integer, ?> iterate in numeric order")
    void comparableElementsUseNaturalOrdering() {
        for (long seed = 0; seed < 10; seed++) {
            Scores fixture = generate(seed);
            assertNull(fixture.scores.comparator());
            assertEquals(sorted(fixture.scores), new ArrayList<>(fixture.scores));
            assertNull(fixture.byScore.comparator());
            assertEquals(sorted(fixture.byScore.keySet()), new ArrayList<>(fixture.byScore.keySet()));
        }
    }

    @Test
    @DisplayName("PriorityQueue<Integer> polls in numeric order")
    void priorityQueueUsesNaturalOrdering() {
        PriorityQueue<Integer> queue = generate(3L).queue;
        assertNull(queue.comparator());
        List<Integer> polled = new ArrayList<>();
        while (!queue.isEmpty()) {
            polled.add(queue.poll());
        }
        assertEquals(sorted(polled), polled);
    }

    @Test
    @DisplayName("non-comparable elements with a value-based toString keep a stable textual ordering")
    void nonComparableElementsKeepTextualOrdering() {
        SortedSet<Point> points = generate(4L).points;
        assertNotNull(points.comparator());
        assertFalse(points.isEmpty());
        assertEquals(new ArrayList<>(points), new ArrayList<>(generate(4L).points));
    }

    @Test
    @DisplayName("sorted containers of identity-ordered elements fail instead of ordering by hash code")
    void identityOrderedElementsAreUnsupported() {
        assertUnsupported(TagSet.class);
        assertUnsupported(TagQueue.class);
        assertUnsupported(TagKeys.class);
        assertUnsupported(Tasks.class);
    }

    @Test
    @DisplayName("ignored errors leave identity-ordered sorted containers unset")
    void ignoredErrorsLeaveIdentityOrderedContainersUnset() {
        GeneratorConfig lenient = GeneratorConfig.builder().seed(1L).objectIgnoreErrors(true).build();
        assertNull(new ObjectGenerator<>(TagSet.class, lenient).generate().tags);
        assertNull(new ObjectGenerator<>(TagQueue.class, lenient).generate().tags);
        assertNull(new ObjectGenerator<>(TagKeys.class, lenient).generate().byTag);
    }
}
