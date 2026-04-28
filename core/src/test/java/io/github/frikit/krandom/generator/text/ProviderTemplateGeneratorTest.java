/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.text;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.schema.FieldLookup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProviderTemplateGenerator")
class ProviderTemplateGeneratorTest {

    @Test
    @DisplayName("resolves provider aliases and canonical tokens")
    void resolvesProviderAliasesAndCanonicalTokens() {
        ProviderTemplateGenerator generator = new ProviderTemplateGenerator(
            "{firstname} {person.last_name} <{email}>",
            GeneratorConfig.builder().locale(Locale.US).seed(12L).build());

        String value = generator.generate();

        assertTrue(value.matches(".+ .+ <.+@.+\\..+>"), value);
        assertTrue(!value.contains("{") && !value.contains("}"), value);
    }

    @Test
    @DisplayName("keeps literal formatting semantics")
    void keepsLiteralFormattingSemantics() {
        ProviderTemplateGenerator generator = new ProviderTemplateGenerator("ID-##-^^-{firstname}", 5L);

        assertTrue(generator.generate().matches("ID-\\d{2}-[0-9a-f]{2}-.+"));
    }

    @Test
    @DisplayName("supports custom registered tokens")
    void supportsCustomRegisteredTokens() {
        FieldLookup lookup = new FieldLookup(GeneratorConfig.defaults());
        lookup.register("fixture.id", ctx -> "fixture-" + ctx.recordIndex());
        ProviderTemplateGenerator generator = new ProviderTemplateGenerator("{fixture.id}", lookup);

        assertEquals("fixture-0", generator.generate());
        assertEquals("fixture-1", generator.generate());
    }

    @Test
    @DisplayName("literal braces are escaped")
    void literalBracesAreEscaped() {
        FieldLookup lookup = new FieldLookup(GeneratorConfig.defaults());
        lookup.register("value", ctx -> "x");
        ProviderTemplateGenerator generator = new ProviderTemplateGenerator("{{literal}} {value}", lookup);

        assertEquals("{literal} x", generator.generate());
        assertEquals("}", new ProviderTemplateGenerator("}").generate());
        assertEquals("}x", new ProviderTemplateGenerator("}x").generate());
    }

    @Test
    @DisplayName("unknown or malformed tokens fail with useful errors")
    void unknownOrMalformedTokensFailWithUsefulErrors() {
        IllegalArgumentException unknown = assertThrows(
            IllegalArgumentException.class,
            () -> new ProviderTemplateGenerator("{missing.token}").generate());
        assertTrue(unknown.getMessage().contains("Unknown field reference"));

        assertThrows(IllegalArgumentException.class, () -> new ProviderTemplateGenerator("{ }").generate());
        assertThrows(IllegalArgumentException.class, () -> new ProviderTemplateGenerator("{").generate());
        assertThrows(IllegalArgumentException.class, () -> new ProviderTemplateGenerator("{firstname").generate());
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seededGenerationIsReproducible() {
        ProviderTemplateGenerator first = new ProviderTemplateGenerator("{firstname}-##", 22L);
        ProviderTemplateGenerator second = new ProviderTemplateGenerator("{firstname}-##", 22L);

        assertEquals(first.generate(), second.generate());
        assertEquals(first.generate(), second.generate());
    }

    @Test
    @DisplayName("facade exposes provider template generator")
    void facadeExposesProviderTemplateGenerator() {
        assertNotNull(Generators.ofProviderTemplate("{firstname}").generate());
        assertNotNull(Generators.ofProviderTemplate("{firstname}", 1L).generate());
        assertNotNull(Generators.ofProviderTemplate("{firstname}", GeneratorConfig.defaults()).generate());
        assertNotNull(Generators.ofProviderTemplate("{firstname}", new FieldLookup(GeneratorConfig.defaults())).generate());
    }

    @Test
    @DisplayName("null inputs fail fast")
    void nullInputsFailFast() {
        assertThrows(NullPointerException.class, () -> new ProviderTemplateGenerator(null));
        assertThrows(NullPointerException.class, () -> new ProviderTemplateGenerator("x", (GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new ProviderTemplateGenerator("x", (FieldLookup) null));
    }
}
