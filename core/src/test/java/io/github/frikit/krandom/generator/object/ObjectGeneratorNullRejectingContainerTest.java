/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationFailureContext;
import io.github.frikit.krandom.generator.failure.GenerationFailureDiagnostic;
import io.github.frikit.krandom.generator.failure.GenerationOperation;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("ObjectGenerator — containers that cannot hold a cut nested object")
class ObjectGeneratorNullRejectingContainerTest {

    record Leaf(String value) {}

    record RankedLeaf(String value) implements Comparable<RankedLeaf> {
        @Override
        public int compareTo(RankedLeaf other) {
            return value.compareTo(other.value);
        }
    }

    record QueueOwner(Queue<Leaf> values) {}

    record QueueTop(QueueOwner mid) {}

    record DequeOwner(Deque<Leaf> values) {}

    record DequeTop(DequeOwner mid) {}

    record ArrayDequeOwner(ArrayDeque<Leaf> values) {}

    record ArrayDequeTop(ArrayDequeOwner mid) {}

    record PriorityOwner(PriorityQueue<RankedLeaf> values) {}

    record PriorityTop(PriorityOwner mid) {}

    record SortedOwner(SortedSet<RankedLeaf> values) {}

    record SortedTop(SortedOwner mid) {}

    record TreeSetOwner(TreeSet<RankedLeaf> values) {}

    record TreeSetTop(TreeSetOwner mid) {}

    record LinkedOwner(LinkedList<Leaf> values) {}

    record LinkedTop(LinkedOwner mid) {}

    record Node(String name, Queue<Node> children) {}

    private static GeneratorConfig limitedTo(int maxDepth) {
        return GeneratorConfig.builder()
                .seed(5521L)
                .objectMaxDepth(maxDepth)
                .collectionSize(2, 2)
                .build();
    }

    static Stream<Arguments> nullRejectingContainers() {
        return Stream.of(
                Arguments.of(QueueTop.class, "QueueOwner", (Function<Object, Object>)
                        top -> ((QueueTop) top).mid().values()),
                Arguments.of(DequeTop.class, "DequeOwner", (Function<Object, Object>)
                        top -> ((DequeTop) top).mid().values()),
                Arguments.of(ArrayDequeTop.class, "ArrayDequeOwner", (Function<Object, Object>)
                        top -> ((ArrayDequeTop) top).mid().values()),
                Arguments.of(PriorityTop.class, "PriorityOwner", (Function<Object, Object>)
                        top -> ((PriorityTop) top).mid().values()),
                Arguments.of(SortedTop.class, "SortedOwner", (Function<Object, Object>)
                        top -> ((SortedTop) top).mid().values()),
                Arguments.of(TreeSetTop.class, "TreeSetOwner", (Function<Object, Object>)
                        top -> ((TreeSetTop) top).mid().values()));
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("nullRejectingContainers")
    @DisplayName("a container that cannot hold the null left at objectMaxDepth fails with its path")
    void strictModeReportsTheContainer(Class<?> top, String owner, Function<Object, Object> values) {
        ObjectGenerationException error = assertThrows(
                ObjectGenerationException.class, () -> new ObjectGenerator<>(top, limitedTo(1)).generate());

        GenerationFailureContext context = error.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.COLLECTION_INSERTION, context.category());
        assertEquals(GenerationOperation.INSERT, context.operation());
        assertEquals(top.getSimpleName() + ".mid.values", context.path());
        assertInstanceOf(NullPointerException.class, error.getCause());
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("nullRejectingContainers")
    @DisplayName("objectIgnoreErrors discards the whole container and tells the listener")
    void lenientModeDiscardsTheContainer(Class<?> top, String owner, Function<Object, Object> values) {
        List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();
        GeneratorConfig lenient = limitedTo(1).toBuilder()
                .objectIgnoreErrors(true)
                .generationFailureListener(diagnostics::add)
                .build();

        Object generated = new ObjectGenerator<>(top, lenient).generate();

        assertNull(values.apply(generated));
        assertEquals(
                List.of(owner + ".values"),
                diagnostics.stream().map(d -> d.context().path()).toList());
        assertEquals(
                GenerationFailureCategory.COLLECTION_INSERTION,
                diagnostics.getFirst().context().category());
    }

    @Test
    @DisplayName("a LinkedList is a list, so it keeps the null elements left at objectMaxDepth")
    void linkedListKeepsNullElements() {
        LinkedTop top = new ObjectGenerator<>(LinkedTop.class, limitedTo(1)).generate();

        assertEquals(Arrays.asList(null, null), top.mid().values());
    }

    @Test
    @DisplayName("a queue cannot hold the null of a cut cycle either")
    void queueCannotHoldACutCycle() {
        ObjectGenerationException error = assertThrows(
                ObjectGenerationException.class, () -> new ObjectGenerator<>(Node.class, limitedTo(5)).generate());

        GenerationFailureContext context = error.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.COLLECTION_INSERTION, context.category());
        assertEquals("Node.children[].children", context.path());
    }
}
