/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.gofakeit;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.datetime.DateGenerator;
import org.github.krandom.generator.location.CityGenerator;
import org.github.krandom.generator.location.CountryGenerator;
import org.github.krandom.generator.location.PhoneNumberGenerator;
import org.github.krandom.generator.location.PostalCodeGenerator;
import org.github.krandom.generator.location.StateGenerator;
import org.github.krandom.generator.location.StreetAddressGenerator;
import org.github.krandom.generator.network.DomainGenerator;
import org.github.krandom.generator.network.HttpMethodGenerator;
import org.github.krandom.generator.network.HttpStatusCodeGenerator;
import org.github.krandom.generator.network.MacAddressGenerator;
import org.github.krandom.generator.network.PortGenerator;
import org.github.krandom.generator.network.SlugGenerator;
import org.github.krandom.generator.network.URLGenerator;
import org.github.krandom.generator.network.UserAgentGenerator;
import org.github.krandom.generator.text.LoremIpsumGenerator;
import org.github.krandom.generator.text.ParagraphGenerator;
import org.github.krandom.generator.text.SentenceGenerator;
import org.github.krandom.generator.text.TemplateStringGenerator;
import org.github.krandom.generator.text.WordGenerator;
import org.github.krandom.generator.user.AgeGenerator;
import org.github.krandom.generator.user.EmailGenerator;
import org.github.krandom.generator.user.FirstNameGenerator;
import org.github.krandom.generator.user.FullNameGenerator;
import org.github.krandom.generator.user.GenderGenerator;
import org.github.krandom.generator.user.LastNameGenerator;
import org.github.krandom.generator.user.PasswordGenerator;
import org.github.krandom.generator.user.SuffixGenerator;
import org.github.krandom.generator.user.TitleGenerator;
import org.github.krandom.generator.user.UsernameGenerator;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Map;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * GoFakeit-style alias facade over existing Java generators.
 *
 * <p>This class focuses on phase-1 parity APIs (identity/contact/address/network/date/text/template)
 * while delegating all data generation to existing domain generators.
 */
public final class GoFakeitGenerator {

    private static final String LOWER_ALPHA = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER_ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIALS = "!@#$%^&*()-_=+[]{};:,.?";
    private static final char[] ASCII_CHARS = printableAscii();
    private static final Map<String, String[]> STREET_PREFIXES = Map.of(
            "FR", new String[]{"Nord", "Sud", "Est", "Ouest"},
            "DE", new String[]{"Nord", "Sud", "Ost", "West"},
            "ES", new String[]{"Norte", "Sur", "Este", "Oeste"},
            "IT", new String[]{"Nord", "Sud", "Est", "Ovest"}
    );
    private static final String[] DEFAULT_STREET_PREFIXES = {"N", "S", "E", "W"};

    private final GeneratorConfig config;
    private final Random random;

    private final FirstNameGenerator firstNameGenerator;
    private final LastNameGenerator lastNameGenerator;
    private final FullNameGenerator fullNameGenerator;
    private final TitleGenerator titleGenerator;
    private final SuffixGenerator suffixGenerator;
    private final GenderGenerator genderGenerator;
    private final AgeGenerator ageGenerator;
    private final PasswordGenerator passwordGenerator;
    private final UsernameGenerator usernameGenerator;
    private final EmailGenerator emailGenerator;

    private final StreetAddressGenerator streetAddressGenerator;
    private final CityGenerator cityGenerator;
    private final StateGenerator stateGenerator;
    private final PostalCodeGenerator postalCodeGenerator;
    private final CountryGenerator countryGenerator;
    private final PhoneNumberGenerator phoneNumberGenerator;

    private final DomainGenerator domainGenerator;
    private final URLGenerator urlGenerator;
    private final SlugGenerator slugGenerator;
    private final MacAddressGenerator macAddressGenerator;
    private final PortGenerator portGenerator;
    private final HttpMethodGenerator httpMethodGenerator;
    private final HttpStatusCodeGenerator httpStatusCodeGenerator;
    private final UserAgentGenerator userAgentGenerator;

    private final DateGenerator dateGenerator;
    private final WordGenerator wordGenerator;
    private final SentenceGenerator sentenceGenerator;
    private final ParagraphGenerator paragraphGenerator;
    private final LoremIpsumGenerator loremIpsumGenerator;
    private final TemplateStringGenerator templateStringGenerator;

    /** Uses default configuration (Locale.US, unseeded). */
    public GoFakeitGenerator() {
        this(GeneratorConfig.defaults());
    }

    /** Uses locale-specific configuration with default randomness. */
    public GoFakeitGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /** Creates a facade with explicit locale/seed configuration. */
    public GoFakeitGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();

