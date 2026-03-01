/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.network;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates hostnames such as {@code api.example.com}.
 */
public final class HostnameGenerator implements Generator<String> {

    private static final String[] SUBDOMAINS = {
            "www", "mail", "api", "app", "cdn", "m", "img", "files"
    };

    private final Random random;
    private final DomainGenerator domainGenerator;

    public HostnameGenerator() {
        this(GeneratorConfig.defaults());
    }

    public HostnameGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    public HostnameGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.random = effective.getSeed().isPresent()
                ? new Random(effective.getSeed().getAsLong())
                : new SecureRandom();
        this.domainGenerator = new DomainGenerator(effective);
    }

    @Override
    public String generate() {
        String subdomain = SUBDOMAINS[random.nextInt(SUBDOMAINS.length)];
        return subdomain + "." + domainGenerator.generate();
    }

    /**
     * Generates a hostname using a fixed subdomain.
     *
     * @param subdomain fixed subdomain, such as {@code api}
     * @return generated hostname
     */
    public String generate(String subdomain) {
        Objects.requireNonNull(subdomain, "subdomain must not be null");
        if (subdomain.isBlank()) {
            throw new IllegalArgumentException("subdomain must not be blank");
        }
        return subdomain + "." + domainGenerator.generate();
    }
}
