/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates realistic email addresses with locale-aware names.
 *
 * <p>This generator creates email addresses using locale-appropriate first and last names
 * combined with popular domain names. Supports multiple email formats and custom domains.
 *
 * <p><strong>Supported Formats:</strong>
 * <ul>
 *   <li><strong>FIRSTNAME_DOT_LASTNAME</strong>: john.smith@example.com</li>
 *   <li><strong>FIRSTNAME_LASTNAME</strong>: johnsmith@example.com</li>
 *   <li><strong>FIRSTINITIAL_LASTNAME</strong>: jsmith@example.com</li>
 *   <li><strong>FIRSTNAME_UNDERSCORE_LASTNAME</strong>: john_smith@example.com</li>
 *   <li><strong>LASTNAME_DOT_FIRSTNAME</strong>: smith.john@example.com</li>
 * </ul>
 *
 * <p><strong>Popular Domains:</strong>
 * gmail.com, yahoo.com, outlook.com, hotmail.com, icloud.com, protonmail.com,
 * mail.com, aol.com, zoho.com, gmx.com, yandex.com, qq.com
 *
 * <p><strong>Basic Usage:</strong>
 * <pre>{@code
 * // Random email with popular domain
 * EmailGenerator gen = new EmailGenerator();
 * String email = gen.generate();               // "john.smith@gmail.com"
 * 
 * // Email with custom domain
 * String corpEmail = gen.generate("example.com");  // "john.smith@example.com"
 * 
 * // Email with specific format
 * String formatEmail = gen.generate(EmailFormat.FIRSTINITIAL_LASTNAME);
 * // "jsmith@yahoo.com"
 * }</pre>
 *
 * <p><strong>Locale-Aware Generation:</strong>
 * <pre>{@code
 * // US names
 * EmailGenerator usGen = new EmailGenerator(Locale.US);
 * String usEmail = usGen.generate();  // "james.wilson@gmail.com"
 * 
 * // German names
 * EmailGenerator deGen = new EmailGenerator(Locale.GERMANY);
 * String deEmail = deGen.generate();  // "hans.mueller@gmail.com"
 * 
 * // Japanese names
 * EmailGenerator jpGen = new EmailGenerator(Locale.JAPAN);
 * String jpEmail = jpGen.generate();  // "yuki.tanaka@gmail.com"
 * }</pre>
 *
 * <p><strong>Seeded Generation:</strong>
 * <pre>{@code
 * EmailGenerator gen1 = new EmailGenerator(
 *     GeneratorConfig.builder().seed(12345L).build());
 * EmailGenerator gen2 = new EmailGenerator(
 *     GeneratorConfig.builder().seed(12345L).build());
 * gen1.generate().equals(gen2.generate());  // true (same sequence)
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong>
 * This generator is thread-safe and can be shared across threads.
 *
 * @see FirstNameGenerator
 * @see LastNameGenerator
 * @see EmailFormat
 */
public final class EmailGenerator implements Generator<String> {
    
    private static final String[] POPULAR_DOMAINS = {
        "gmail.com",
        "yahoo.com",
        "outlook.com",
        "hotmail.com",
        "icloud.com",
        "protonmail.com",
        "mail.com",
        "aol.com",
        "zoho.com",
        "gmx.com",
        "yandex.com",
        "qq.com"
    };
    
    private final GeneratorConfig config;
    private final Random random;
    private final FirstNameGenerator firstNameGenerator;
    private final LastNameGenerator lastNameGenerator;
    
    /**
     * Creates an email generator with default configuration (US locale).
     */
    public EmailGenerator() {
        this(GeneratorConfig.defaults());
    }
    
    /**
     * Creates an email generator for the specified locale.
     *
     * @param locale the locale for name generation; must not be {@code null}
     * @throws NullPointerException if {@code locale} is {@code null}
     */
    public EmailGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }
    
    /**
     * Creates an email generator with the specified configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public EmailGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
        this.firstNameGenerator = new FirstNameGenerator(config);
        this.lastNameGenerator = new LastNameGenerator(config);
    }
    
    /**
     * {@inheritDoc}
     *
     * <p>Generates an email address with a random format and popular domain.
     *
     * @return an email address; never {@code null}
     */
    @Override
    public String generate() {
        EmailFormat format = getRandomFormat();
        String domain = getRandomDomain();
        return generate(format, domain);
    }
    
    /**
     * Generates an email address with the specified domain.
     *
     * @param domain the domain to use (e.g., "example.com"); must not be {@code null}
     * @return an email address with the specified domain; never {@code null}
     * @throws NullPointerException if {@code domain} is {@code null}
     */
    public String generate(String domain) {
        Objects.requireNonNull(domain, "domain must not be null");
        EmailFormat format = getRandomFormat();
        return generate(format, domain);
    }
    
    /**
     * Generates an email address with the specified format.
     *
     * @param format the email format to use; must not be {@code null}
     * @return an email address with the specified format; never {@code null}
     * @throws NullPointerException if {@code format} is {@code null}
     */
    public String generate(EmailFormat format) {
        Objects.requireNonNull(format, "format must not be null");
        String domain = getRandomDomain();
        return generate(format, domain);
    }
    
    /**
     * Generates an email address with the specified format and domain.
     *
     * @param format the email format to use; must not be {@code null}
     * @param domain the domain to use (e.g., "example.com"); must not be {@code null}
     * @return an email address; never {@code null}
     * @throws NullPointerException if {@code format} or {@code domain} is {@code null}
     */
    public String generate(EmailFormat format, String domain) {
        Objects.requireNonNull(format, "format must not be null");
        Objects.requireNonNull(domain, "domain must not be null");
        
        String firstName = firstNameGenerator.generate();
        String lastName = lastNameGenerator.generate();
        
        String localPart = formatLocalPart(firstName, lastName, format);
        return localPart + "@" + domain;
    }
    
    /**
     * Formats the local part of the email address based on the specified format.
     *
     * @param firstName the first name
     * @param lastName the last name
     * @param format the email format
     * @return the formatted local part
     */
    private String formatLocalPart(String firstName, String lastName, EmailFormat format) {
        String first = firstName.toLowerCase().replace(" ", "");
        String last = lastName.toLowerCase().replace(" ", "");
        
        return switch (format) {
            case FIRSTNAME_DOT_LASTNAME -> first + "." + last;
            case FIRSTNAME_LASTNAME -> first + last;
            case FIRSTINITIAL_LASTNAME -> first.charAt(0) + last;
            case FIRSTNAME_UNDERSCORE_LASTNAME -> first + "_" + last;
            case LASTNAME_DOT_FIRSTNAME -> last + "." + first;
        };
    }
    
    /**
     * Returns a random email format.
     *
     * @return a random EmailFormat
     */
    private EmailFormat getRandomFormat() {
        EmailFormat[] formats = EmailFormat.values();
        return formats[random.nextInt(formats.length)];
    }
    
    /**
     * Returns a random popular domain.
     *
     * @return a random domain name
     */
    private String getRandomDomain() {
        return POPULAR_DOMAINS[random.nextInt(POPULAR_DOMAINS.length)];
    }
}
