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
 * Generates locale-aware bank type labels.
 */
public final class BankTypeGenerator implements Generator<String> {

    private static final String[] DEFAULT_TYPES = {
            "Retail Bank", "Commercial Bank", "Investment Bank", "Credit Union", "Online Bank"
    };
    private static final Map<String, String[]> TYPES_BY_LANGUAGE = Map.of(
            "de", new String[]{"Privatkundenbank", "Geschaeftsbank", "Investmentbank", "Kreditgenossenschaft", "Online-Bank"},
            "fr", new String[]{"Banque de detail", "Banque commerciale", "Banque d'investissement", "Cooperative de credit", "Banque en ligne"},
            "es", new String[]{"Banco minorista", "Banco comercial", "Banco de inversion", "Cooperativa de credito", "Banco en linea"},
            "it", new String[]{"Banca al dettaglio", "Banca commerciale", "Banca d'investimento", "Cooperativa di credito", "Banca online"},
            "pt", new String[]{"Banco de varejo", "Banco comercial", "Banco de investimento", "Cooperativa de credito", "Banco online"}
    );

    private final Locale locale;
    private final Random random;

    public BankTypeGenerator() {
        this(GeneratorConfig.defaults());
    }

    public BankTypeGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    public BankTypeGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.locale = config.getLocale();
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
    }

    @Override
    public String generate() {
        String[] values = TYPES_BY_LANGUAGE.getOrDefault(locale.getLanguage(), DEFAULT_TYPES);
        return values[random.nextInt(values.length)];
    }
}
