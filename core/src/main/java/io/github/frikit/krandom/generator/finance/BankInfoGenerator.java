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
 *
 * <p>Use an explicit {@link GeneratorConfig} and select
 * {@link BankingSafetyPolicy#REALISTIC_UNCLASSIFIED} only for isolated compatibility fixtures.
 * The default configured policy is {@link BankingSafetyPolicy#DISABLED}.
 */
public final class BankInfoGenerator implements Generator<BankInfo> {

    private final GeneratorConfig      config;
    private final BankAccountGenerator bankAccountGenerator;
    private final AbaRoutingGenerator  abaRoutingGenerator;
    private final BankNameGenerator    bankNameGenerator;
    private final BankTypeGenerator    bankTypeGenerator;

    /**
     * @deprecated Use {@link #BankInfoGenerator(GeneratorConfig)}. This 1.6 bridge retains
     *             realistic but unclassified output; v2 configuration fails closed by default.
     */
    @Deprecated(since = "1.6", forRemoval = true)
    public BankInfoGenerator() {
        this(GeneratorConfig.builder().bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED).build());
    }

    /**
     * @deprecated Use {@link #BankInfoGenerator(GeneratorConfig)}. This 1.6 bridge retains
     *             realistic but unclassified output; v2 configuration fails closed by default.
     */
    @Deprecated(since = "1.6", forRemoval = true)
    public BankInfoGenerator(Locale locale) {
        this(GeneratorConfig.builder()
                            .locale(Objects.requireNonNull(locale, "locale must not be null"))
                            .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED)
                            .build());
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
        config.getBankingSafetyPolicy().requireRealisticOutput();
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
