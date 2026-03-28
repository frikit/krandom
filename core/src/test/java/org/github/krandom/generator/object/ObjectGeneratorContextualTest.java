/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.ContextualGenerator;
import org.github.krandom.generator.core.model.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ObjectGenerator — context-aware generators")
class ObjectGeneratorContextualTest {

    private static final int SAMPLES = 20;

    @Test
    @DisplayName("contextual type override receives field name")
    void contextualTypeOverrideHasFieldName() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .override(String.class, (ContextualGenerator<String>) ctx -> ctx.getFieldName() + "_value")
                                                            .build();
        Person p = new ObjectGenerator<>(Person.class, config).generate();
        assertNotNull(p.getFirstName());
        assertTrue(p.getFirstName().endsWith("_value"),
                   "Expected firstName to end with '_value', got: " + p.getFirstName());
    }

    @Test
    @DisplayName("contextual field override receives owner type")
    void contextualFieldOverrideHasOwnerType() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .override(Person.class, "firstName",
                                                                      (ContextualGenerator<String>) ctx -> ctx.getOwnerType().getSimpleName() + "-name")
                                                            .build();
        Person p = new ObjectGenerator<>(Person.class, config).generate();
        assertEquals("Person-name", p.getFirstName());
    }

    @Test
    @DisplayName("contextual type override at root has depth 0")
    void contextualTypeOverrideAtRootHasDepthZero() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .override(String.class, (ContextualGenerator<String>) ctx -> "d" + ctx.getDepth())
                                                            .build();
        for (int i = 0; i < SAMPLES; i++) {
            Person p = new ObjectGenerator<>(Person.class, config).generate();
            // Root-level String fields should have depth 0
            assertEquals("d0", p.getFirstName(),
                         "Expected depth 0 for root field, got: " + p.getFirstName());
        }
    }

    @Test
    @DisplayName("contextual field override takes precedence over contextual type override")
    void fieldOverrideTakesPrecedenceOverTypeOverride() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .override(String.class, (ContextualGenerator<String>) ctx -> "type-level")
                                                            .override(Person.class, "firstName",
                                                                      (ContextualGenerator<String>) ctx -> "field-level")
                                                            .build();
        Person p = new ObjectGenerator<>(Person.class, config).generate();
        assertEquals("field-level", p.getFirstName());
    }

    @Test
    @DisplayName("contextual override is called once per field across generateList")
    void contextualOverrideCalledForEachInstance() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .override(Person.class, "firstName",
                                                                      (ContextualGenerator<String>) ctx -> "generated-" + ctx.getFieldName())
                                                            .build();
        var list = new ObjectGenerator<>(Person.class, config).generateList(SAMPLES);
        list.forEach(p -> assertEquals("generated-firstName", p.getFirstName()));
    }
}
