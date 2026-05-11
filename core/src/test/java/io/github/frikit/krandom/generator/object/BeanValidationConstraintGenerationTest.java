/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Bean Validation constraint generation")
class BeanValidationConstraintGenerationTest {

    private static final int SAMPLES = 20;

    private static final Validator VALIDATOR =
        Validation.byProvider(HibernateValidator.class)
                  .configure()
                  .messageInterpolator(new ParameterMessageInterpolator())
                  .buildValidatorFactory()
                  .getValidator();

    @Test
    @DisplayName("generated objects satisfy field and getter constraints with Hibernate Validator")
    void generatedObjectsSatisfyBeanValidationConstraints() {
        ObjectGenerator<ConstraintFixture> generator = new ObjectGenerator<>(ConstraintFixture.class);

        for (int i = 0; i < SAMPLES; i++) {
            ConstraintFixture fixture = generator.generate();

            assertTrue(VALIDATOR.validate(fixture).isEmpty(), () -> violationsFor(fixture));
        }
    }

    private static String violationsFor(ConstraintFixture fixture) {
        return VALIDATOR.validate(fixture)
                        .stream()
                        .map(BeanValidationConstraintGenerationTest::formatViolation)
                        .collect(Collectors.joining(", "));
    }

    private static String formatViolation(ConstraintViolation<ConstraintFixture> violation) {
        return violation.getPropertyPath() + "=" + violation.getInvalidValue() + " " + violation.getMessage();
    }

    static final class ConstraintFixture {

        @AssertFalse
        boolean disabled;

        @AssertTrue
        Boolean enabled;

        @DecimalMin("1.50")
        BigDecimal decimalMinOnly;

        @DecimalMax("9.50")
        BigDecimal decimalMaxOnly;

        @Email
        String email;

        @Future
        Instant futureInstant;

        @FutureOrPresent
        LocalDate futureOrPresentDate;

        @Max(10)
        byte maxByte;

        @Min(3)
        short minShort;

        @Negative
        int negativeInt;

        @NegativeOrZero
        long negativeOrZeroLong;

        @NotBlank
        String notBlank;

        @Null
        String absent;

        @Past
        Date pastDate;

        @PastOrPresent
        Calendar pastOrPresentCalendar;

        @Pattern(regexp = "[A-Z]{2}\\d{3}")
        String code;

        @Positive
        BigInteger positiveBigInteger;

        @PositiveOrZero
        double positiveOrZeroDouble;

        @Size(min = 2, max = 4)
        String sizedText;

        @Size(min = 2, max = 4)
        String[] tags;

        @Size(min = 2, max = 4)
        List<Integer> scores;

        @Size(min = 2, max = 4)
        LinkedList<Integer> linkedScores;

        @Size(min = 2, max = 4)
        Set<String> labels;

        @Size(min = 2, max = 4)
        Queue<Integer> queue;

        @Size(min = 2, max = 4)
        Map<String, Integer> counts;

        String getterSizedText;

        @Size(min = 6, max = 6)
        public String getGetterSizedText() {
            return getterSizedText;
        }
    }
}
