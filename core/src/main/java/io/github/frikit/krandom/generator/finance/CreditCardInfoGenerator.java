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
 * Generates structured credit-card payloads.
 *
 * <p>The contained card number follows the configured {@link PaymentCardSafetyPolicy}. The
 * default deliberately fails Luhn; checksum-valid output is an explicit validator-fixture opt-in
 * and is never a real or processor-sandbox credential. {@link PaymentCardSafetyPolicy#STRIPE_SANDBOX}
 * selects fixed Stripe sandbox values and requires Stripe sandbox/test API keys.
 */
public final class CreditCardInfoGenerator implements Generator<CreditCardInfo> {

    private final GeneratorConfig     config;
    private final CreditCardGenerator creditCardGenerator;

    /**
     * Creates a credit-card-info generator using default configuration ({@link Locale#US}).
     */
    public CreditCardInfoGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a credit-card-info generator for the specified locale.
     *
     * @param locale locale to use
     */
    public CreditCardInfoGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates a credit-card-info generator using explicit configuration.
     *
     * @param config generator configuration
     */
    public CreditCardInfoGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.creditCardGenerator = new CreditCardGenerator(config);
    }

    @Override
    public CreditCardInfo generate() {
        return creditCardGenerator.generateCreditCardInfo();
    }

    /**
     * Returns the configured locale.
     */
    public Locale getLocale() {
        return config.getLocale();
    }
}
