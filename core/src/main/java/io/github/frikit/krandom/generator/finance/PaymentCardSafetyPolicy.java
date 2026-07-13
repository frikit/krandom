/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

/**
 * Safety contract for generated payment-card numbers.
 *
 * <p>A checksum-valid card number is not a sandbox credential and must never be sent to payment,
 * account-creation, identity, or production systems.
 */
public enum PaymentCardSafetyPolicy {

    /**
     * Produces issuer-shaped, format-valid numbers whose final digit deliberately fails Luhn.
     *
     * <p>This is the default. It provides fixtures that exercise formatting and validation-rejection
     * paths without creating a checksum-valid payment-card number.
     */
    TEST_SAFE_NON_ROUTABLE,

    /**
     * Produces fixed card numbers published by Stripe for its sandbox.
     *
     * <p>Use this only with Stripe sandbox/test API keys. Stripe recommends its named
     * {@code PaymentMethod} values instead of raw numbers for server-side test code. This policy
     * does not make the values portable to another processor or safe for live payment APIs.
     */
    STRIPE_SANDBOX,

    /**
     * Produces issuer-shaped numbers that pass Luhn validation.
     *
     * <p>This opt-in supports validator fixtures only. It does not make a number a processor
     * sandbox value, a usable card, or safe to submit to any external system.
     */
    CHECKSUM_VALID
}
