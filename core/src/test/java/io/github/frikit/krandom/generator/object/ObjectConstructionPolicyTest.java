/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    @DisplayName("declared constructor parameters use Bean Validation normalization")
    void constructorParametersUseConstraintNormalization() {
        ConstrainedConstructorFixture value =
            new ObjectGenerator<>(ConstrainedConstructorFixture.class).generate();

        assertNotNull(value.code);
        assertEquals(4, value.code.length());
        assertFalse(value.code.isBlank());
    }

    @Test
    @DisplayName("unsupported root shapes fail before allocation under the selected policy")
    void unsupportedRootShapesFailBeforeAllocation() {
        assertUnsupportedRoot(AbstractFixture.class, ObjectConstructionPolicy.SAFE_CONSTRUCTORS);
        assertUnsupportedRoot(InterfaceFixture.class, ObjectConstructionPolicy.SAFE_CONSTRUCTORS);
        assertUnsupportedRoot(NonStaticInnerFixture.class, ObjectConstructionPolicy.SAFE_CONSTRUCTORS);
        assertUnsupportedRoot(String[].class, ObjectConstructionPolicy.SAFE_CONSTRUCTORS);
        assertUnsupportedRoot(int.class, ObjectConstructionPolicy.SAFE_CONSTRUCTORS);
        assertUnsupportedRoot(EnumFixture.class, ObjectConstructionPolicy.SAFE_CONSTRUCTORS);
        assertUnsupportedRoot(AnnotationFixture.class, ObjectConstructionPolicy.SAFE_CONSTRUCTORS);

        class LocalFixture {
        }
        assertUnsupportedRoot(LocalFixture.class, ObjectConstructionPolicy.SAFE_CONSTRUCTORS);

        Object anonymous = new Object() {};
        assertUnsupportedRoot(anonymous.getClass(), ObjectConstructionPolicy.SAFE_CONSTRUCTORS);
    }

    @Test
    @DisplayName("unsafe policy is named when an unsupported root cannot be allocated")
    void unsafeUnsupportedRootNamesPolicy() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectConstructionPolicy(
                                                    ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS)
                                                .build();

        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(AbstractFixture.class, config).generate());

        assertEquals(GenerationFailureCategory.CONSTRUCTION, ex.getContext().orElseThrow().category());
        assertTrue(ex.getCause().getMessage().contains(
            ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS.name()));
    }

    private static void assertUnsupportedRoot(Class<?> type, ObjectConstructionPolicy policy) {
        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(type).generate());

        assertEquals(GenerationFailureCategory.CONSTRUCTION, ex.getContext().orElseThrow().category());
        assertTrue(ex.getCause().getMessage().contains(policy.name()));
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

    static final class ConstrainedConstructorFixture {

        final String code;

        ConstrainedConstructorFixture(@NotBlank @Size(min = 4, max = 4) String code) {
            this.code = code;
        }
    }

    abstract static class AbstractFixture {
    }

    interface InterfaceFixture {
    }

    enum EnumFixture { VALUE }

    @interface AnnotationFixture {
    }

    final class NonStaticInnerFixture {
    }
}
