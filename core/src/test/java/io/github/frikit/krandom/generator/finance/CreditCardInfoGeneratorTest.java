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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CreditCardInfoGenerator")
class CreditCardInfoGeneratorTest {

    @Test
    @DisplayName("generates a coherent structured credit card payload")
    void generateCreditCardInfo() {
        CreditCardInfo info = new CreditCardInfoGenerator(Locale.US).generate();

        assertNotNull(info);
        String unformatted = info.number().replaceAll("\\s", "");
        assertTrue(unformatted.matches("\\d+"));
        assertFalse(CreditCardGenerator.isValidLuhn(unformatted));
        assertTrue(!info.type().isBlank());
        assertTrue(info.exp().matches("\\d{2}/\\d{2}"));
        assertTrue(info.cvv().matches("\\d{3,4}"));
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seededGeneration() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(Locale.US)
                                                .seed(42L)
                                                .build();

        CreditCardInfoGenerator one = new CreditCardInfoGenerator(config);
        CreditCardInfoGenerator two = new CreditCardInfoGenerator(config);

        assertEquals(one.generate(), two.generate());
    }

    @Test
    @DisplayName("constructors and factories reject nulls and expose locale")
    void constructorValidation() {
        assertThrows(NullPointerException.class, () -> new CreditCardInfoGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new CreditCardInfoGenerator((GeneratorConfig) null));
        assertEquals(Locale.US, new CreditCardInfoGenerator(Locale.US).getLocale());
        assertNotNull(Generators.ofCreditCardInfo().generate());
        assertNotNull(Generators.ofCreditCardInfo(Locale.US).generate());
        assertNotNull(Generators.ofCreditCardInfo(GeneratorConfig.defaults()).generate());
    }
}
