/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Bean Validation text normalization")
class BeanValidationTextNormalizationTest {

    private static final Validator VALIDATOR =
        Validation.byProvider(HibernateValidator.class)
                  .configure()
                  .messageInterpolator(new ParameterMessageInterpolator())
                  .buildValidatorFactory()
                  .getValidator();

    @Test
    @DisplayName("compatible email, pattern, numeric, size, and blankness rules compose")
    void compatibleTextConstraintsCompose() {
        for (long seed = 0; seed < 32; seed++) {
            GeneratorConfig config = GeneratorConfig.builder().seed(seed).build();

            CompatibleTextFixture value = new ObjectGenerator<>(CompatibleTextFixture.class, config).generate();

            assertTrue(VALIDATOR.validate(value).isEmpty(), () -> VALIDATOR.validate(value).toString());
        }
    }

    @Test
    @DisplayName("repeatable patterns compose on record components and getter annotations")
    void repeatablePatternsComposeAcrossAnnotationSources() {
        PatternRecord record = new ObjectGenerator<>(PatternRecord.class).generate();
        GetterPatternFixture bean = new ObjectGenerator<>(GetterPatternFixture.class).generate();
        InterfacePatternFixture inherited = new ObjectGenerator<>(InterfacePatternFixture.class).generate();

        assertTrue(record.code().matches("[A-Z]{2}"));
        assertTrue(bean.code.matches("[A-Z]{3}"));
        assertTrue(inherited.code.matches("[A-Z]{4}"));
        assertTrue(VALIDATOR.validate(record).isEmpty(), () -> VALIDATOR.validate(record).toString());
        assertTrue(VALIDATOR.validate(bean).isEmpty(), () -> VALIDATOR.validate(bean).toString());
        assertTrue(VALIDATOR.validate(inherited).isEmpty(), () -> VALIDATOR.validate(inherited).toString());
    }

    @Test
    @DisplayName("incompatible repeatable patterns exhaust generation contextually")
    void incompatibleRepeatablePatternsFailContextually() {
        assertConflict(IncompatiblePatternsFixture.class,
                       "IncompatiblePatternsFixture.value",
                       String.class.getTypeName());
    }

    @Test
    @DisplayName("pattern, email, numeric, and blankness conflicts fail contextually")
    void incompatibleTextFamiliesFailContextually() {
        assertConflict(PatternSizeConflictFixture.class,
                       "PatternSizeConflictFixture.value",
                       String.class.getTypeName());
        assertConflict(ShortPatternSizeConflictFixture.class,
                       "ShortPatternSizeConflictFixture.value",
                       String.class.getTypeName());
        assertConflict(EmailSizeConflictFixture.class,
                       "EmailSizeConflictFixture.value",
                       String.class.getTypeName());
        assertConflict(NumericPatternConflictFixture.class,
                       "NumericPatternConflictFixture.value",
                       String.class.getTypeName());
        assertConflict(BlankPatternConflictFixture.class,
                       "BlankPatternConflictFixture.value",
                       String.class.getTypeName());
        assertConflict(NonNumericPatternConflictFixture.class,
                       "NonNumericPatternConflictFixture.value",
                       String.class.getTypeName());
        assertConflict(ExclusiveNumericLowerConflictFixture.class,
                       "ExclusiveNumericLowerConflictFixture.value",
                       String.class.getTypeName());
        assertConflict(NumericUpperConflictFixture.class,
                       "NumericUpperConflictFixture.value",
                       String.class.getTypeName());
        assertConflict(ExclusiveNumericUpperConflictFixture.class,
                       "ExclusiveNumericUpperConflictFixture.value",
                       String.class.getTypeName());
    }

    @Test
    @DisplayName("malformed patterns and unsupported targets fail before returning a fixture")
    void invalidPatternContractsFailContextually() {
        assertConflict(MalformedPatternFixture.class,
                       "MalformedPatternFixture.value",
                       String.class.getTypeName());
        assertConflict(UnsupportedPatternTargetFixture.class,
                       "UnsupportedPatternTargetFixture.value",
                       Integer.class.getTypeName());
        assertConflict(UnsupportedRegexSyntaxFixture.class,
                       "UnsupportedRegexSyntaxFixture.value",
                       String.class.getTypeName());
    }

