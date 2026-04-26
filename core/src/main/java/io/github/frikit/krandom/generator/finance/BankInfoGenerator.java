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

/**
 * Generates structured bank and ACH-style payloads.
 */
public final class BankInfoGenerator implements Generator<BankInfo> {

    private final GeneratorConfig      config;
    private final BankAccountGenerator bankAccountGenerator;
    private final AbaRoutingGenerator  abaRoutingGenerator;
    private final BankNameGenerator    bankNameGenerator;
    private final BankTypeGenerator    bankTypeGenerator;

    /**
     * Creates a bank-info generator using default configuration ({@link Locale#US}).
     */
    public BankInfoGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a bank-info generator for the specified locale.
     *
     * @param locale locale to use
     */
    public BankInfoGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates a bank-info generator using explicit configuration.
     *
     * @param config generator configuration
     */
    public BankInfoGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.bankAccountGenerator = new BankAccountGenerator(config);
        this.abaRoutingGenerator = new AbaRoutingGenerator(config);
        this.bankNameGenerator = new BankNameGenerator(config);
        this.bankTypeGenerator = new BankTypeGenerator(config);
    }

    @Override
    public BankInfo generate() {
        return new BankInfo(
            bankAccountGenerator.generateAccountNumber(),
            abaRoutingGenerator.generate(),
            bankNameGenerator.generate(),
            bankTypeGenerator.generate(),
            bankAccountGenerator.generateAccountName(),
            bankAccountGenerator.generateTransactionType()
        );
    }

    /**
     * Returns the configured locale.
     */
    public Locale getLocale() {
        return config.getLocale();
    }
}
