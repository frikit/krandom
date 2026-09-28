/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.database;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates database-like field names and SQL type names.
 *
 * <p>Column names are resolved through the configuration's {@code DataRegistryContext}, which
 * defaults to {@link DatabaseColumnDataRegistry}; locales without built-in data fall back to the
 * bundled English column names.
 */
public final class DatabaseGenerator implements Generator<String> {

    private static final DatabaseColumnDataProvider DEFAULT_PROVIDER =
        new BuiltInDatabaseColumnDataProvider(Locale.ROOT, "default");

    private static final String[] TYPES = {
        "VARCHAR(255)", "TEXT", "INTEGER", "BIGINT", "BOOLEAN", "DATE", "TIMESTAMP", "DECIMAL(10,2)", "JSON", "UUID"
    };
    private static final String[] TABLES = {
        "users", "orders", "products", "invoices", "payments", "events", "accounts", "sessions"
    };

    private final Random       random;
    private final List<String> columns;

    public DatabaseGenerator() {
        this(GeneratorConfig.defaults());
    }

    public DatabaseGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    public DatabaseGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        DatabaseColumnDataProvider provider = config.getRegistryContext().databaseColumnProvider(config.getLocale());
        this.columns = (provider != null ? provider : DEFAULT_PROVIDER).getColumns();
    }

    @Override
    public String generate() {
        return generateColumn();
    }

    public String generateQueryShape() {
        return switch (random.nextInt(4)) {
            case 0 -> generateSelect();
            case 1 -> generateInsert();
            case 2 -> generateUpdate();
            default -> generateDelete();
        };
    }

    public String generateTable() {
        return TABLES[random.nextInt(TABLES.length)];
    }

    public String generateColumn() {
        return column();
    }

    public String generateType() {
        return TYPES[random.nextInt(TYPES.length)];
    }

    public String generateSelect() {
        String c1 = column();
        String c2 = column();
        String where = column();
        return "SELECT " + c1 + ", " + c2 + " FROM " + generateTable() + " WHERE " + where + " = ?";
    }

    public String generateInsert() {
        String c1 = column();
        String c2 = column();
        return "INSERT INTO " + generateTable() + " (" + c1 + ", " + c2 + ") VALUES (?, ?)";
    }

    public String generateUpdate() {
        String set = column();
        String where = column();
        return "UPDATE " + generateTable() + " SET " + set + " = ? WHERE " + where + " = ?";
    }

    public String generateDelete() {
        String where = column();
        return "DELETE FROM " + generateTable() + " WHERE " + where + " = ?";
    }

    private String column() {
        return columns.get(random.nextInt(columns.size()));
    }
}
