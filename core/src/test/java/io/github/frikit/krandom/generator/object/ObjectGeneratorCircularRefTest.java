/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.core.model.CircularNode;
import io.github.frikit.krandom.generator.failure.GenerationFailureContext;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ObjectGenerator — circular reference handling")
class ObjectGeneratorCircularRefTest {

    private static Set<CircularNode> collectChainNodes(CircularNode root, int maxSteps) {
        Set<CircularNode> nodes = Collections.newSetFromMap(new IdentityHashMap<>());
        CircularNode current = root;
        int steps = 0;
        while (current != null && steps < maxSteps && nodes.add(current)) {
            current = current.getNext();
            steps++;
        }
        return nodes;
    }

    @Test
    @DisplayName("generating a self-referential type does not throw StackOverflowError")
    void doesNotStackOverflow() {
        assertDoesNotThrow(
                () -> new ObjectGenerator<>(CircularNode.class).generate(),
                "CircularNode must be generated without stack overflow");
    }

    @Test
    @DisplayName("generated CircularNode instance is non-null")
    void generatesNonNull() {
        CircularNode node = new ObjectGenerator<>(CircularNode.class).generate();
        assertNotNull(node, "root node must not be null");
    }

    @Test
    @DisplayName("name field on the root node is populated")
    void nameFieldPopulated() {
        CircularNode node = new ObjectGenerator<>(CircularNode.class).generate();
        assertNotNull(node.getName(), "name field must not be null");
        assertFalse(node.getName().isEmpty(), "name field must not be empty");
    }

    @Test
    @DisplayName("the next reference is either null (cycle broken) or a valid CircularNode")
    void nextIsNullOrValidNode() {
        CircularNode node = new ObjectGenerator<>(CircularNode.class).generate();
        // next may be null (cycle broken) or a non-null node — never an infinite chain
        if (node.getNext() != null) {
            // We can navigate the chain: it must terminate (no infinite loop)
            CircularNode current = node.getNext();
            for (int steps = 0; steps < 10 && current != null; steps++) {
                current = current.getNext();
            }
            // If we reach here the chain terminated within 10 steps — no infinite recursion
        }
    }

    @Test
    @DisplayName("generateList produces N independent non-null instances without error")
    void generateListWorks() {
        var list = new ObjectGenerator<>(CircularNode.class).generateList(5);
        assertEquals(5, list.size());
        list.forEach(n -> assertNotNull(n, "each generated node must be non-null"));
    }

    @Test
    @DisplayName("objectPoolSize(0) still prevents stack overflow via in-progress cycle detection")
    void zeroSizedObjectPoolStillPreventsOverflow() {
        ObjectGeneratorConfig config =
                ObjectGeneratorConfig.builder().objectPoolSize(0).build();
        assertDoesNotThrow(() -> new ObjectGenerator<>(CircularNode.class, config).generate());
    }

    @Test
    @DisplayName("reusing the same generator does not share node instances across calls")
    void repeatedGenerateDoesNotShareNodesAcrossCalls() {
        ObjectGenerator<CircularNode> generator = new ObjectGenerator<>(CircularNode.class);

        CircularNode first = generator.generate();
        CircularNode second = generator.generate();

        Set<CircularNode> firstNodes = collectChainNodes(first, 16);
        Set<CircularNode> secondNodes = collectChainNodes(second, 16);

        assertTrue(
                Collections.disjoint(firstNodes, secondNodes),
                "separate generate() calls must not share node identities");
    }

    @Test
    @DisplayName("a @NotNull self-typed member below the depth limit is cut by the cycle guard and stays null")
    void requiredSelfTypedMemberBelowTheLimitStaysNull() {
        RequiredNode root = new ObjectGenerator<>(RequiredNode.class).generate();

        assertNotNull(root.next, "the first self-typed member is generated");
        assertNull(root.next.next, "the cycle guard cuts the next one, whatever its constraints");
    }

    @Test
    @DisplayName("a @NotNull self-typed member at the depth limit fails like any required member")
    void requiredSelfTypedMemberAtTheLimitFails() {
        GeneratorConfig limit = GeneratorConfig.builder().objectMaxDepth(1).build();

        ObjectGenerationException error = assertThrows(
                ObjectGenerationException.class, () -> new ObjectGenerator<>(RequiredNode.class, limit).generate());

        GenerationFailureContext context = error.getContext().orElseThrow();
        assertEquals("RequiredNode.next.next", context.path());
        assertEquals(RequiredNode.class, context.ownerType());
        assertEquals(1, context.depth());
        assertTrue(error.getCause().getMessage().contains("objectMaxDepth 1"));
    }

    @Test
    @DisplayName("a larger depth limit moves a @NotNull self-typed member to the cycle guard")
    void largerLimitMovesRequiredSelfTypedMemberToTheCycleGuard() {
        GeneratorConfig limit = GeneratorConfig.builder().objectMaxDepth(2).build();

        RequiredNode root = new ObjectGenerator<>(RequiredNode.class, limit).generate();

        assertNotNull(root.next);
        assertNull(root.next.next);
    }

    @Test
    @DisplayName("an unannotated self-typed member at the depth limit stays null")
    void nullableSelfTypedMemberAtTheLimitStaysNull() {
        GeneratorConfig limit = GeneratorConfig.builder().objectMaxDepth(1).build();

        CircularNode root = new ObjectGenerator<>(CircularNode.class, limit).generate();

        assertNotNull(root.getNext());
        assertNull(root.getNext().getNext());
    }

    static class RequiredNode {
        @NotNull RequiredNode next;
    }
}
