/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware bank account artifacts (number, name, transaction type).
 *
 * <p>Use an explicit {@link GeneratorConfig} and select
 * {@link BankingSafetyPolicy#REALISTIC_UNCLASSIFIED} only for isolated compatibility fixtures.
 * The default configured policy is {@link BankingSafetyPolicy#DISABLED}.
 *
 * <p>Account names and transaction types are resolved through the configuration's
 * {@code DataRegistryContext}, which defaults to {@link BankAccountDataRegistry}; locales without
 * built-in data fall back to the bundled English vocabulary.
 */
public final class BankAccountGenerator implements Generator<String> {

    private static final BankAccountDataProvider DEFAULT_PROVIDER =
        new BuiltInBankAccountDataProvider(Locale.ROOT, "default");

    private final Locale                locale;
    private final BankAccountDataProvider vocabulary;
    private final Random                random;
    private final BankingSafetyPolicy bankingSafetyPolicy;

    public BankAccountGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.locale = config.getLocale();
        this.random = config.createRandom();
        this.bankingSafetyPolicy = config.getBankingSafetyPolicy();
        BankAccountDataProvider provider = config.getRegistryContext().bankAccountProvider(locale);
        this.vocabulary = provider != null ? provider : DEFAULT_PROVIDER;
    }

    private static int lengthByCountry(String country) {
        return switch (country) {
            case "GB" -> 8;
            case "DE", "US", "ES" -> 10;
            case "FR" -> 11;
            case "IT", "CN" -> 12;
            case "BR" -> 9;
            case "JP" -> 7;
            case "AU" -> 9;
            default -> 10;
        };
    }

    /**
     * Generates a locale-shaped account number.
     */
    @Override
    public String generate() {
        return generateAccountNumber();
    }

    public String generateAccountNumber() {
        bankingSafetyPolicy.requireRealisticOutput();
        return randomDigits(lengthByCountry(locale.getCountry()));
    }

    public String generateAccountName() {
        List<String> values = vocabulary.getAccountNames();
        return values.get(random.nextInt(values.size()));
    }

    public String generateTransactionType() {
        List<String> values = vocabulary.getTransactionTypes();
        return values.get(random.nextInt(values.size()));
    }

    public Locale getLocale() {
        return locale;
    }

    private String randomDigits(int length) {
        StringBuilder out = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            out.append((char) ('0' + random.nextInt(10)));
        }
        return out.toString();
    }
}
