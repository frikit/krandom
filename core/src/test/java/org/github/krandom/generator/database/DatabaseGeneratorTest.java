/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.database;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DatabaseGenerator")
class DatabaseGeneratorTest {

    @Test
    @DisplayName("generates column and SQL type")
    void basics() {
        DatabaseGenerator generator = new DatabaseGenerator(Locale.US);
        assertFalse(generator.generate().isBlank());
        assertFalse(generator.generateColumn().isBlank());
        assertTrue(generator.generateType().matches(
                "VARCHAR\\(255\\)|TEXT|INTEGER|BIGINT|BOOLEAN|DATE|TIMESTAMP|DECIMAL\\(10,2\\)|JSON|UUID"));
    }

    @Test
    @DisplayName("locale branch changes column vocabulary")
    void localeSupport() {
        DatabaseGenerator en = new DatabaseGenerator(GeneratorConfig.builder().seed(8L).locale(Locale.US).build());
        DatabaseGenerator fr = new DatabaseGenerator(GeneratorConfig.builder().seed(8L).locale(Locale.FRANCE).build());
        assertNotEquals(en.generateColumn(), fr.generateColumn());
    }

    @Test
    @DisplayName("covers all locale branches for columns")
    void localeBranchCoverage() {
        Locale[] locales = {
                Locale.US,
                Locale.GERMANY,
                Locale.FRANCE,
                Locale.of("es", "ES"),
                Locale.ITALY
        };
        for (Locale locale : locales) {
            DatabaseGenerator generator = new DatabaseGenerator(
                    GeneratorConfig.builder().seed(2L).locale(locale).build()
            );
            assertFalse(generator.generateColumn().isBlank());
            assertFalse(generator.generateType().isBlank());
        }
    }

    @Test
    @DisplayName("validates constructor arguments and factory wiring")
    void constructorAndFactory() {
        assertThrows(NullPointerException.class, () -> new DatabaseGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new DatabaseGenerator((GeneratorConfig) null));
        assertNotNull(Generators.ofDatabase().generateColumn());
    }
}