    private static void assertConflict(Class<?> fixtureType, String path, String declaredType) {
        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(fixtureType).generate());

        var context = ex.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, context.category());
        assertEquals(path, context.path());
        assertEquals(declaredType, context.declaredType());
        assertNotNull(ex.getCause());
    }

    static final class CompatibleTextFixture {

        @Email
        @Pattern(regexp = "[a-z]{4,8}@[a-z]{3,8}\\.(com|net|org)")
        @Size(min = 12, max = 24)
        String email;

        @Email(regexp = "[a-z]{2,4}@example\\.com")
        @Size(min = 14, max = 16)
        String companyEmail;

        @Pattern(regexp = "[A-Z]{3}")
        @Pattern(regexp = "[A-Z0-9]{3}")
        @NotBlank
        @Size(min = 3, max = 3)
        String code;

        @Min(10)
        @Max(99)
        @Pattern(regexp = "\\d{2}")
        @Size(min = 2, max = 2)
        String numericCode;

        @Pattern(regexp = "[A-Z]{2}", flags = Pattern.Flag.CASE_INSENSITIVE)
        String flaggedCode;

        @Min(10)
        @Pattern(regexp = "(10|11)")
        String inclusiveNumericEndpoint;
    }

    record PatternRecord(
        @Pattern(regexp = "[A-Z]{2}")
        @Pattern(regexp = "[A-Z0-9]{2}")
        String code
    ) {
    }

    static final class GetterPatternFixture {

        String code;

        @Pattern(regexp = "[A-Z]{3}")
        @Pattern(regexp = "[A-Z0-9]{3}")
        @Size(min = 3, max = 3)
        String getCode() {
            return code;
        }
    }

    interface InterfacePatternContract {

        @Pattern(regexp = "[A-Z]{4}")
        @Pattern(regexp = "[A-Z0-9]{4}")
        String getCode();
    }

    static final class InterfacePatternFixture implements InterfacePatternContract {

        String code;

        @Override
        public String getCode() {
            return code;
        }
    }

    static final class IncompatiblePatternsFixture {

        @Pattern(regexp = "[A-Z]{2}")
        @Pattern(regexp = "\\d{2}")
        String value;
    }

    static final class PatternSizeConflictFixture {

        @Pattern(regexp = "\\d{5}")
        @Size(min = 2, max = 2)
        String value;
    }

    static final class ShortPatternSizeConflictFixture {

        @Pattern(regexp = "\\d{2}")
        @Size(min = 5, max = 5)
        String value;
    }

    static final class EmailSizeConflictFixture {

        @Email
        @Size(max = 5)
        String value;
    }

    static final class NumericPatternConflictFixture {

        @Min(100)
        @Max(999)
        @Pattern(regexp = "\\d{2}")
        String value;
    }

    static final class BlankPatternConflictFixture {

        @NotBlank
        @Pattern(regexp = "\\s{2}")
        String value;
    }

    static final class NonNumericPatternConflictFixture {

        @Min(1)
        @Pattern(regexp = "[A-Z]{2}")
        String value;
    }

    static final class ExclusiveNumericLowerConflictFixture {

        @jakarta.validation.constraints.DecimalMin(value = "10", inclusive = false)
        @Pattern(regexp = "10")
        String value;
    }

    static final class NumericUpperConflictFixture {

        @Max(9)
        @Pattern(regexp = "[5-9]\\d")
        String value;
    }

    static final class ExclusiveNumericUpperConflictFixture {

        @jakarta.validation.constraints.DecimalMax(value = "10", inclusive = false)
        @Pattern(regexp = "10")
        String value;
    }

    static final class MalformedPatternFixture {

        @Pattern(regexp = "[")
        String value;
    }

    static final class UnsupportedPatternTargetFixture {

        @Pattern(regexp = "\\d+")
        Integer value;
    }

    static final class UnsupportedRegexSyntaxFixture {

        @Pattern(regexp = "a{10001}")
        String value;
    }
}
