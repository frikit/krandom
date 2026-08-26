/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalized configuration for the krandom auto-configuration.
 *
 * <p>All properties are optional. When omitted, krandom uses its built-in defaults
 * (an unseeded {@link java.util.Random}, US locale, depth 5, etc.).
 *
 * <pre>{@code
 *   krandom.seed=42
 *   krandom.locale=de-DE
 *   krandom.object-max-depth=3
 * }</pre>
 */
@ConfigurationProperties(prefix = "krandom")
public class KrandomProperties {

    /**
     * Numeric PRNG seed for reproducible output. When unset, the core default
     * {@link java.util.Random} source is used.
     */
    private Long seed;

    /**
     * Locale tag (e.g. {@code en-US}, {@code en_US}, {@code de-DE}). Defaults to {@code en_US}.
     */
    private String locale;

    /**
     * Maximum nesting depth for reflection-based object generation. Defaults to 5.
     */
    private Integer objectMaxDepth;

    /**
     * Probability (0.0 – 1.0) that a nullable reference field is set to null
     * during object generation. Defaults to 0.0.
     */
    private Double objectNullProbability;

    /**
     * Minimum length of generated strings. Defaults to the krandom built-in (5).
     */
    private Integer minStringLength;

    /**
     * Maximum length of generated strings. Defaults to the krandom built-in (20).
     */
    private Integer maxStringLength;

    /**
     * Minimum size of generated collections and arrays. Defaults to the krandom built-in (1).
     */
    private Integer minCollectionSize;

    /**
     * Maximum size of generated collections and arrays. Defaults to the krandom built-in (10).
     */
    private Integer maxCollectionSize;

    /** Creates an empty property holder for Spring Boot binding. */
    public KrandomProperties() {
    }

    /**
     * Returns the configured seed.
     *
     * @return the seed, or {@code null} to use the core default
     */
    public Long getSeed() {
        return seed;
    }

    /**
     * Sets the seed.
     *
     * @param seed seed to use, or {@code null} for the core default
     */
    public void setSeed(Long seed) {
        this.seed = seed;
    }

    /**
     * Returns the configured locale tag.
     *
     * @return the locale tag, or {@code null} to use the core default
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Sets the locale tag.
     *
     * @param locale locale tag to use, or {@code null} for the core default
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * Returns the maximum generated object depth.
     *
     * @return the maximum depth, or {@code null} to use the core default
     */
    public Integer getObjectMaxDepth() {
        return objectMaxDepth;
    }

    /**
     * Sets the maximum generated object depth.
     *
     * @param objectMaxDepth maximum depth, or {@code null} for the core default
     */
    public void setObjectMaxDepth(Integer objectMaxDepth) {
        this.objectMaxDepth = objectMaxDepth;
    }

    /**
     * Returns the probability of assigning {@code null} to eligible object fields.
     *
     * @return the null probability, or {@code null} to use the core default
     */
    public Double getObjectNullProbability() {
        return objectNullProbability;
    }

    /**
     * Sets the probability of assigning {@code null} to eligible object fields.
     *
     * @param objectNullProbability probability from 0.0 to 1.0, or {@code null} for the default
     */
    public void setObjectNullProbability(Double objectNullProbability) {
        this.objectNullProbability = objectNullProbability;
    }

    /**
     * Returns the minimum generated string length.
     *
     * @return the minimum length, or {@code null} to use the core default
     */
    public Integer getMinStringLength() {
        return minStringLength;
    }

    /**
     * Sets the minimum generated string length.
     *
     * @param minStringLength minimum length, or {@code null} for the core default
     */
    public void setMinStringLength(Integer minStringLength) {
        this.minStringLength = minStringLength;
    }

    /**
     * Returns the maximum generated string length.
     *
     * @return the maximum length, or {@code null} to use the core default
     */
    public Integer getMaxStringLength() {
        return maxStringLength;
    }

    /**
     * Sets the maximum generated string length.
     *
     * @param maxStringLength maximum length, or {@code null} for the core default
     */
    public void setMaxStringLength(Integer maxStringLength) {
        this.maxStringLength = maxStringLength;
    }

    /**
     * Returns the minimum generated collection size.
     *
     * @return the minimum size, or {@code null} to use the core default
     */
    public Integer getMinCollectionSize() {
        return minCollectionSize;
    }

    /**
     * Sets the minimum generated collection size.
     *
     * @param minCollectionSize minimum size, or {@code null} for the core default
     */
    public void setMinCollectionSize(Integer minCollectionSize) {
        this.minCollectionSize = minCollectionSize;
    }

    /**
     * Returns the maximum generated collection size.
     *
     * @return the maximum size, or {@code null} to use the core default
     */
    public Integer getMaxCollectionSize() {
        return maxCollectionSize;
    }

