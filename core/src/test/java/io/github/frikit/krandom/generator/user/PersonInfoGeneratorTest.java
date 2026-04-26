/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PersonInfoGenerator")
class PersonInfoGeneratorTest {

    @Test
    @DisplayName("generates a coherent structured person payload")
    void generatePersonInfo() {
        PersonInfo info = new PersonInfoGenerator(Locale.US).generate();

        assertNotNull(info);
        assertNotNull(info.contact());
        assertNotNull(info.address());
        assertEquals(info.username(), info.contact().email().substring(0, info.contact().email().indexOf('@')));
        assertTrue(info.password().length() >= 8);
        assertEquals("US", info.address().countryAbbr());
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seededGeneration() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(Locale.UK)
                                                .seed(42L)
                                                .build();

        PersonInfoGenerator one = new PersonInfoGenerator(config);
        PersonInfoGenerator two = new PersonInfoGenerator(config);

        assertEquals(one.generate(), two.generate());
    }

    @Test
    @DisplayName("constructors and factories reject nulls and expose locale")
    void constructorValidation() {
        assertThrows(NullPointerException.class, () -> new PersonInfoGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new PersonInfoGenerator((GeneratorConfig) null));
        assertEquals(Locale.US, new PersonInfoGenerator(Locale.US).getLocale());
        assertNotNull(Generators.ofPersonInfo().generate());
        assertNotNull(Generators.ofPersonInfo(Locale.US).generate());
        assertNotNull(Generators.ofPersonInfo(GeneratorConfig.defaults()).generate());
    }
}