        this.firstNameGenerator = new FirstNameGenerator(config);
        this.lastNameGenerator = new LastNameGenerator(config);
        this.fullNameGenerator = new FullNameGenerator(config);
        this.titleGenerator = new TitleGenerator(config);
        this.suffixGenerator = new SuffixGenerator(config);
        this.genderGenerator = new GenderGenerator(config);
        this.ageGenerator = new AgeGenerator();
        this.passwordGenerator = new PasswordGenerator(config);
        this.usernameGenerator = new UsernameGenerator(config);
        this.emailGenerator = new EmailGenerator(config);

        this.streetAddressGenerator = new StreetAddressGenerator(config);
        this.cityGenerator = new CityGenerator(config);
        this.stateGenerator = new StateGenerator(config);
        this.postalCodeGenerator = new PostalCodeGenerator(config);
        this.countryGenerator = new CountryGenerator(config);
        this.phoneNumberGenerator = new PhoneNumberGenerator(config);

        this.domainGenerator = new DomainGenerator(config);
        this.urlGenerator = new URLGenerator(config);
        this.slugGenerator = new SlugGenerator(config);
        this.macAddressGenerator = new MacAddressGenerator(config);
        this.portGenerator = new PortGenerator();
        this.httpMethodGenerator = new HttpMethodGenerator(config);
        this.httpStatusCodeGenerator = new HttpStatusCodeGenerator(config);
        this.userAgentGenerator = new UserAgentGenerator(config);