    /**
     * Sets the maximum generated collection size.
     *
     * @param maxCollectionSize maximum size, or {@code null} for the core default
     */
    public void setMaxCollectionSize(Integer maxCollectionSize) {
        this.maxCollectionSize = maxCollectionSize;
    }

    /**
     * Serialized replay recipe ({@code base64:<url-safe base64>} or the literal serialized form
     * with {@code \n} escapes). Mutually exclusive with {@code krandom.seed} and
     * {@code krandom.locale}.
     */
    private String recipe;

    /** ISO-8601 instant for a fixed clock, e.g. {@code 2026-01-01T00:00:00Z}. */
    private String clock;

    /** Zone ID for the fixed clock; defaults to UTC when only {@code krandom.clock} is set. */
    private String clockZone;

    private io.github.frikit.krandom.generator.finance.PaymentCardSafetyPolicy paymentCardSafetyPolicy;
    private io.github.frikit.krandom.generator.location.PhoneNumberSafetyPolicy phoneNumberSafetyPolicy;
    private io.github.frikit.krandom.generator.user.nationalid.NationalIdSafetyPolicy nationalIdSafetyPolicy;
    private io.github.frikit.krandom.generator.finance.BankingSafetyPolicy bankingSafetyPolicy;
    private io.github.frikit.krandom.generator.finance.SecuritiesIdentifierSafetyPolicy securitiesIdentifierSafetyPolicy;
    private io.github.frikit.krandom.generator.finance.CryptoAddressSafetyPolicy cryptoAddressSafetyPolicy;
    private io.github.frikit.krandom.generator.BusinessTaxIdentifierSafetyPolicy businessTaxIdentifierSafetyPolicy;
    private io.github.frikit.krandom.generator.user.IdentityDocumentSafetyPolicy identityDocumentSafetyPolicy;
    private io.github.frikit.krandom.generator.object.ObjectConstructionPolicy objectConstructionPolicy;

    /**
     * Returns the serialized replay recipe.
     *
     * @return the recipe, or {@code null} when not configured
     */
    public String getRecipe() {
        return recipe;
    }

    /**
     * Sets the serialized replay recipe.
     *
     * @param recipe recipe to use, or {@code null} when not configured
     */
    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    /**
     * Returns the fixed-clock instant.
     *
     * @return the ISO-8601 instant, or {@code null} when not configured
     */
    public String getClock() {
        return clock;
    }

    /**
     * Sets the fixed-clock instant.
     *
     * @param clock ISO-8601 instant, or {@code null} when not configured
     */
    public void setClock(String clock) {
        this.clock = clock;
    }

    /**
     * Returns the fixed-clock zone.
     *
     * @return the zone ID, or {@code null} to use UTC
     */
    public String getClockZone() {
        return clockZone;
    }

    /**
     * Sets the fixed-clock zone.
     *
     * @param clockZone zone ID, or {@code null} to use UTC
     */
    public void setClockZone(String clockZone) {
        this.clockZone = clockZone;
    }

    /**
     * Returns the payment-card safety policy.
     *
     * @return the policy, or {@code null} to use the core default
     */
    public io.github.frikit.krandom.generator.finance.PaymentCardSafetyPolicy getPaymentCardSafetyPolicy() {
        return paymentCardSafetyPolicy;
    }

    /**
     * Sets the payment-card safety policy.
     *
     * @param paymentCardSafetyPolicy policy to use, or {@code null} for the core default
     */
    public void setPaymentCardSafetyPolicy(
            io.github.frikit.krandom.generator.finance.PaymentCardSafetyPolicy paymentCardSafetyPolicy) {
        this.paymentCardSafetyPolicy = paymentCardSafetyPolicy;
    }

    /**
     * Returns the phone-number safety policy.
     *
     * @return the policy, or {@code null} to use the core default
     */
    public io.github.frikit.krandom.generator.location.PhoneNumberSafetyPolicy getPhoneNumberSafetyPolicy() {
        return phoneNumberSafetyPolicy;
    }

    /**
     * Sets the phone-number safety policy.
     *
     * @param phoneNumberSafetyPolicy policy to use, or {@code null} for the core default
     */
    public void setPhoneNumberSafetyPolicy(
            io.github.frikit.krandom.generator.location.PhoneNumberSafetyPolicy phoneNumberSafetyPolicy) {
        this.phoneNumberSafetyPolicy = phoneNumberSafetyPolicy;
    }

    /**
     * Returns the national-ID safety policy.
     *
     * @return the policy, or {@code null} to use the core default
     */
    public io.github.frikit.krandom.generator.user.nationalid.NationalIdSafetyPolicy getNationalIdSafetyPolicy() {
        return nationalIdSafetyPolicy;
    }

