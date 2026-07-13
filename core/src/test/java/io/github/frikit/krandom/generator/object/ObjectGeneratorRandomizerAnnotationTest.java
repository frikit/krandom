/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import jakarta.validation.constraints.Email;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationFailureContext;
import io.github.frikit.krandom.generator.failure.GenerationOperation;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ObjectGenerator — @Randomizer")
class ObjectGeneratorRandomizerAnnotationTest {

    @Test
    @DisplayName("@Randomizer applies custom generator to annotated field")
    void randomizerAnnotationAppliesToField() {
        AnnotatedFieldTarget target = new ObjectGenerator<>(AnnotatedFieldTarget.class).generate();
        assertEquals("ANNOTATED", target.getToken());
        assertNotNull(target.getName());
    }

    @Test
    @DisplayName("field override takes precedence over @Randomizer annotation")
    void fieldOverrideWinsOverRandomizerAnnotation() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .override(OverridePrecedenceTarget.class, "value", () -> "FIELD_OVERRIDE")
                                                            .build();
        OverridePrecedenceTarget target = new ObjectGenerator<>(OverridePrecedenceTarget.class, config).generate();
        assertEquals("FIELD_OVERRIDE", target.getValue());
    }

    @Test
    @DisplayName("type override takes precedence over @Randomizer annotation")
    void typeOverrideWinsOverRandomizerAnnotation() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                            .override(String.class, () -> "TYPE_OVERRIDE")
                                                            .build();
        OverridePrecedenceTarget target = new ObjectGenerator<>(OverridePrecedenceTarget.class, config).generate();
        assertEquals("TYPE_OVERRIDE", target.getValue());
    }

    @Test
    @DisplayName("@Randomizer takes precedence over bean validation constraints")
    void randomizerWinsOverBeanValidation() {
        BeanValidationAnnotatedTarget target = new ObjectGenerator<>(BeanValidationAnnotatedTarget.class).generate();
        assertEquals("ANNOTATED", target.getEmail());
    }

    @Test
    @DisplayName("@Randomizer applies to record components")
    void randomizerAppliesToRecordComponents() {
        AnnotatedRecordTarget target = new ObjectGenerator<>(AnnotatedRecordTarget.class).generate();
        assertEquals("ANNOTATED", target.token());
    }

    @Test
    @DisplayName("invalid @Randomizer generator type throws ObjectGenerationException")
    void invalidRandomizerTypeThrows() {
        ObjectGenerationException error = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(InvalidAnnotatedTarget.class).generate());

        var context = error.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.CUSTOM_GENERATOR, context.category());
        assertEquals(GenerationOperation.CONSTRUCT, context.operation());
        assertEquals("InvalidAnnotatedTarget.value", context.path());
        assertEquals(NoDefaultCtorGenerator.class.getName(), context.declaredType());
        assertTrue(error.getCause() instanceof NoSuchMethodException);
    }

    @Test
    @DisplayName("throwing @Randomizer generator reports sanitized field context")
    void throwingRandomizerIsContextual() {
        ObjectGenerationException error = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(ThrowingRandomizerTarget.class).generate());

        var context = error.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.CUSTOM_GENERATOR, context.category());
        assertEquals(GenerationOperation.GENERATE, context.operation());
        assertEquals("ThrowingRandomizerTarget.value", context.path());
        assertEquals(ThrowingGenerator.class.getName(), context.declaredType());
        assertTrue(error.getCause() instanceof IllegalStateException);
        assertFalse(error.getMessage().contains("personal-looking-value"));
    }

    @Test
    @DisplayName("legacy uncontextual @Randomizer failure gains field context")
    void legacyRandomizerFailureGainsContext() {
        ObjectGenerationException error = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(LegacyFailingRandomizerTarget.class).generate());

        assertEquals("LegacyFailingRandomizerTarget.value", error.getContext().orElseThrow().path());
        assertTrue(error.getCause() instanceof ObjectGenerationException);
        assertFalse(error.getMessage().contains("personal-looking-value"));
    }

    @Test
    @DisplayName("already-contextual @Randomizer failure is not wrapped again")
    void contextualRandomizerFailurePassesThrough() {
        ObjectGenerationException error = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(ContextualFailingRandomizerTarget.class).generate());

        assertSame(ContextualFailingGenerator.FAILURE, error);
        assertEquals("Nested.path", error.getContext().orElseThrow().path());
    }

    @Test
    @DisplayName("@RandomizerArgument binds constructor arguments for annotated randomizer")
    void randomizerArgumentsBindConstructor() {
        RandomizerArgumentTarget target = new ObjectGenerator<>(RandomizerArgumentTarget.class).generate();
        assertEquals("pre-7-SWEET", target.getValue());
    }

    @Test
    @DisplayName("invalid @RandomizerArgument values throw ObjectGenerationException")
    void invalidRandomizerArgumentsThrow() {
        assertThrows(ObjectGenerationException.class,
                     () -> new ObjectGenerator<>(InvalidRandomizerArgumentTarget.class).generate());
    }

    @Test
    @DisplayName("@RandomizerArgument supports char conversion")
    void randomizerArgumentCharConversion() {
        CharRandomizerArgumentTarget target = new ObjectGenerator<>(CharRandomizerArgumentTarget.class).generate();
        assertEquals("Z", target.getValue());
    }

    @Test
    @DisplayName("invalid char @RandomizerArgument values throw ObjectGenerationException")
    void invalidCharRandomizerArgumentsThrow() {
        assertThrows(ObjectGenerationException.class,
                     () -> new ObjectGenerator<>(InvalidCharRandomizerArgumentTarget.class).generate());
    }

    @Test
    @DisplayName("unsupported @RandomizerArgument type throws ObjectGenerationException")
    void unsupportedRandomizerArgumentTypeThrows() {
        assertThrows(ObjectGenerationException.class,
                     () -> new ObjectGenerator<>(UnsupportedTypeRandomizerArgumentTarget.class).generate());
    }

    @Test
    @DisplayName("@Randomizer applies to inherited fields")
    void randomizerAppliesToInheritedFields() {
        InheritedAnnotatedTarget target = new ObjectGenerator<>(InheritedAnnotatedTarget.class).generate();
        assertEquals("ANNOTATED", target.getInheritedToken());
        assertNotNull(target.getLocalField());
    }


    enum Flavor {
        SWEET,
        SALTY
    }


    public static class ConstantValueGenerator implements Generator<String> {

        @Override
        public String generate() {
            return "ANNOTATED";
        }
    }


    public static class NoDefaultCtorGenerator implements Generator<String> {

        public NoDefaultCtorGenerator(String ignored) {
        }

        @Override
        public String generate() {
            return "INVALID";
        }
    }


    public static class ThrowingGenerator implements Generator<String> {

        @Override
        public String generate() {
            throw new IllegalStateException("personal-looking-value");
        }
    }


    public static class LegacyFailingGenerator implements Generator<String> {

        @Override
        public String generate() {
            throw new ObjectGenerationException("personal-looking-value");
        }
    }


    public static class ContextualFailingGenerator implements Generator<String> {

        private static final ObjectGenerationException FAILURE = new ObjectGenerationException(
            "nested failure",
            new GenerationFailureContext(
                GenerationFailureCategory.ASSIGNMENT,
                GenerationOperation.ASSIGN,
                "Nested.path",
                ContextualFailingGenerator.class,
                String.class.getName(),
                2,
                -1),
            new IllegalStateException("nested cause"));

        @Override
        public String generate() {
            throw FAILURE;
        }
    }


    public static class PrefixGenerator implements Generator<String> {

        private final String prefix;
        private final int    count;
        private final Flavor flavor;

        public PrefixGenerator(String prefix, int count, Flavor flavor) {
            this.prefix = prefix;
            this.count = count;
            this.flavor = flavor;
        }

        @Override
        public String generate() {
            return prefix + "-" + count + "-" + flavor.name();
        }
    }


    public static class CharGenerator implements Generator<String> {

        private final char value;

        public CharGenerator(char value) {
            this.value = value;
        }

        @Override
        public String generate() {
            return String.valueOf(value);
        }
    }


    public static class UnsupportedArgTypeGenerator implements Generator<String> {

        public UnsupportedArgTypeGenerator(Class<?> ignored) {
        }

        @Override
        public String generate() {
            return "UNUSED";
        }
    }


    static class AnnotatedFieldTarget {

        @Randomizer(ConstantValueGenerator.class)
        private String token;
        private String name;

        String getToken() {
            return token;
        }

        String getName() {
            return name;
        }
    }


    static class OverridePrecedenceTarget {

        @Randomizer(ConstantValueGenerator.class)
        private String value;

        String getValue() {
            return value;
        }
    }


    static class BeanValidationAnnotatedTarget {

        @Email
        @Randomizer(ConstantValueGenerator.class)
        private String email;

        String getEmail() {
            return email;
        }
    }


    static class InvalidAnnotatedTarget {

        @Randomizer(NoDefaultCtorGenerator.class)
        private String value;
    }


    static class ThrowingRandomizerTarget {

        @Randomizer(ThrowingGenerator.class)
        private String value;
    }


    static class LegacyFailingRandomizerTarget {

        @Randomizer(LegacyFailingGenerator.class)
        private String value;
    }


    static class ContextualFailingRandomizerTarget {

        @Randomizer(ContextualFailingGenerator.class)
        private String value;
    }


    static class RandomizerArgumentTarget {

        @Randomizer(PrefixGenerator.class)
        @RandomizerArgument(type = String.class, value = "pre")
        @RandomizerArgument(type = int.class, value = "7")
        @RandomizerArgument(type = Flavor.class, value = "SWEET")
        private String value;

        String getValue() {
            return value;
        }
    }


    static class InvalidRandomizerArgumentTarget {

        @Randomizer(PrefixGenerator.class)
        @RandomizerArgument(type = String.class, value = "pre")
        @RandomizerArgument(type = int.class, value = "NaN")
        @RandomizerArgument(type = Flavor.class, value = "SWEET")
        private String value;
    }


    static class CharRandomizerArgumentTarget {

        @Randomizer(CharGenerator.class)
        @RandomizerArgument(type = char.class, value = "Z")
        private String value;

        String getValue() {
            return value;
        }
    }


    static class InvalidCharRandomizerArgumentTarget {

        @Randomizer(CharGenerator.class)
        @RandomizerArgument(type = char.class, value = "ZZ")
        private String value;
    }


    static class UnsupportedTypeRandomizerArgumentTarget {

        @Randomizer(UnsupportedArgTypeGenerator.class)
        @RandomizerArgument(type = Class.class, value = "java.lang.String")
        private String value;
    }


    static class BaseAnnotatedTarget {

        @Randomizer(ConstantValueGenerator.class)
        private String inheritedToken;

        String getInheritedToken() {
            return inheritedToken;
        }
    }


    static class InheritedAnnotatedTarget extends BaseAnnotatedTarget {

        private String localField;

        String getLocalField() {
            return localField;
        }
    }


    record AnnotatedRecordTarget(@Randomizer(ConstantValueGenerator.class) String token) {

    }
}
