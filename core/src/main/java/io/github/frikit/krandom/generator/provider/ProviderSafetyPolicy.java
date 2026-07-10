/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.provider;

import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.Objects;

/**
 * A configuration policy that determines a provider's safety contract.
 */
public enum ProviderSafetyPolicy {

    /** The payment-card policy selected in {@link GeneratorConfig}. */
    PAYMENT_CARD("payment.card-safety-policy"),

    /** The phone-number policy selected in {@link GeneratorConfig}. */
    PHONE_NUMBER("phone-number.safety-policy");

    private final String setting;

    ProviderSafetyPolicy(String setting) {
        this.setting = setting;
    }

    /**
     * Returns the portable-recipe setting name for this policy.
     *
     * @return recipe setting name
     */
    public String getSetting() {
        return setting;
    }

    /**
     * Returns the policy value selected by one generator configuration.
     *
     * @param config configuration that selects the safety policy
     * @return selected policy enum name
     */
    public String selectedValue(GeneratorConfig config) {
        GeneratorConfig selectedConfig = Objects.requireNonNull(config, "config must not be null");
        return switch (this) {
            case PAYMENT_CARD -> selectedConfig.getPaymentCardSafetyPolicy().name();
            case PHONE_NUMBER -> selectedConfig.getPhoneNumberSafetyPolicy().name();
        };
    }
}
