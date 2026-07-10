/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Bean Validation published support matrix")
class BeanValidationSupportMatrixTest {

    private static final Clock CLOCK =
        Clock.fixed(Instant.parse("2026-07-09T12:00:00Z"), ZoneOffset.UTC);
    private static final Validator VALIDATOR =
        Validation.byProvider(HibernateValidator.class)
                  .configure()
                  .clockProvider(() -> CLOCK)
                  .messageInterpolator(new ParameterMessageInterpolator())
                  .buildValidatorFactory()
                  .getValidator();

    @Test
    @DisplayName("all 21 advertised constraints validate over a deterministic seed matrix")
    void allAdvertisedConstraintsValidateAcrossSeeds() {
        for (long seed = 0; seed < 64; seed++) {
            GeneratorConfig config = GeneratorConfig.builder().seed(seed).clock(CLOCK).build();

            AdvertisedConstraintFixture value =
                new ObjectGenerator<>(AdvertisedConstraintFixture.class, config).generate();

            assertTrue(VALIDATOR.validate(value).isEmpty(), () -> VALIDATOR.validate(value).toString());
        }
    }

    @Test
    @DisplayName("field, record, getter, and interface accessor sources share the contract")
    void allSupportedAnnotationSourcesValidate() {
        for (long seed = 0; seed < 16; seed++) {
            GeneratorConfig config = GeneratorConfig.builder().seed(seed).build();
            SourceRecord record = new ObjectGenerator<>(SourceRecord.class, config).generate();
            GetterSource bean = new ObjectGenerator<>(GetterSource.class, config).generate();
            InterfaceSource inherited = new ObjectGenerator<>(InterfaceSource.class, config).generate();

            assertTrue(VALIDATOR.validate(record).isEmpty(), () -> VALIDATOR.validate(record).toString());
            assertTrue(VALIDATOR.validate(bean).isEmpty(), () -> VALIDATOR.validate(bean).toString());
            assertTrue(VALIDATOR.validate(inherited).isEmpty(), () -> VALIDATOR.validate(inherited).toString());
        }
    }

    @Test
    @DisplayName("unadvertised Jakarta constraints remain ordinary metadata")
    void unsupportedConstraintIsNotClaimedByNormalization() throws Exception {
        Field field = UnsupportedMetadataFixture.class.getDeclaredField("value");

        BeanValidationSupport.ConstraintModel model =
            BeanValidationSupport.constraintModelFor(field, Integer.class);

        assertFalse(model.constrained());
        assertNull(BeanValidationSupport.constraintGeneratorFor(field, Integer.class));
    }

    static final class AdvertisedConstraintFixture {

        @AssertFalse
        boolean disabled;

        @AssertTrue
        Boolean enabled;

        @DecimalMin(value = "1.25", inclusive = false)
        @DecimalMax(value = "1.75", inclusive = false)
        BigDecimal decimal;

        @Email
        @Pattern(regexp = "[a-z]{4,8}@[a-z]{3,8}\\.(com|net|org)")
        @Size(min = 12, max = 24)
        String email;

        @Future
        Instant future;

        @FutureOrPresent
        LocalDate futureOrPresent;

        @Min(10)
        @Max(20)
        int bounded;

        @Negative
        int negative;

        @NegativeOrZero
        long negativeOrZero;

        @NotBlank
        String text;

        @NotEmpty
        List<String> items;

        @NotNull
        Optional<String> optional;

        @Null
        String absent;

        @Past
        Date past;

        @PastOrPresent
        Calendar pastOrPresent;

        @Positive
        BigInteger positive;

        @PositiveOrZero
        double positiveOrZero;
    }

    record SourceRecord(
        @NotBlank
        @Pattern(regexp = "[A-Z]{2}")
        @Size(min = 2, max = 2)
        String code
    ) {
    }

    static final class GetterSource {

        String email;

        @Email
        @Size(min = 12, max = 24)
        String getEmail() {
            return email;
        }
    }

    interface InterfaceContract {

        @Pattern(regexp = "[A-Z]{3}")
        @Pattern(regexp = "[A-Z0-9]{3}")
        String getCode();
    }

    static final class InterfaceSource implements InterfaceContract {

        String code;

        @Override
        public String getCode() {
            return code;
        }
    }

    static final class UnsupportedMetadataFixture {

        @Digits(integer = 3, fraction = 0)
        Integer value;
    }
}
