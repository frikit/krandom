/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.location.AddressInfo;
import io.github.frikit.krandom.generator.location.AddressInfoGenerator;
import io.github.frikit.krandom.generator.location.PhoneNumberGenerator;

import java.util.Locale;
import java.util.Objects;

/**
 * Generates structured company payloads composed from business, contact, and address generators.
 */
public final class CompanyInfoGenerator implements Generator<CompanyInfo> {

    private final GeneratorConfig             config;
    private final CompanyNameGenerator        companyNameGenerator;
    private final IndustryGenerator           industryGenerator;
    private final CompanyCatchPhraseGenerator catchPhraseGenerator;
    private final CompanyBuzzwordGenerator    buzzwordGenerator;
    private final CompanyEmailGenerator       emailGenerator;
    private final PhoneNumberGenerator        phoneNumberGenerator;
    private final AddressInfoGenerator        addressInfoGenerator;

    /**
     * Creates a company-info generator using default configuration ({@link Locale#US}).
     */
    public CompanyInfoGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a company-info generator for the specified locale.
     *
     * @param locale locale to use
     */
    public CompanyInfoGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates a company-info generator using explicit configuration.
     *
     * @param config generator configuration
     */
    public CompanyInfoGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.companyNameGenerator = new CompanyNameGenerator(config);
        this.industryGenerator = new IndustryGenerator(config);
        this.catchPhraseGenerator = new CompanyCatchPhraseGenerator(config);
        this.buzzwordGenerator = new CompanyBuzzwordGenerator(config);
        this.emailGenerator = new CompanyEmailGenerator(config);
        this.phoneNumberGenerator = new PhoneNumberGenerator(config);
        this.addressInfoGenerator = new AddressInfoGenerator(config);
    }

    @Override
    public CompanyInfo generate() {
        String name = companyNameGenerator.generate();
        String email = emailGenerator.generate(name);
        String website = "https://www." + email.substring(email.indexOf('@') + 1);
        AddressInfo address = addressInfoGenerator.generate();
        return new CompanyInfo(
            name,
            industryGenerator.generate(),
            catchPhraseGenerator.generate(),
            buzzwordGenerator.generate(),
            email,
            website,
            phoneNumberGenerator.generate(),
            address
        );
    }

    /**
     * Returns the configured locale.
     */
    public Locale getLocale() {
        return config.getLocale();
    }
}
