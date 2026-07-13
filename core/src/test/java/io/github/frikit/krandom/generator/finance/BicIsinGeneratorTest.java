/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Bic and Isin generators")
@SuppressWarnings("removal")
class BicIsinGeneratorTest {

    private static boolean isValidIsin(String isin) {
        String base = isin.substring(0, 11);
        int expected = IsinGenerator.computeCheckDigit(base);
        int actual = isin.charAt(11) - '0';
        return expected == actual;
    }

    @Test
    void bicGenerator() {
        BicGenerator gen = new BicGenerator(GeneratorConfig.builder() .locale(Locale.GERMANY) .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED) .build());
        String bic8 = gen.generate(false);
        String bic11 = gen.generate(true);
        String localeDefault = gen.generate(Locale.GERMANY);
        String swift = gen.generateSwift();
        String swift8 = gen.generateSwift8();
        String swift11 = gen.generateSwift11();
        assertTrue(bic8.matches("[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}"));
        assertTrue(bic11.matches("[A-Z]{4}[A-Z]{2}[A-Z0-9]{5}"));
        assertTrue(localeDefault.matches("[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?"));
        assertTrue(swift.matches("[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?"));
        assertTrue(swift8.matches("[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}"));
        assertTrue(swift11.matches("[A-Z]{4}[A-Z]{2}[A-Z0-9]{5}"));
        assertEquals("DE", bic8.substring(4, 6));
        assertThrows(NullPointerException.class, () -> new BicGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> gen.generate(null));
    }

    @Test
    void bicLocaleFallbackBranches() {
        BicGenerator gen = new BicGenerator(GeneratorConfig.builder()
                                                               .seed(5L)
                                                               .locale(Locale.US)
                                                               .bankingSafetyPolicy(
                                                                   BankingSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                               .build());
        // no country => fallback to US
        String langOnly = gen.generate(Locale.ENGLISH, false);
        assertEquals("US", langOnly.substring(4, 6));

        // non-2-letter country (numeric UN M.49 region) => fallback to US
        Locale numericRegion = new Locale.Builder().setLanguage("en").setRegion("001").build();
        String numeric = gen.generate(numericRegion, false);
        assertEquals("US", numeric.substring(4, 6));
    }

    @Test
    void isinGenerator() {
        IsinGenerator gen = new IsinGenerator(GeneratorConfig.builder() .locale(Locale.US) .securitiesIdentifierSafetyPolicy( SecuritiesIdentifierSafetyPolicy.REALISTIC_UNCLASSIFIED) .build());
        String isin = gen.generate();
        assertTrue(isin.matches("[A-Z]{2}[A-Z0-9]{9}\\d"));
        assertTrue(isValidIsin(isin));
        assertTrue(gen.generate(Locale.GERMANY).startsWith("DE"));
        assertThrows(NullPointerException.class, () -> new IsinGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> gen.generate(null));
        assertThrows(IllegalArgumentException.class, () -> IsinGenerator.computeCheckDigit("US12345-789"));
        assertThrows(IllegalArgumentException.class, () -> IsinGenerator.computeCheckDigit("US12345{789"));
    }

    @Test
    void isinLocaleFallbackBranches() {
        IsinGenerator gen = new IsinGenerator(GeneratorConfig.builder()
                                                              .seed(6L)
                                                              .locale(Locale.US)
                                                              .securitiesIdentifierSafetyPolicy(
                                                                  SecuritiesIdentifierSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                              .build());
        assertTrue(gen.generate(Locale.ENGLISH).startsWith("US"));
        Locale numericRegion = new Locale.Builder().setLanguage("en").setRegion("001").build();
        assertTrue(gen.generate(numericRegion).startsWith("US"));
    }

    @Test
    void configuredIsinGenerationFailsClosedByDefault() {
        assertThrows(IllegalStateException.class,
                     () -> new IsinGenerator(GeneratorConfig.defaults()).generate());
        assertThrows(IllegalStateException.class,
                     () -> new IsinGenerator(GeneratorConfig.defaults()).generate(Locale.US));
        assertThrows(IllegalStateException.class, () -> Generators.ofIsin().generate());

        GeneratorConfig config = GeneratorConfig.builder()
                                                 .securitiesIdentifierSafetyPolicy(
                                                     SecuritiesIdentifierSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                 .build();
        assertTrue(Generators.ofIsin(config).generate().matches("[A-Z]{2}[A-Z0-9]{9}\\d"));
    }
}
