/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.apache.commons.validator.routines.IBANValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Isolated
@DisplayName("IBAN output does not depend on the JVM default locale")
class IbanJvmDefaultLocaleTest {

    private Locale originalDefaultLocale;

    @BeforeEach
    void rememberDefaultLocale() {
        originalDefaultLocale = Locale.getDefault();
    }

    @AfterEach
    void restoreDefaultLocale() {
        Locale.setDefault(originalDefaultLocale);
    }

    @Test
    @DisplayName("check digits stay ASCII under a JVM default locale with native digits")
    void checkDigitsStayAsciiUnderNativeDigitDefaultLocale() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .seed(11L)
                                                .locale(Locale.GERMANY)
                                                .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                .build();
        Locale.setDefault(Locale.ROOT);
        String expected = new IbanGenerator(config).generate();

        Locale.setDefault(Locale.forLanguageTag("ar-SA"));
        String actual = new IbanGenerator(config).generate();

        assertEquals(expected, actual);
        assertTrue(actual.matches("DE[0-9]{20}"), actual);
        assertTrue(IBANValidator.getInstance().isValid(actual), actual);
    }
}
