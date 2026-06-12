/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.provider;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DataFakerExpressionGenerator")
class DataFakerExpressionGeneratorTest {

    @Test
    @DisplayName("resolves tokens embedded in literal text")
    void resolvesTokensWithLiterals() {
        String value = new DataFakerExpressionGenerator("Hi #{Name.firstName}!").generate();

        assertTrue(value.startsWith("Hi "), "literal prefix should be preserved: " + value);
        assertTrue(value.endsWith("!"), "literal suffix should be preserved: " + value);
        assertFalse(value.contains("#{"), "token should be expanded: " + value);
        assertTrue(value.length() > "Hi !".length());
    }

    @Test
    @DisplayName("resolves adjacent tokens at expression start and end")
    void resolvesTokenOnlyExpressions() {
        String value = new DataFakerExpressionGenerator("#{Name.firstName} #{Name.lastName}").generate();

        assertFalse(value.contains("#{"));
        assertTrue(value.contains(" "));
    }

    @Test
    @DisplayName("camelCase and snake_case forms of the same token are equivalent")
    void camelAndSnakeCaseEquivalent() {
        GeneratorConfig seeded = GeneratorConfig.builder().seed(42L).build();

        String camel = new DataFakerExpressionGenerator("#{Name.firstName}", seeded).generate();
        String snake = new DataFakerExpressionGenerator("#{Name.first_name}", seeded).generate();

        assertEquals(camel, snake);
    }

    @Test
    @DisplayName("same seed produces the same output")
    void deterministicWithSeed() {
        GeneratorConfig seeded = GeneratorConfig.builder().seed(7L).build();

        String first = new DataFakerExpressionGenerator("#{Name.fullName} <#{Internet.emailAddress}>", seeded)
            .generate();
        String second = new DataFakerExpressionGenerator("#{Name.fullName} <#{Internet.emailAddress}>", seeded)
            .generate();

        assertEquals(first, second);
    }

    @Test
    @DisplayName("literal-only expressions pass through unchanged")
    void literalOnlyPassThrough() {
        assertEquals("no tokens here", new DataFakerExpressionGenerator("no tokens here").generate());
    }

    @Test
    @DisplayName("unknown tokens fail fast with the supported-expression list")
    void unknownTokenThrows() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                                                      () -> new DataFakerExpressionGenerator("#{Beer.name}"));

        assertTrue(error.getMessage().contains("Unsupported DataFaker expression"));
        assertTrue(error.getMessage().contains("name.firstname"));
    }

    @Test
    @DisplayName("every supported expression resolves against the provider hub")
    void allSupportedExpressionsResolve() {
        for (String key : DataFakerExpressionGenerator.supportedExpressions()) {
            String value = new DataFakerExpressionGenerator("#{" + key + "}").generate();
            assertNotNull(value, key);
            assertFalse(value.contains("#{"), key);
        }
    }

    @Test
    @DisplayName("null arguments are rejected")
    void nullArgumentsThrow() {
        assertThrows(NullPointerException.class, () -> new DataFakerExpressionGenerator(null));
        assertThrows(NullPointerException.class,
                     () -> new DataFakerExpressionGenerator("#{Name.firstName}", null));
    }

    @Test
    @DisplayName("Generators facade exposes both overloads")
    void facadeOverloads() {
        assertNotNull(Generators.ofDataFakerExpression("#{Address.city}").generate());
        assertNotNull(Generators.ofDataFakerExpression("#{Address.city}",
                                                       GeneratorConfig.builder().seed(3L).build()).generate());
    }
}
