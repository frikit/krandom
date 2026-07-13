/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationFailureDiagnostic;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Bean Validation normalization")
class BeanValidationNormalizationTest {

    private static final Validator VALIDATOR =
        Validation.byProvider(HibernateValidator.class)
                  .configure()
                  .messageInterpolator(new ParameterMessageInterpolator())
                  .buildValidatorFactory()
                  .getValidator();

    private static final GeneratorConfig REQUIRED_CONFIG = GeneratorConfig.builder()
                                                                           .seed(2301L)
                                                                           .collectionSize(0, 0)
                                                                           .objectNullProbability(1.0)
                                                                           .objectOptionalEmptyProbability(1.0)
                                                                           .objectSemanticMode(
                                                                               ObjectGenerationSemanticMode.STRICT)
                                                                           .build();

    @Test
    @DisplayName("required and non-empty constraints override null and empty defaults")
    void requiredConstraintsOverrideNullAndEmptyDefaults() {
        RequiredFixture value = new ObjectGenerator<>(RequiredFixture.class, REQUIRED_CONFIG).generate();

        assertNotNull(value.required);
        assertFalse(value.text.isEmpty());
        assertFalse(value.values.isEmpty());
        assertFalse(value.mapping.isEmpty());
        assertFalse(value.array.length == 0);
        assertFalse(value.nonBlank.isBlank());
        assertNotNull(value.optional);
        assertFalse(value.optional.isPresent());
        assertEquals(0, VALIDATOR.validate(value).size());
    }

    @Test
    @DisplayName("record components use the same required and non-empty rules")
    void recordComponentsUseSameRules() {
        RequiredRecord value = new ObjectGenerator<>(RequiredRecord.class, REQUIRED_CONFIG).generate();

        assertNotNull(value.required());
        assertFalse(value.values().isEmpty());
        assertEquals(0, VALIDATOR.validate(value).size());
    }

    @Test
    @DisplayName("recognized size constraint wins over strict semantic field inference")
    void sizeConstraintWinsOverStrictSemanticInference() {
        SizedSemanticFixture value = new ObjectGenerator<>(SizedSemanticFixture.class, REQUIRED_CONFIG).generate();

        assertEquals(2, value.email.length());
        assertEquals(0, VALIDATOR.validate(value).size());
    }

    @Test
    @DisplayName("null-only and required constraints fail with field context")
    void nullAndRequiredConflictFailsContextually() {
        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(NullAndRequiredFixture.class).generate());

        assertConflict(ex, "NullAndRequiredFixture.value", String.class.getTypeName());
    }

    @Test
    @DisplayName("inverted size range fails before generating a value")
    void invertedSizeRangeFailsContextually() {
        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(InvertedSizeFixture.class).generate());

        assertConflict(ex, "InvertedSizeFixture.values", "java.util.List<java.lang.String>");
    }

    @Test
    @DisplayName("not-empty intersected with max zero fails before generation")
    void notEmptyAndZeroMaxFailContextually() {
        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(EmptyIntersectionFixture.class).generate());

        assertConflict(ex, "EmptyIntersectionFixture.value", String.class.getTypeName());
    }

    @Test
    @DisplayName("null on a primitive fails before returning an invalid fixture")
    void nullPrimitiveFailsContextually() {
        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(NullPrimitiveFixture.class).generate());

        assertConflict(ex, "NullPrimitiveFixture.value", int.class.getTypeName());
    }

    @Test
    @DisplayName("not-empty on an unsupported target fails contextually")
    void notEmptyUnsupportedTargetFailsContextually() {
        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(UnsupportedNotEmptyFixture.class).generate());

        assertConflict(
            ex,
            "UnsupportedNotEmptyFixture.value",
            "java.util.Optional<java.lang.String>");
    }

    @Test
    @DisplayName("not-blank on a non-text target fails contextually")
    void notBlankUnsupportedTargetFailsContextually() {
        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(UnsupportedNotBlankFixture.class).generate());

        assertConflict(ex, "UnsupportedNotBlankFixture.value", Integer.class.getTypeName());
    }

    @Test
    @DisplayName("lenient conflicts return the type default and emit a diagnostic")
    void lenientConflictReturnsDefaultAndEmitsDiagnostic() {
        AtomicReference<GenerationFailureDiagnostic> observed = new AtomicReference<>();
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectIgnoreErrors(true)
                                                .generationFailureListener(observed::set)
                                                .build();

        NullAndRequiredFixture value = new ObjectGenerator<>(NullAndRequiredFixture.class, config).generate();

        assertNull(value.value);
        assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, observed.get().context().category());
        assertEquals("NullAndRequiredFixture.value", observed.get().context().path());
    }

    private static void assertConflict(ObjectGenerationException ex, String path, String declaredType) {
        var context = ex.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, context.category());
        assertEquals(path, context.path());
        assertEquals(declaredType, context.declaredType());
        assertNotNull(ex.getCause());
    }

    static final class RequiredFixture {

        @NotNull
        String required;

        @NotEmpty
        String text;

        @NotEmpty
        List<String> values;

        @NotEmpty
        Map<String, Integer> mapping;

        @NotEmpty
        String[] array;

        @NotBlank
        String nonBlank;

        @NotNull
        Optional<String> optional;
    }

    record RequiredRecord(@NotNull String required, @NotEmpty List<String> values) {
    }

    static final class SizedSemanticFixture {

        @Size(min = 2, max = 2)
        String email;
    }

    static final class NullAndRequiredFixture {

        @Null
        @NotNull
        String value;
    }

    static final class InvertedSizeFixture {

        @Size(min = 5, max = 2)
        List<String> values;
    }

    static final class EmptyIntersectionFixture {

        @NotEmpty
        @Size(max = 0)
        String value;
    }

    static final class NullPrimitiveFixture {

        @Null
        int value;
    }

    static final class UnsupportedNotEmptyFixture {

        @NotEmpty
        Optional<String> value;
    }

    static final class UnsupportedNotBlankFixture {

        @NotBlank
        Integer value;
    }
}
