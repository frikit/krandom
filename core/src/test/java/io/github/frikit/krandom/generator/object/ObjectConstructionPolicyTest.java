/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Object construction policy")
class ObjectConstructionPolicyTest {

    @BeforeEach
    void resetConstructorCounters() {
        UniqueConstructorFixture.constructorCalls = 0;
        AmbiguousConstructorFixture.constructorCalls = 0;
    }

    @Test
    @DisplayName("safe constructors are the default and round-trip through configuration")
    void safePolicyIsTheRoundTrippingDefault() {
        GeneratorConfig defaults = GeneratorConfig.defaults();
        assertSame(ObjectConstructionPolicy.SAFE_CONSTRUCTORS, defaults.getObjectConstructionPolicy());
        assertSame(ObjectConstructionPolicy.SAFE_CONSTRUCTORS,
                   defaults.toBuilder().build().getObjectConstructionPolicy());

        GeneratorConfig unsafe = GeneratorConfig.builder()
                                                .objectConstructionPolicy(
                                                    ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS)
                                                .build();
        ObjectGeneratorConfig mapped = ObjectGeneratorConfig.builder().generatorConfig(unsafe).build();

        assertSame(ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS, mapped.getConstructionPolicy());
        assertSame(ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS,
                   mapped.toGeneratorConfig().getObjectConstructionPolicy());
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().objectConstructionPolicy(null));
    }

    @Test
    @DisplayName("safe mode invokes one declared constructor and preserves its final invariant")
    void safeModeInvokesUniqueDeclaredConstructor() {
        UniqueConstructorFixture value = new ObjectGenerator<>(UniqueConstructorFixture.class).generate();

        assertEquals(1, UniqueConstructorFixture.constructorCalls);
        assertNotNull(value.required);
        assertTrue(value.invariantEstablished);
    }

    @Test
    @DisplayName("safe mode rejects ambiguous declared constructors contextually")
    void safeModeRejectsAmbiguousConstructors() {
        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(AmbiguousConstructorFixture.class).generate());

        assertEquals(0, AmbiguousConstructorFixture.constructorCalls);
        assertEquals(GenerationFailureCategory.CONSTRUCTION, ex.getContext().orElseThrow().category());
        assertTrue(ex.getCause().getMessage().contains(ObjectConstructionPolicy.SAFE_CONSTRUCTORS.name()));
    }

    @Test
    @DisplayName("unsafe bypass remains explicit and preserves legacy constructor skipping")
    void unsafeModeExplicitlyBypassesConstructors() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectConstructionPolicy(
                                                    ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS)
                                                .build();

        UniqueConstructorFixture value =
            new ObjectGenerator<>(UniqueConstructorFixture.class, config).generate();

        assertEquals(0, UniqueConstructorFixture.constructorCalls);
        assertNull(value.required);
    }

    static final class UniqueConstructorFixture {

        static int constructorCalls;

        final String required;
        final boolean invariantEstablished;

        private UniqueConstructorFixture(String required) {
            constructorCalls++;
            this.required = java.util.Objects.requireNonNull(required);
            this.invariantEstablished = true;
        }
    }

    static final class AmbiguousConstructorFixture {

        static int constructorCalls;

        String value;

        AmbiguousConstructorFixture(String value) {
            constructorCalls++;
            this.value = value;
        }

        AmbiguousConstructorFixture(int value) {
            constructorCalls++;
            this.value = Integer.toString(value);
        }
    }
}
