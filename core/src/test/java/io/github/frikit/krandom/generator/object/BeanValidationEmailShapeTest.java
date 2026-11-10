/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.locale.SupportedLocale;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Locale;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** An {@code @Email} field, checked by the validator a consumer runs rather than by matching it here. */
@DisplayName("Bean Validation email shape")
class BeanValidationEmailShapeTest {

    private static final Validator VALIDATOR = Validation.byProvider(HibernateValidator.class)
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory()
            .getValidator();

    @Test
    @DisplayName("an @Email field takes from a pattern only what Hibernate Validator's @Email accepts")
    void emailFieldTakesFromPatternOnlyWhatHibernateValidatorAccepts() {
        List<Locale> locales = SupportedLocale.locales();
        for (int index = 0; index < locales.size(); index++) {
            GeneratorConfig config = GeneratorConfig.builder()
                    .locale(locales.get(index))
                    .seed(1_000L + index)
                    .build();
            ObjectGenerator<LoosePatterns> generator = new ObjectGenerator<>(LoosePatterns.class, config);
            for (int draw = 0; draw < 20; draw++) {
                LoosePatterns value = generator.generate();

                assertTrue(
                        VALIDATOR.validate(value).isEmpty(),
                        () -> VALIDATOR.validate(value).toString());
            }
        }
    }

    @Test
    @DisplayName("an @Email field takes a domain without a dot, which Hibernate Validator accepts")
    void emailFieldTakesDomainWithoutDot() {
        for (long seed = 0; seed < 32; seed++) {
            GeneratorConfig config = GeneratorConfig.builder().seed(seed).build();

            DotlessDomain value = new ObjectGenerator<>(DotlessDomain.class, config).generate();

            assertTrue(value.email.matches("[a-z]{3,6}@localhost"), value.email);
            assertTrue(
                    VALIDATOR.validate(value).isEmpty(),
                    () -> VALIDATOR.validate(value).toString());
        }
    }

    @Test
    @DisplayName("an @Email field refuses a quoted local part or an address literal, with its path")
    void emailFieldRefusesQuotedLocalPartAndAddressLiteral() {
        assertConflict(QuotedLocalPart.class, "QuotedLocalPart.email");
        assertConflict(AddressLiteral.class, "AddressLiteral.email");
    }

    private static void assertConflict(Class<?> fixtureType, String path) {
        ObjectGenerationException ex =
                assertThrows(ObjectGenerationException.class, () -> new ObjectGenerator<>(fixtureType).generate());

        var context = ex.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, context.category());
        assertEquals(path, context.path());
        assertEquals(String.class.getTypeName(), context.declaredType());
        assertNotNull(ex.getCause());
    }

    static final class LoosePatterns {

        // Dots and dashes where an address cannot have them, as in a..b@c-.com, a@c.-com and a@c..com.
        @Email @Pattern(regexp = "[a-z]{1,3}\\.{0,2}[a-z]{1,3}@[a-z]{1,4}[-.]{1,2}[a-z]{2,3}") String separators;

        // A local part of up to 70 characters, where 64 is the most.
        @Email @Pattern(regexp = "[a-z]{60,70}@example\\.com") String longLocalPart;

        // A label of up to 66 characters, where 63 is the most.
        @Email @Pattern(regexp = "[a-z]{3}@[a-z]{60,66}\\.com") String longLabel;

        // A domain of 254 to 264 characters, where 255 is the most.
        @Email @Pattern(regexp = "[a-z]{3}@([a-z]{60}\\.){4}[a-z]{10,20}")
        String longDomain;

        // A domain ending in a dot.
        @Email @Pattern(regexp = "[a-z]{3}@example\\.com\\.?") String trailingDot;
    }

    static final class DotlessDomain {

        @Email @Pattern(regexp = "[a-z]{3,6}@localhost") String email;
    }

    static final class QuotedLocalPart {

        @Email @Pattern(regexp = "\"[a-z]{3}\"@example\\.com") String email;
    }

    static final class AddressLiteral {

        @Email @Pattern(regexp = "[a-z]{3}@\\[192\\.0\\.2\\.[1-9]\\]") String email;
    }
}
