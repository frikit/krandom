/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.EmailDomainPolicy;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.network.DomainGenerator;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates company-style email addresses.
 *
 * <p>The domain label is derived from a company name. Under the default
 * {@link EmailDomainPolicy#TEST_SAFE_RESERVED_DOMAINS} the label uses the reserved {@code .test}
 * top-level domain (RFC 2606 / RFC 6761), for example {@code jsmith@acme.test}, so the address can
 * never reach a real company. {@link EmailDomainPolicy#REALISTIC_UNCLASSIFIED} restores realistic
 * top-level domains for isolated fixtures.
 */
public final class CompanyEmailGenerator implements Generator<String> {

    private static final String RESERVED_TLD = "test";

    private final Random               random;
    private final FirstNameGenerator   firstNameGenerator;
    private final LastNameGenerator    lastNameGenerator;
    private final CompanyNameGenerator companyNameGenerator;
    private final DomainGenerator      domainGenerator;
    private final EmailDomainPolicy    emailDomainPolicy;

    public CompanyEmailGenerator() {
        this(GeneratorConfig.defaults());
    }

    public CompanyEmailGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    public CompanyEmailGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.random = effective.createRandom();
        this.firstNameGenerator = new FirstNameGenerator(effective.forChildStream("firstName"));
        this.lastNameGenerator = new LastNameGenerator(effective.forChildStream("lastName"));
        this.companyNameGenerator = new CompanyNameGenerator(effective.forChildStream("companyName"));
        this.domainGenerator = new DomainGenerator(effective.forChildStream("domain"));
        this.emailDomainPolicy = effective.getEmailDomainPolicy();
    }

    private static String normalize(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                                      .replaceAll("\\p{M}+", "")
                                      .toLowerCase(Locale.ROOT)
                                      .replaceAll("[^a-z0-9]", "");
        return normalized;
    }

    @Override
    public String generate() {
        return generate(companyNameGenerator.generate());
    }

    /**
     * Generates a company email using the provided company name for the domain.
     *
     * @param companyName company name used as domain label
     * @return company email
     */
    public String generate(String companyName) {
        Objects.requireNonNull(companyName, "companyName must not be null");
        String localPart = localPart();
        String domainLabel = normalize(companyName);
        if (domainLabel.isBlank()) {
            domainLabel = domainGenerator.generateName();
        }
        String tld = emailDomainPolicy == EmailDomainPolicy.REALISTIC_UNCLASSIFIED
            ? domainGenerator.getTLD()
            : RESERVED_TLD;
        return localPart + "@" + domainLabel + "." + tld;
    }

    private String localPart() {
        String first = normalize(firstNameGenerator.generate());
        String last = normalize(lastNameGenerator.generate());
        if (first.isBlank()) {
            first = "employee";
        }
        if (last.isBlank()) {
            last = "user";
        }
        return switch (random.nextInt(3)) {
            case 0 -> first + "." + last;
            case 1 -> first.charAt(0) + last;
            default -> first + last;
        };
    }
}
