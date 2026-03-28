/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import jakarta.validation.constraints.Email;
import org.github.krandom.generator.Generator;
import org.github.krandom.generator.object.exception.ObjectGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertThrows(ObjectGenerationException.class,
                     () -> new ObjectGenerator<>(InvalidAnnotatedTarget.class).generate());
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
