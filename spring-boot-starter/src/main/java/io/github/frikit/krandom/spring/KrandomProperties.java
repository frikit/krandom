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

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Integer getObjectMaxDepth() {
        return objectMaxDepth;
    }

    public void setObjectMaxDepth(Integer objectMaxDepth) {
        this.objectMaxDepth = objectMaxDepth;
    }

    public Double getObjectNullProbability() {
        return objectNullProbability;
    }

    public void setObjectNullProbability(Double objectNullProbability) {
        this.objectNullProbability = objectNullProbability;
    }

    public Integer getMinStringLength() {
        return minStringLength;
    }

    public void setMinStringLength(Integer minStringLength) {
        this.minStringLength = minStringLength;
    }

    public Integer getMaxStringLength() {
        return maxStringLength;
    }

    public void setMaxStringLength(Integer maxStringLength) {
        this.maxStringLength = maxStringLength;
    }

    public Integer getMinCollectionSize() {
        return minCollectionSize;
    }

    public void setMinCollectionSize(Integer minCollectionSize) {
        this.minCollectionSize = minCollectionSize;
    }

    public Integer getMaxCollectionSize() {
        return maxCollectionSize;
    }

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

    public String getRecipe() {
        return recipe;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    public String getClock() {
        return clock;
    }

    public void setClock(String clock) {
        this.clock = clock;
    }

    public String getClockZone() {
        return clockZone;
    }

    public void setClockZone(String clockZone) {
        this.clockZone = clockZone;
    }

    public io.github.frikit.krandom.generator.finance.PaymentCardSafetyPolicy getPaymentCardSafetyPolicy() {
        return paymentCardSafetyPolicy;
    }

    public void setPaymentCardSafetyPolicy(
            io.github.frikit.krandom.generator.finance.PaymentCardSafetyPolicy paymentCardSafetyPolicy) {
        this.paymentCardSafetyPolicy = paymentCardSafetyPolicy;
    }

    public io.github.frikit.krandom.generator.location.PhoneNumberSafetyPolicy getPhoneNumberSafetyPolicy() {
        return phoneNumberSafetyPolicy;
    }

    public void setPhoneNumberSafetyPolicy(
            io.github.frikit.krandom.generator.location.PhoneNumberSafetyPolicy phoneNumberSafetyPolicy) {
        this.phoneNumberSafetyPolicy = phoneNumberSafetyPolicy;
    }

    public io.github.frikit.krandom.generator.user.nationalid.NationalIdSafetyPolicy getNationalIdSafetyPolicy() {
        return nationalIdSafetyPolicy;
    }

    public void setNationalIdSafetyPolicy(
            io.github.frikit.krandom.generator.user.nationalid.NationalIdSafetyPolicy nationalIdSafetyPolicy) {
        this.nationalIdSafetyPolicy = nationalIdSafetyPolicy;
    }

    public io.github.frikit.krandom.generator.finance.BankingSafetyPolicy getBankingSafetyPolicy() {
        return bankingSafetyPolicy;
    }

    public void setBankingSafetyPolicy(
            io.github.frikit.krandom.generator.finance.BankingSafetyPolicy bankingSafetyPolicy) {
        this.bankingSafetyPolicy = bankingSafetyPolicy;
    }

    public io.github.frikit.krandom.generator.finance.SecuritiesIdentifierSafetyPolicy getSecuritiesIdentifierSafetyPolicy() {
        return securitiesIdentifierSafetyPolicy;
    }

    public void setSecuritiesIdentifierSafetyPolicy(
            io.github.frikit.krandom.generator.finance.SecuritiesIdentifierSafetyPolicy securitiesIdentifierSafetyPolicy) {
        this.securitiesIdentifierSafetyPolicy = securitiesIdentifierSafetyPolicy;
    }

    public io.github.frikit.krandom.generator.finance.CryptoAddressSafetyPolicy getCryptoAddressSafetyPolicy() {
        return cryptoAddressSafetyPolicy;
    }

    public void setCryptoAddressSafetyPolicy(
            io.github.frikit.krandom.generator.finance.CryptoAddressSafetyPolicy cryptoAddressSafetyPolicy) {
        this.cryptoAddressSafetyPolicy = cryptoAddressSafetyPolicy;
    }

    public io.github.frikit.krandom.generator.BusinessTaxIdentifierSafetyPolicy getBusinessTaxIdentifierSafetyPolicy() {
        return businessTaxIdentifierSafetyPolicy;
    }

    public void setBusinessTaxIdentifierSafetyPolicy(
            io.github.frikit.krandom.generator.BusinessTaxIdentifierSafetyPolicy businessTaxIdentifierSafetyPolicy) {
        this.businessTaxIdentifierSafetyPolicy = businessTaxIdentifierSafetyPolicy;
    }

    public io.github.frikit.krandom.generator.user.IdentityDocumentSafetyPolicy getIdentityDocumentSafetyPolicy() {
        return identityDocumentSafetyPolicy;
    }

    public void setIdentityDocumentSafetyPolicy(
            io.github.frikit.krandom.generator.user.IdentityDocumentSafetyPolicy identityDocumentSafetyPolicy) {
        this.identityDocumentSafetyPolicy = identityDocumentSafetyPolicy;
    }

    public io.github.frikit.krandom.generator.object.ObjectConstructionPolicy getObjectConstructionPolicy() {
        return objectConstructionPolicy;
    }

    public void setObjectConstructionPolicy(
            io.github.frikit.krandom.generator.object.ObjectConstructionPolicy objectConstructionPolicy) {
        this.objectConstructionPolicy = objectConstructionPolicy;
    }
}
