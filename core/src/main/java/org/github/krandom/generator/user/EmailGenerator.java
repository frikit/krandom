/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

    private static final String[] SAFE_DOMAINS = {
        "example.com",
        "example.org",
        "example.net"
    };

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

    private static final String[] POPULAR_DOMAINS_DE = { "gmx.de", "web.de", "gmail.com", "outlook.com" };
    private static final String[] POPULAR_DOMAINS_FR = { "orange.fr", "laposte.net", "gmail.com", "outlook.com" };
    private static final String[] POPULAR_DOMAINS_ES = { "hotmail.es", "gmail.com", "outlook.com", "yahoo.com" };
    private static final String[] POPULAR_DOMAINS_IT = { "libero.it", "gmail.com", "outlook.com", "yahoo.com" };
    private static final String[] POPULAR_DOMAINS_PT = { "uol.com.br", "bol.com.br", "gmail.com", "outlook.com" };
    private static final String[] POPULAR_DOMAINS_JA = { "yahoo.co.jp", "gmail.com", "outlook.com" };
    private static final String[] POPULAR_DOMAINS_ZH = { "qq.com", "163.com", "126.com", "gmail.com" };

    private final GeneratorConfig    config;
    private final Random             random;
    private final FirstNameGenerator firstNameGenerator;
    private final LastNameGenerator  lastNameGenerator;
    private final Set<String>        issuedEmails;

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
        this.issuedEmails = ConcurrentHashMap.newKeySet();
    }

    private static String[] requireDomains(String... domains) {
        Objects.requireNonNull(domains, "domains must not be null");
        if (domains.length == 0) {
            throw new IllegalArgumentException("domains must not be empty");
        }
        String[] normalized = Arrays.copyOf(domains, domains.length);
        for (int i = 0; i < normalized.length; i++) {
            String domain = normalized[i];
            if (domain == null || domain.isBlank()) {
                throw new IllegalArgumentException("domains must not contain null/blank values");
            }
            normalized[i] = domain;
        }
        return normalized;
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
     * Generates an email address using a commonly used free-email provider domain.
     *
     * @return an email with a free provider domain
     */
    public String generateFreeEmail() {
        return generate(getFreeEmailProvider());
    }

    /**
     * Generates an email address using a safe/reserved test domain.
     *
     * @return an email with {@code example.com}, {@code example.org}, or {@code example.net}
     */
    public String generateSafeEmail() {
        return generate(SAFE_DOMAINS[random.nextInt(SAFE_DOMAINS.length)]);
    }

    /**
     * Returns a free-email provider domain.
     *
     * @return provider domain (for example, {@code gmail.com})
     */
    public String getFreeEmailProvider() {
        return getRandomFreeDomain();
    }

    /**
     * Returns a free-email provider domain (Faker-style {@code free_email_domain()} equivalent).
     *
     * @return provider domain (for example, {@code gmail.com})
     */
    public String generateFreeEmailDomain() {
        return getFreeEmailProvider();
    }

    /**
     * Generates a company email address.
     *
     * @return a company-style email
     */
    public String generateCompanyEmail() {
        return new CompanyEmailGenerator(config).generate();
    }

    /**
     * Generates an email using one of the provided domains.
     *
     * @param domains non-null, non-empty domain candidates
     * @return generated email
     */
    public String generateFromDomains(String... domains) {
        String[] candidates = requireDomains(domains);
        return generate(candidates[random.nextInt(candidates.length)]);
    }

    /**
     * Generates a unique email for this generator instance.
     *
     * @return unique email value
     */
    public String generateUnique() {
        return generateUniqueInternal(() -> generate());
    }

    /**
     * Generates a unique email at the provided domain for this generator instance.
     *
     * @param domain fixed domain
     * @return unique email value
     */
    public String generateUnique(String domain) {
        Objects.requireNonNull(domain, "domain must not be null");
        return generateUniqueInternal(() -> generate(domain));
    }

    /**
     * Generates a unique email using one of the provided domains.
     *
     * @param domains non-null, non-empty domain candidates
     * @return unique email value
     */
    public String generateUniqueFromDomains(String... domains) {
        String[] candidates = requireDomains(domains);
        return generateUniqueInternal(() -> generate(candidates[random.nextInt(candidates.length)]));
    }

    /**
     * Formats the local part of the email address based on the specified format.
     *
     * @param firstName the first name
     * @param lastName  the last name
     * @param format    the email format
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

    private String getRandomFreeDomain() {
        String language = config.getLocale().getLanguage();
        String[] providers = switch (language) {
            case "de" -> POPULAR_DOMAINS_DE;
            case "fr" -> POPULAR_DOMAINS_FR;
            case "es" -> POPULAR_DOMAINS_ES;
            case "it" -> POPULAR_DOMAINS_IT;
            case "pt" -> POPULAR_DOMAINS_PT;
            case "ja" -> POPULAR_DOMAINS_JA;
            case "zh" -> POPULAR_DOMAINS_ZH;
            default -> POPULAR_DOMAINS;
        };
        return providers[random.nextInt(providers.length)];
    }

    private String generateUniqueInternal(java.util.function.Supplier<String> supplier) {
        final int maxAttempts = 1_000;
        for (int i = 0; i < maxAttempts; i++) {
            String email = supplier.get();
            if (issuedEmails.add(email)) {
                return email;
            }
        }
        throw new IllegalStateException("Unable to generate a unique email after " + maxAttempts + " attempts");
    }
}
