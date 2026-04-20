/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.provider;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TextFormatProvider")
class TextFormatProviderTest {

    @Test
    @DisplayName("seeded providers produce repeatable formatting sequences")
    void seededProvidersProduceRepeatableFormattingSequences() {
        GeneratorConfig config = GeneratorConfig.builder().seed(99L).build();
        TextFormatProvider first = new TextFormatProvider(config);
        TextFormatProvider second = new TextFormatProvider(config);

        assertEquals(first.lexify("??"), second.lexify("??"));
        assertEquals(first.lexify("??", true), second.lexify("??", true));
        assertEquals(first.template("??-##"), second.template("??-##"));
        assertEquals(first.asciify("***"), second.asciify("***"));
        assertEquals(first.asciify("___", '_'), second.asciify("___", '_'));
        assertEquals(first.regexify("[A-Z]{2}\\d{3}"), second.regexify("[A-Z]{2}\\d{3}"));
    }

    @Test
    @DisplayName("formatting helpers respect their placeholder semantics")
    void formattingHelpersRespectPlaceholderSemantics() {
        TextFormatProvider provider = new TextFormatProvider(GeneratorConfig.builder().seed(7L).build());

        assertTrue(provider.numerify("##-##").matches("\\d{2}-\\d{2}"));
        assertTrue(provider.lexify("??").matches("[a-z]{2}"));
        assertTrue(provider.lexify("??", true).matches("[A-Z]{2}"));
        assertTrue(provider.bothify("??-##").matches("[a-z]{2}-\\d{2}"));
        assertTrue(provider.asciify("***").chars().noneMatch(ch -> ch == '*'));
        assertTrue(provider.asciify("___", '_').chars().noneMatch(ch -> ch == '_'));
        assertTrue(provider.regexify("(foo|bar)\\d{2}").matches("(foo|bar)\\d{2}"));
    }

    @Test
    @DisplayName("formatting helpers validate null input")
    void formattingHelpersValidateNullInput() {
        TextFormatProvider provider = new TextFormatProvider();

        assertThrows(NullPointerException.class, () -> provider.template(null));
        assertThrows(NullPointerException.class, () -> provider.numerify(null));
        assertThrows(NullPointerException.class, () -> provider.lexify(null));
        assertThrows(NullPointerException.class, () -> provider.lexify(null, true));
        assertThrows(NullPointerException.class, () -> provider.bothify(null));
        assertThrows(NullPointerException.class, () -> provider.asciify(null));
        assertThrows(NullPointerException.class, () -> provider.asciify(null, '_'));
        assertThrows(NullPointerException.class, () -> provider.regexify(null));
        assertThrows(NullPointerException.class, () -> new TextFormatProvider(null));
    }
}