    /**
     * Sets the national-ID safety policy.
     *
     * @param nationalIdSafetyPolicy policy to use, or {@code null} for the core default
     */
    public void setNationalIdSafetyPolicy(
            io.github.frikit.krandom.generator.user.nationalid.NationalIdSafetyPolicy nationalIdSafetyPolicy) {
        this.nationalIdSafetyPolicy = nationalIdSafetyPolicy;
    }

    /**
     * Returns the banking safety policy.
     *
     * @return the policy, or {@code null} to use the core default
     */
    public io.github.frikit.krandom.generator.finance.BankingSafetyPolicy getBankingSafetyPolicy() {
        return bankingSafetyPolicy;
    }

    /**
     * Sets the banking safety policy.
     *
     * @param bankingSafetyPolicy policy to use, or {@code null} for the core default
     */
    public void setBankingSafetyPolicy(
            io.github.frikit.krandom.generator.finance.BankingSafetyPolicy bankingSafetyPolicy) {
        this.bankingSafetyPolicy = bankingSafetyPolicy;
    }

    /**
     * Returns the securities-identifier safety policy.
     *
     * @return the policy, or {@code null} to use the core default
     */
    public io.github.frikit.krandom.generator.finance.SecuritiesIdentifierSafetyPolicy getSecuritiesIdentifierSafetyPolicy() {
        return securitiesIdentifierSafetyPolicy;
    }

    /**
     * Sets the securities-identifier safety policy.
     *
     * @param securitiesIdentifierSafetyPolicy policy to use, or {@code null} for the core default
     */
    public void setSecuritiesIdentifierSafetyPolicy(
            io.github.frikit.krandom.generator.finance.SecuritiesIdentifierSafetyPolicy securitiesIdentifierSafetyPolicy) {
        this.securitiesIdentifierSafetyPolicy = securitiesIdentifierSafetyPolicy;
    }

    /**
     * Returns the crypto-address safety policy.
     *
     * @return the policy, or {@code null} to use the core default
     */
    public io.github.frikit.krandom.generator.finance.CryptoAddressSafetyPolicy getCryptoAddressSafetyPolicy() {
        return cryptoAddressSafetyPolicy;
    }

    /**
     * Sets the crypto-address safety policy.
     *
     * @param cryptoAddressSafetyPolicy policy to use, or {@code null} for the core default
     */
    public void setCryptoAddressSafetyPolicy(
            io.github.frikit.krandom.generator.finance.CryptoAddressSafetyPolicy cryptoAddressSafetyPolicy) {
        this.cryptoAddressSafetyPolicy = cryptoAddressSafetyPolicy;
    }

    /**
     * Returns the business-tax-identifier safety policy.
     *
     * @return the policy, or {@code null} to use the core default
     */
    public io.github.frikit.krandom.generator.BusinessTaxIdentifierSafetyPolicy getBusinessTaxIdentifierSafetyPolicy() {
        return businessTaxIdentifierSafetyPolicy;
    }

    /**
     * Sets the business-tax-identifier safety policy.
     *
     * @param businessTaxIdentifierSafetyPolicy policy to use, or {@code null} for the core default
     */
    public void setBusinessTaxIdentifierSafetyPolicy(
            io.github.frikit.krandom.generator.BusinessTaxIdentifierSafetyPolicy businessTaxIdentifierSafetyPolicy) {
        this.businessTaxIdentifierSafetyPolicy = businessTaxIdentifierSafetyPolicy;
    }

    /**
     * Returns the identity-document safety policy.
     *
     * @return the policy, or {@code null} to use the core default
     */
    public io.github.frikit.krandom.generator.user.IdentityDocumentSafetyPolicy getIdentityDocumentSafetyPolicy() {
        return identityDocumentSafetyPolicy;
    }

    /**
     * Sets the identity-document safety policy.
     *
     * @param identityDocumentSafetyPolicy policy to use, or {@code null} for the core default
     */
    public void setIdentityDocumentSafetyPolicy(
            io.github.frikit.krandom.generator.user.IdentityDocumentSafetyPolicy identityDocumentSafetyPolicy) {
        this.identityDocumentSafetyPolicy = identityDocumentSafetyPolicy;
    }

    /**
     * Returns the object-construction policy.
     *
     * @return the policy, or {@code null} to use the core default
     */
    public io.github.frikit.krandom.generator.object.ObjectConstructionPolicy getObjectConstructionPolicy() {
        return objectConstructionPolicy;
    }

    /**
     * Sets the object-construction policy.
     *
     * @param objectConstructionPolicy policy to use, or {@code null} for the core default
     */
    public void setObjectConstructionPolicy(
            io.github.frikit.krandom.generator.object.ObjectConstructionPolicy objectConstructionPolicy) {
        this.objectConstructionPolicy = objectConstructionPolicy;
    }
}