        this.dateGenerator = new DateGenerator(config);
        this.wordGenerator = new WordGenerator(config);
        this.sentenceGenerator = new SentenceGenerator(config);
        this.paragraphGenerator = new ParagraphGenerator(config);
        this.loremIpsumGenerator = new LoremIpsumGenerator(config);
        this.templateStringGenerator = new TemplateStringGenerator("", config);
    }

    public String firstName() {
        return firstNameGenerator.generate();
    }

    public String lastName() {
        return lastNameGenerator.generate();
    }

    public String name() {
        return fullNameGenerator.generate();
    }

    public String namePrefix() {
        return titleGenerator.generate();
    }

    public String nameSuffix() {
        return suffixGenerator.generate();
    }

    public String gender() {
        return genderGenerator.generate();
    }

    public int age() {
        return ageGenerator.generate();
    }

    public String password() {
        return passwordGenerator.generate();
    }

    public String password(int minLength, int maxLength) {
        return passwordGenerator.generate(minLength, maxLength);
    }

    /**
     * Generates a password using an explicit character policy.
     */
    public String password(boolean lower, boolean upper, boolean numeric, boolean special,
                           int minLength, int maxLength) {
        if (!(lower || upper || numeric || special)) {
            throw new IllegalArgumentException("at least one character class must be enabled");
        }
        if (minLength <= 0) {
            throw new IllegalArgumentException("minLength must be positive, got: " + minLength);
        }
        if (maxLength < minLength) {
            throw new IllegalArgumentException("maxLength must be >= minLength, got: "
                    + maxLength + " < " + minLength);
        }

        StringBuilder charPool = new StringBuilder();
        if (lower) {
            charPool.append(LOWER_ALPHA);
        }
        if (upper) {
            charPool.append(UPPER_ALPHA);
        }
        if (numeric) {
            charPool.append(DIGITS);
        }
        if (special) {
            charPool.append(SPECIALS);
        }

        int requiredClasses = (lower ? 1 : 0) + (upper ? 1 : 0) + (numeric ? 1 : 0) + (special ? 1 : 0);
        int length = minLength + random.nextInt(maxLength - minLength + 1);
        if (length < requiredClasses) {
            throw new IllegalArgumentException("length range is too small for required character classes");
        }

        char[] out = new char[length];
        int i = 0;
        if (lower) {
            out[i++] = LOWER_ALPHA.charAt(random.nextInt(LOWER_ALPHA.length()));
        }
        if (upper) {
            out[i++] = UPPER_ALPHA.charAt(random.nextInt(UPPER_ALPHA.length()));
        }
        if (numeric) {
            out[i++] = DIGITS.charAt(random.nextInt(DIGITS.length()));
        }
        if (special) {
            out[i++] = SPECIALS.charAt(random.nextInt(SPECIALS.length()));
        }
        while (i < out.length) {
            out[i++] = charPool.charAt(random.nextInt(charPool.length()));
        }

        // Fisher-Yates shuffle so required chars are not always front-loaded.
        for (int j = out.length - 1; j > 0; j--) {
            int k = random.nextInt(j + 1);
            char tmp = out[j];
            out[j] = out[k];
            out[k] = tmp;
        }
        return new String(out);
    }

    public String phone() {
        return phoneNumberGenerator.generate(false);
    }

    public String phoneFormatted() {
        return phoneNumberGenerator.generate(true);
    }

    public String email() {
        return emailGenerator.generate();
    }

    public String street() {
        return streetAddressGenerator.generate();
    }

    public String streetNumber() {
        return streetAddressGenerator.generateStreetAddressNumber();
    }

    public String streetName() {
        return streetAddressGenerator.generateStreetName();
    }

    public String streetSuffix() {
        return streetAddressGenerator.generateStreetSuffix();
    }

    public String streetPrefix() {
        String[] values = STREET_PREFIXES.getOrDefault(config.getLocale().getCountry(), DEFAULT_STREET_PREFIXES);
        return values[random.nextInt(values.length)];
    }

    public String streetUnit() {
        return streetAddressGenerator.generateSecondaryAddress();
    }

    public String city() {
        return cityGenerator.generate();
    }

    public String state() {
        return stateGenerator.generate(false);
    }

    public String stateAbbr() {
        return stateGenerator.generate(true);
    }

    public String zip() {
        return postalCodeGenerator.generate();
    }

    public String country() {
        return countryGenerator.generate();
    }

    public String countryAbbr() {
        return countryGenerator.generateCode();
    }

    public AddressInfo address() {
        return new AddressInfo(
                streetAddressGenerator.generateFullAddress(),
                street(),
                streetNumber(),
                streetName(),
                streetSuffix(),
                streetPrefix(),
                streetUnit(),
                city(),
                state(),
                stateAbbr(),
                zip(),
                country(),
                countryAbbr()
        );
    }

    public ContactInfo contact() {
        return new ContactInfo(
                firstName(),
                lastName(),
                name(),
                gender(),
                age(),
                phone(),
                phoneFormatted(),
                email()
        );
    }

    public PersonInfo person() {
        return new PersonInfo(contact(), address(), usernameGenerator.generate(), password());
    }

    public String domainName() {
        return domainGenerator.generateDomainName();
    }

    public String domainSuffix() {
        return domainGenerator.generateDomainSuffix();
    }

    public String url() {
        return urlGenerator.generateUrl();
    }

    public String urlSlug() {
        return slugGenerator.generate();
    }

    public String macAddress() {
        return macAddressGenerator.generate();
    }

    public int port() {
        return Integer.parseInt(portGenerator.generate());
    }

    public String httpMethod() {
        return httpMethodGenerator.generate();
    }

    public int httpStatusCode() {
        return httpStatusCodeGenerator.generate();
    }

    public String httpStatusSimple() {
        return httpStatusSimple(httpStatusCode());
    }

    public String httpStatusSimple(int statusCode) {
        return switch (statusCode / 100) {
            case 1 -> "informational";
            case 2 -> "success";
            case 3 -> "redirection";
            case 4 -> "client_error";
            default -> "server_error";
        };
    }

    public String userAgent() {
        return userAgentGenerator.generate();
    }

    public LocalDate date() {
        return dateGenerator.generate();
    }

    public LocalDate dateRange(LocalDate fromInclusive, LocalDate toInclusive) {
        return dateGenerator.between(fromInclusive, toInclusive);
    }

    public LocalDate futureDate() {
        return dateGenerator.future();
    }

    public LocalDate pastDate() {
        return dateGenerator.past();
    }

    public String word() {
        return wordGenerator.generate();
    }

    public String sentence() {
        return sentenceGenerator.generate();
    }

    public String paragraph() {
        return paragraphGenerator.generate();
    }

    public String lorem() {
        return loremIpsumGenerator.generate();
    }

    public String numerify(String template) {
        return templateStringGenerator.numerify(template);
    }

    public String lexify(String template) {
        return templateStringGenerator.letterify(template);
    }

    public String bothify(String template) {
        return templateStringGenerator.bothify(template);
    }

    public String asciify(String template) {
        Objects.requireNonNull(template, "template must not be null");
        StringBuilder out = new StringBuilder(template.length());
        for (int i = 0; i < template.length(); i++) {
            char ch = template.charAt(i);
            out.append(ch == '*' ? ASCII_CHARS[random.nextInt(ASCII_CHARS.length)] : ch);
        }
        return out.toString();
    }

    public Locale getLocale() {
        return config.getLocale();
    }

    private static char[] printableAscii() {
        char[] out = new char[94];
        for (int i = 0; i < out.length; i++) {
            out[i] = (char) (33 + i);
        }
        return out;
    }
}
