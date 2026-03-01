/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.database;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates database-like field names and SQL type names.
 */
public final class DatabaseGenerator implements Generator<String> {

    private static final String[] EN_COLUMNS = {
            "id", "created_at", "updated_at", "user_id", "order_id", "status", "name", "description", "amount", "metadata"
    };
    private static final String[] DE_COLUMNS = {
            "id", "erstellt_am", "aktualisiert_am", "benutzer_id", "bestellung_id", "status", "name", "beschreibung", "betrag", "metadaten"
    };
    private static final String[] FR_COLUMNS = {
            "id", "cree_le", "mis_a_jour_le", "utilisateur_id", "commande_id", "statut", "nom", "description", "montant", "metadonnees"
    };
    private static final String[] ES_COLUMNS = {
            "id", "creado_en", "actualizado_en", "usuario_id", "pedido_id", "estado", "nombre", "descripcion", "importe", "metadatos"
    };
    private static final String[] IT_COLUMNS = {
            "id", "creato_il", "aggiornato_il", "utente_id", "ordine_id", "stato", "nome", "descrizione", "importo", "metadati"
    };

    private static final String[] TYPES = {
            "VARCHAR(255)", "TEXT", "INTEGER", "BIGINT", "BOOLEAN", "DATE", "TIMESTAMP", "DECIMAL(10,2)", "JSON", "UUID"
    };

    private final Locale locale;
    private final Random random;

    public DatabaseGenerator() {
        this(GeneratorConfig.defaults());
    }

    public DatabaseGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    public DatabaseGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.locale = config.getLocale();
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
    }

    @Override
    public String generate() {
        return generateColumn();
    }

    public String generateColumn() {
        String[] columns = switch (locale.getLanguage()) {
            case "de" -> DE_COLUMNS;
            case "fr" -> FR_COLUMNS;
            case "es" -> ES_COLUMNS;
            case "it" -> IT_COLUMNS;
            default -> EN_COLUMNS;
        };
        return columns[random.nextInt(columns.length)];
    }

    public String generateType() {
        return TYPES[random.nextInt(TYPES.length)];
    }
}
