/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.schema;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FieldLookup")
class FieldLookupTest {

    @Test
    @DisplayName("supported references are exposed")
    void supportedReferences() {
        FieldLookup lookup = new FieldLookup(GeneratorConfig.defaults());
        Set<String> refs = lookup.supportedReferences();
        assertTrue(refs.contains("person.full_name"));
        assertTrue(refs.contains("finance.currency_iso_code"));
        assertTrue(refs.contains("code.uuid4"));
    }

    @Test
    @DisplayName("resolves and generates values for all built-in references")
    void resolveAllBuiltins() {
        GeneratorConfig config = GeneratorConfig.builder().seed(77L).locale(Locale.US).build();
        FieldLookup lookup = new FieldLookup(config);
        SchemaContext context = new SchemaContext(Locale.US, new Random(1L), 0);

        for (String ref : lookup.supportedReferences()) {
            Object value = lookup.resolve(ref).generate(context);
            assertNotNull(value, "Expected non-null for " + ref);
        }
    }

    @Test
    @DisplayName("validation for null, blank and unknown references")
    void validation() {
        FieldLookup lookup = new FieldLookup(GeneratorConfig.defaults());
        assertThrows(NullPointerException.class, () -> lookup.resolve(null));
        assertThrows(IllegalArgumentException.class, () -> lookup.resolve(" "));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> lookup.resolve("person.unknown"));
        assertTrue(ex.getMessage().contains("Supported references"));
    }

    @Test
    @DisplayName("constructor rejects null config")
    void nullConfig() {
        assertThrows(NullPointerException.class, () -> new FieldLookup(null));
    }
}
