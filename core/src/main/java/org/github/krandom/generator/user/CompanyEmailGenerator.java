/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.network.DomainGenerator;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates company-style email addresses.
 */
public final class CompanyEmailGenerator implements Generator<String> {

    private final Random               random;
    private final FirstNameGenerator   firstNameGenerator;
    private final LastNameGenerator    lastNameGenerator;
    private final CompanyNameGenerator companyNameGenerator;
    private final DomainGenerator      domainGenerator;

    public CompanyEmailGenerator() {
        this(GeneratorConfig.defaults());
    }

    public CompanyEmailGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    public CompanyEmailGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.random = effective.getSeed().isPresent()
                      ? new Random(effective.getSeed().getAsLong())
                      : new SecureRandom();
        this.firstNameGenerator = new FirstNameGenerator(effective);
        this.lastNameGenerator = new LastNameGenerator(effective);
        this.companyNameGenerator = new CompanyNameGenerator(effective);
        this.domainGenerator = new DomainGenerator(effective);
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
        String tld = domainGenerator.getTLD();
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
