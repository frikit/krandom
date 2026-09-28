/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates ISO 13616 basic bank account numbers (BBANs) that follow the IBAN-registry structure of
 * one country.
 *
 * <p><strong>Country:</strong> the BBAN country is resolved exactly as for {@link IbanGenerator}:
 * the configured locale's country when it has an IBAN format built in (AT, BE, BG, BR, CH, CZ, DE,
 * DK, ES, FI, FR, GB, GR, HR, HU, IE, IL, IT, NL, NO, PL, PT, RO, RU, SA, SE, SK, TR, UA), otherwise
 * Germany ({@code DE}). Locales of countries without IBANs, such as {@code en_US}, {@code ja_JP},
 * or {@code zh_CN}, locales without a country, and the default {@code en_US} configuration
 * therefore produce German 18-digit BBANs rather than a domestic account-number format.
 *
 * <p>Each BBAN has the registry length and character classes of the resolved country (for example
 * {@code 8!n10!n} for DE and {@code 4!a6!n8!n} for GB); for BE, CZ, ES, FI, FR, HR, HU, IT, NO,
 * PL, PT, and SK its national check digits are valid as well. For the same configuration it equals
 * the BBAN part of the corresponding {@link IbanGenerator} output.
 *
 * <p>Use an explicit {@link GeneratorConfig} and select
 * {@link BankingSafetyPolicy#REALISTIC_UNCLASSIFIED} only for isolated compatibility fixtures.
 * The default configured policy is {@link BankingSafetyPolicy#DISABLED}.
 */
public final class BbanGenerator implements Generator<String> {

    private final Locale              locale;
    private final Random              random;
    private final BankingSafetyPolicy bankingSafetyPolicy;

    /**
     * Creates a BBAN generator for the country resolved from the configured locale.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public BbanGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.locale = effective.getLocale();
        this.random = effective.createRandom();
        this.bankingSafetyPolicy = effective.getBankingSafetyPolicy();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if the configured banking safety policy is
     *                               {@link BankingSafetyPolicy#DISABLED}
     */
    @Override
    public String generate() {
        bankingSafetyPolicy.requireRealisticOutput();
        return IbanCountryFormat.forLocale(locale).randomBban(random);
    }
}
