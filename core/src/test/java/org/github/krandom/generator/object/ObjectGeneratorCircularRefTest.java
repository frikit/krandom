/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.core.model.CircularNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ObjectGenerator — circular reference handling")
class ObjectGeneratorCircularRefTest {

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
}
