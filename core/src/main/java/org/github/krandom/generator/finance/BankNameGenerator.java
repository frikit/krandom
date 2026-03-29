/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.finance;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware bank names.
 */
public final class BankNameGenerator implements Generator<String> {

    private static final String[]              DEFAULT_BANKS     = {
        "First National Bank", "Global Trust Bank", "Pioneer Credit Union", "Summit Financial", "Civic Savings"
    };
    private static final Map<String, String[]> BANKS_BY_LANGUAGE = Map.of(
        "de", new String[] { "Erste Nationalbank", "Global Trust Bank", "Buerger Sparkasse", "Summit Finanz", "Pionier Kreditbank" },
        "fr", new String[] { "Banque Nationale Premiere", "Banque Confiance Globale", "Epargne Civique", "Finance Sommet", "Credit Pionnier" },
        "es", new String[] { "Banco Nacional Primero", "Banco Confianza Global", "Ahorro Civico", "Finanzas Cumbre", "Credito Pionero" },
        "it", new String[] { "Banca Nazionale Prima", "Banca Fiducia Globale", "Risparmio Civico", "Finanza Vertice", "Credito Pioniere" },
        "pt", new String[] { "Banco Nacional Primeiro", "Banco Confianca Global", "Poupanca Civica", "Financas Pico", "Credito Pioneiro" }
    );

    private final Locale locale;
    private final Random random;

    public BankNameGenerator() {
        this(GeneratorConfig.defaults());
    }

    public BankNameGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    public BankNameGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.locale = config.getLocale();
        this.random = config.createRandom();
    }

    @Override
    public String generate() {
        String[] values = BANKS_BY_LANGUAGE.getOrDefault(locale.getLanguage(), DEFAULT_BANKS);
        return values[random.nextInt(values.length)];
    }
}
