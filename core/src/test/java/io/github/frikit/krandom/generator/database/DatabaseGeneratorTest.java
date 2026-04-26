/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.database;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    @DisplayName("query helpers generate SQL query shapes")
    void queryHelpers() {
        DatabaseGenerator generator = new DatabaseGenerator(GeneratorConfig.builder().seed(7L).locale(Locale.US).build());

        String table = generator.generateTable();
        assertFalse(table.isBlank());

        String select = generator.generateSelect();
        assertTrue(select.startsWith("SELECT "));
        assertTrue(select.contains(" FROM "));
        assertTrue(select.contains(" WHERE "));
        assertTrue(select.endsWith(" = ?"));

        String insert = generator.generateInsert();
        assertTrue(insert.startsWith("INSERT INTO "));
        assertTrue(insert.contains(" VALUES (?, ?)"));

        String update = generator.generateUpdate();
        assertTrue(update.startsWith("UPDATE "));
        assertTrue(update.contains(" SET "));
        assertTrue(update.contains(" WHERE "));

        String delete = generator.generateDelete();
        assertTrue(delete.startsWith("DELETE FROM "));
        assertTrue(delete.contains(" WHERE "));

        String shape = generator.generateQueryShape();
        assertTrue(shape.startsWith("SELECT ")
                   || shape.startsWith("INSERT INTO ")
                   || shape.startsWith("UPDATE ")
                   || shape.startsWith("DELETE FROM "));
    }

    @Test
    @DisplayName("seeded query generation is reproducible")
    void queryDeterminism() {
        GeneratorConfig config = GeneratorConfig.builder().seed(101L).locale(Locale.US).build();
        DatabaseGenerator a = new DatabaseGenerator(config);
        DatabaseGenerator b = new DatabaseGenerator(config);

        assertEquals(a.generateTable(), b.generateTable());
        assertEquals(a.generateSelect(), b.generateSelect());
        assertEquals(a.generateInsert(), b.generateInsert());
        assertEquals(a.generateUpdate(), b.generateUpdate());
        assertEquals(a.generateDelete(), b.generateDelete());
    }

    @Test
    @DisplayName("query shapes cover all top-level SQL operations")
    void queryShapeCoverage() {
        DatabaseGenerator generator = new DatabaseGenerator(GeneratorConfig.builder().seed(33L).locale(Locale.US).build());
        Set<String> ops = Set.of("SELECT", "INSERT", "UPDATE", "DELETE");
        for (int i = 0; i < 20; i++) {
            String query = generator.generateQueryShape();
            String op = query.substring(0, query.indexOf(' '));
            assertTrue(ops.contains(op));
        }
    }
}
