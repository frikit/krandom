/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.network.DomainGenerator;
import io.github.frikit.krandom.generator.network.NetworkSafetyPolicy;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates company website URLs.
 *
 * <p>Under the default {@link NetworkSafetyPolicy#TEST_SAFE_RESERVED} the host is under the reserved {@code .test}
 * top-level domain, also when a company name is supplied.
 *
 * <p>A URL from a company name takes its host label from the name's ASCII letters and digits, at most the 63
 * characters that a domain label may hold, so that {@link java.net.IDN#toASCII(String)} accepts the host.
 */
public final class CompanyUrlGenerator implements Generator<String> {

    private static final String[] PROTOCOLS = {"https", "http"};
    private static final String RESERVED_TLD = "test";

    /** The most characters a domain label may hold (RFC 1035). */
    private static final int MAX_LABEL_LENGTH = 63;

    private final DomainGenerator domainGenerator;
    private final CompanyNameGenerator companyNameGenerator;
    private final Random random;
    private final NetworkSafetyPolicy networkSafetyPolicy;

    /**
     * Creates a company URL generator with default configuration.
     */
    public CompanyUrlGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a company URL generator for a locale.
     */
    public CompanyUrlGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /**
     * Creates a company URL generator with the specified configuration.
     */
    public CompanyUrlGenerator(GeneratorConfig config) {
        GeneratorConfig safe = Objects.requireNonNull(config, "config must not be null");
        this.domainGenerator = new DomainGenerator(safe);
        this.companyNameGenerator = new CompanyNameGenerator(safe);
        this.random = safe.createRandom();
        this.networkSafetyPolicy = safe.getNetworkSafetyPolicy();
    }

    @Override
    public String generate() {
        String protocol = PROTOCOLS[random.nextInt(PROTOCOLS.length)];
        return protocol + "://www." + domainGenerator.generate();
    }

    /**
     * Generates a company URL from a generated company name and locale TLD.
     */
    public String generateFromCompanyName() {
        return generateFromCompanyName(companyNameGenerator.generate(false));
    }

    /**
     * Generates a company URL from the provided company name and locale TLD.
     *
     * @param companyName company name used as host label, of which the first 63 letters and digits are kept
     * @return company URL
     */
    public String generateFromCompanyName(String companyName) {
        Objects.requireNonNull(companyName, "companyName must not be null");
        String company = companyName.toLowerCase(Locale.ROOT);
        String slug = company.replaceAll("[^a-z0-9]+", "");
        if (slug.isBlank()) {
            slug = "company";
        } else if (slug.length() > MAX_LABEL_LENGTH) {
            slug = slug.substring(0, MAX_LABEL_LENGTH);
        }
        String tld = networkSafetyPolicy == NetworkSafetyPolicy.REALISTIC_UNCLASSIFIED
                ? domainGenerator.getTLD()
                : RESERVED_TLD;
        return "https://www." + slug + "." + tld;
    }
}
