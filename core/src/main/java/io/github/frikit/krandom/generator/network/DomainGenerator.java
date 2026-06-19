/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.network;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates realistic domain names with customizable TLDs (Top-Level Domains).
 *
 * <p>This generator creates domain names using random words combined with popular
 * or custom TLDs. Supports both generic and country-code TLDs.
 *
 * <p><strong>Popular TLDs:</strong>
 * .com, .net, .org, .io, .co, .dev, .app, .tech, .online, .site
 *
 * <p><strong>Country-Code TLDs (by locale):</strong>
 * <ul>
 *   <li>en_US → .us</li>
 *   <li>en_GB → .uk</li>
 *   <li>en_AU → .au</li>
 *   <li>de_DE → .de</li>
 *   <li>fr_FR → .fr</li>
 *   <li>es_ES → .es</li>
 *   <li>it_IT → .it</li>
 *   <li>pt_BR → .br</li>
 *   <li>ja_JP → .jp</li>
 *   <li>zh_CN → .cn</li>
 * </ul>
 *
 * <p><strong>Basic Usage:</strong>
 * <pre>{@code
 * // Random domain with popular TLD
 * DomainGenerator gen = new DomainGenerator();
 * String domain = gen.generate();              // "techcloud.com"
 *
 * // Domain with specific TLD
 * String customDomain = gen.generate("io");    // "datahub.io"
 *
 * // Get just the TLD
 * String tld = gen.getTLD();                   // "net"
 * }</pre>
 *
 * <p><strong>Locale-Aware Generation:</strong>
 * <pre>{@code
 * // US domains
 * DomainGenerator usGen = new DomainGenerator(Locale.US);
 * String usDomain = usGen.generate();  // Might include ".us" TLD
 *
 * // German domains
 * DomainGenerator deGen = new DomainGenerator(Locale.GERMANY);
 * String deDomain = deGen.generate();  // Might include ".de" TLD
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong>
 * This generator holds a single mutable PRNG. Concurrent calls are memory-safe but interleave the
 * random sequence (destroying reproducibility) and contend on the PRNG, so an instance is <em>not</em>
 * safe to share across threads. Confine one instance per thread, or wrap construction with
 * {@code Generators.threadLocal(...)} for deterministic concurrent use.
 */
public final class DomainGenerator implements Generator<String> {

    private static final String[] POPULAR_TLDS = {
        "com", "net", "org", "io", "co", "dev", "app",
        "tech", "online", "site", "xyz", "pro"
    };

    private static final String[] DOMAIN_WORDS = {
        "tech", "data", "cloud", "digital", "web", "net", "info", "media",
        "global", "world", "smart", "quick", "easy", "fast", "modern",
        "secure", "prime", "apex", "vertex", "nexus", "core", "central",
        "hub", "link", "connect", "sync", "flow", "stream", "wave",
        "code", "dev", "build", "maker", "labs", "works", "solutions"
    };

    private final GeneratorConfig config;
    private final Random          random;
    private final String          localeTLD;

    /**
     * Creates a domain generator with default configuration.
     */
    public DomainGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a domain generator for the specified locale.
     *
     * @param locale the locale for country-code TLD preference
     */
    public DomainGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /**
     * Creates a domain generator with the specified configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public DomainGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.localeTLD = getLocaleTLD(config.getLocale());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates a domain name with a random TLD (could be popular or locale-specific).
     *
     * @return a domain name; never {@code null}
     */
    @Override
    public String generate() {
        String name = generateDomainLabel();
        String tld = random.nextBoolean() && localeTLD != null ? localeTLD : getRandomPopularTLD();
        return name + "." + tld;
    }

    /**
     * Generates a domain name with the specified TLD.
     *
     * @param tld the top-level domain (without dot, e.g., "com", "org"); must not be {@code null}
     * @return a domain name with the specified TLD; never {@code null}
     * @throws NullPointerException if {@code tld} is {@code null}
     */
    public String generate(String tld) {
        Objects.requireNonNull(tld, "tld must not be null");
        String name = generateDomainLabel();
        return name + "." + tld;
    }

    /**
     * Generates a full domain name.
     * Bogus-style alias: {@code domainName()}.
     *
     * @return full domain name with suffix
     */
    public String generateDomainName() {
        return generate();
    }

    /**
     * Generates a domain name label without TLD.
     *
     * @return a domain label such as {@code "techcloud"} or {@code "data"}
     */
    public String generateName() {
        return generateDomainLabel();
    }

    /**
     * Generates a single domain word/label.
     * Bogus-style alias: {@code domainWord()}.
     *
     * @return domain label without suffix
     */
    public String generateDomainWord() {
        return generateName();
    }

    /**
     * Generates just a TLD (top-level domain).
     *
     * <p>Returns either a popular TLD or a locale-specific TLD based on configuration.
     *
     * @return a TLD without dot (e.g., "com", "org", "de"); never {@code null}
     */
    public String getTLD() {
        return random.nextBoolean() && localeTLD != null ? localeTLD : getRandomPopularTLD();
    }

    /**
     * Generates a domain suffix (top-level domain).
     * Bogus-style alias: {@code domainSuffix()}.
     *
     * @return suffix without dot
     */
    public String generateDomainSuffix() {
        return getTLD();
    }

    /**
     * Generates a top-level domain token.
     * Mimesis-style alias.
     *
     * @return suffix without dot
     */
    public String generateTld() {
        return getTLD();
    }

    /**
     * Generates just a TLD from popular options.
     *
     * @return a popular TLD without dot (e.g., "com", "net", "org"); never {@code null}
     */
    public String getPopularTLD() {
        return getRandomPopularTLD();
    }

    /**
     * Gets the locale-specific TLD for this generator.
     *
     * @return the locale TLD, or null if no specific TLD for the locale
     */
    public String getLocaleTLD() {
        return localeTLD;
    }

    /**
     * Generates a domain name (without TLD).
     *
     * @return a domain name
     */
    private String generateDomainLabel() {
        // Simple approach: 1-2 words
        if (random.nextBoolean()) {
            // Single word
            return getRandomWord();
        } else {
            // Two words
            return getRandomWord() + getRandomWord();
        }
    }

    /**
     * Returns a random domain word.
     *
     * @return a random word
     */
    private String getRandomWord() {
        return DOMAIN_WORDS[random.nextInt(DOMAIN_WORDS.length)];
    }

    /**
     * Returns a random popular TLD.
     *
     * @return a popular TLD
     */
    private String getRandomPopularTLD() {
        return POPULAR_TLDS[random.nextInt(POPULAR_TLDS.length)];
    }

    /**
     * Gets the country-code TLD for the given locale.
     *
     * @param locale the locale
     * @return the country-code TLD, or null if not mapped
     */
    private String getLocaleTLD(Locale locale) {
        if (locale == null) {
            return null;
        }

        String country = locale.getCountry();

        return switch (country) {
            case "US" -> "us";
            case "GB" -> "uk";
            case "AU" -> "au";
            case "DE" -> "de";
            case "FR" -> "fr";
            case "ES" -> "es";
            case "IT" -> "it";
            case "BR" -> "br";
            case "JP" -> "jp";
            case "CN" -> "cn";
            default -> null;
        };
    }
}
