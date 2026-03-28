/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.Set;

/**
 * Generates locale-aware full names by combining a {@link FirstNameGenerator} and a
 * {@link LastNameGenerator}.
 *
 * <p>Built-in support covers the same 10 locales as the underlying name generators
 * (US, UK, AU, FR, DE, JA, ES, IT, PT, ZH).
 *
 * <pre>{@code
 * FullNameGenerator gen = new FullNameGenerator();
 * String name = gen.generate();               // "James Smith"
 * String male = gen.generate(Gender.MALE);    // "John Doe"
 *
 * FullNameGenerator de = new FullNameGenerator(Locale.GERMANY);
 * String deName = de.generate(Gender.FEMALE); // "Marie Müller"
 * }</pre>
 */
public final class FullNameGenerator implements Generator<String> {

    private static final Map<String, Locale> NATIONALITY_TO_LOCALE      = nationalityToLocaleMap();
    private static final Set<String>         SUPPORTED_NATIONALITY_KEYS = NATIONALITY_TO_LOCALE.keySet();

    private final GeneratorConfig             config;
    private final FirstNameGenerator          firstNameGenerator;
    private final LastNameGenerator           lastNameGenerator;
    private final Map<Locale, NameGenerators> generatorsByLocale;

    /**
     * Uses {@link GeneratorConfig#defaults()} — locale defaults to {@link Locale#US}.
     */
    public FullNameGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Constructs a generator for the given locale.
     */
    public FullNameGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /**
     * Full constructor using a {@link GeneratorConfig} (locale + optional seed).
     */
    public FullNameGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.firstNameGenerator = new FirstNameGenerator(config);
        this.lastNameGenerator = new LastNameGenerator(config);
        this.generatorsByLocale = new HashMap<>();
        this.generatorsByLocale.put(config.getLocale(),
                                    new NameGenerators(config.getLocale(), firstNameGenerator, lastNameGenerator));
    }

    private static Locale resolveNationalityLocale(String nationality) {
        String key = normalizeNationality(nationality);
        Locale locale = NATIONALITY_TO_LOCALE.get(key);
        if (locale == null) {
            throw new UnsupportedOperationException(
                "Nationality '" + nationality + "' is not supported. Supported: " + SUPPORTED_NATIONALITY_KEYS);
        }
        return locale;
    }

    private static String normalizeNationality(String nationality) {
        Objects.requireNonNull(nationality, "nationality must not be null");
        String normalized = nationality.trim().toLowerCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("nationality must not be blank");
        }
        return normalized;
    }

    private static Map<String, Locale> nationalityToLocaleMap() {
        Map<String, Locale> map = new HashMap<>();
        map.put("en", Locale.US);
        map.put("us", Locale.US);
        map.put("en_us", Locale.US);

        map.put("gb", Locale.UK);
        map.put("uk", Locale.UK);
        map.put("en_gb", Locale.UK);

        map.put("au", Locale.of("en", "AU"));
        map.put("en_au", Locale.of("en", "AU"));

        map.put("de", Locale.GERMANY);
        map.put("de_de", Locale.GERMANY);

        map.put("fr", Locale.FRANCE);
        map.put("fr_fr", Locale.FRANCE);

        map.put("es", Locale.of("es", "ES"));
        map.put("es_es", Locale.of("es", "ES"));

        map.put("it", Locale.ITALY);
        map.put("it_it", Locale.ITALY);

        map.put("pt", Locale.of("pt", "BR"));
        map.put("br", Locale.of("pt", "BR"));
        map.put("pt_br", Locale.of("pt", "BR"));

        map.put("ja", Locale.JAPAN);
        map.put("jp", Locale.JAPAN);
        map.put("ja_jp", Locale.JAPAN);

        map.put("zh", Locale.CHINA);
        map.put("cn", Locale.CHINA);
        map.put("zh_cn", Locale.CHINA);
        return Map.copyOf(map);
    }

    /**
     * Generates a random full name (first + last) with a randomly chosen gender.
     *
     * @return a full name string; never {@code null}
     */
    @Override
    public String generate() {
        return firstNameGenerator.generate() + " " + lastNameGenerator.generate();
    }

    /**
     * Generates a full name for the specified gender.
     *
     * @param gender {@link Gender#MALE} or {@link Gender#FEMALE}; must not be {@code null}
     * @return a full name string; never {@code null}
     * @throws NullPointerException if {@code gender} is {@code null}
     */
    public String generate(Gender gender) {
        Objects.requireNonNull(gender, "gender must not be null");
        return firstNameGenerator.generate(gender) + " " + lastNameGenerator.generate();
    }

    /**
     * Generates a full name using female first-name data.
     *
     * @return female full name
     */
    public String generateFemale() {
        return generate(Gender.FEMALE);
    }

    /**
     * Generates a full name using male first-name data.
     *
     * @return male full name
     */
    public String generateMale() {
        return generate(Gender.MALE);
    }

    /**
     * Generates a full name that includes a middle name.
     *
     * <p>For locales where middle names are not part of the naming model (for example
     * {@code es_ES}, {@code ja_JP}, {@code zh_CN}), this method throws
     * {@link UnsupportedOperationException}.
     *
     * @return a three-part full name string; never {@code null}
     * @throws UnsupportedOperationException if middle names are not supported for this locale
     */
    public String generateWithMiddleName() {
        MiddleNameGenerator middleNameGenerator = new MiddleNameGenerator(config);
        return firstNameGenerator.generate()
               + " " + middleNameGenerator.generate()
               + " " + lastNameGenerator.generate();
    }

    /**
     * Generates a full name that includes a middle name for the specified gender.
     *
     * <p>For locales where middle names are not part of the naming model (for example
     * {@code es_ES}, {@code ja_JP}, {@code zh_CN}), this method throws
     * {@link UnsupportedOperationException}.
     *
     * @param gender {@link Gender#MALE} or {@link Gender#FEMALE}; must not be {@code null}
     * @return a three-part full name string; never {@code null}
     * @throws NullPointerException          if {@code gender} is {@code null}
     * @throws UnsupportedOperationException if middle names are not supported for this locale
     */
    public String generateWithMiddleName(Gender gender) {
        Objects.requireNonNull(gender, "gender must not be null");
        MiddleNameGenerator middleNameGenerator = new MiddleNameGenerator(config);
        return firstNameGenerator.generate(gender)
               + " " + middleNameGenerator.generate(gender)
               + " " + lastNameGenerator.generate();
    }

    /**
     * Generates a full name with a middle initial (for example, {@code "John P. Smith"}).
     *
     * <p>For locales where middle names are not part of the naming model (for example
     * {@code es_ES}, {@code ja_JP}, {@code zh_CN}), this method throws
     * {@link UnsupportedOperationException}.
     *
     * @return a three-part full name with middle initial; never {@code null}
     * @throws UnsupportedOperationException if middle names are not supported for this locale
     */
    public String generateWithMiddleInitial() {
        return firstNameGenerator.generate()
               + " " + new MiddleNameGenerator(config).generateInitial()
               + " " + lastNameGenerator.generate();
    }

    /**
     * Generates a full name with a middle initial for the specified gender.
     *
     * @param gender {@link Gender#MALE} or {@link Gender#FEMALE}; must not be {@code null}
     * @return a three-part full name with middle initial; never {@code null}
     * @throws NullPointerException          if {@code gender} is {@code null}
     * @throws UnsupportedOperationException if middle names are not supported for this locale
     */
    public String generateWithMiddleInitial(Gender gender) {
        Objects.requireNonNull(gender, "gender must not be null");
        return firstNameGenerator.generate(gender)
               + " " + new MiddleNameGenerator(config).generateInitial(gender)
               + " " + lastNameGenerator.generate();
    }

    /**
     * Generates a full name with Chance-style option combinations.
     *
     * <p>Supported options:
     * <ul>
     *   <li>{@code middle} — include full middle name</li>
     *   <li>{@code middleInitial} — include middle initial (takes precedence over {@code middle})</li>
     *   <li>{@code prefix} — prepend title/honorific</li>
     *   <li>{@code suffix} — append suffix</li>
     *   <li>{@code reverse} — emit name in reversed order (last name first)</li>
     *   <li>{@code gender} — male/female first/middle name selection</li>
     *   <li>{@code nationality} — locale selector token (for example: {@code "en"}, {@code "it"})</li>
     * </ul>
     *
     * @param options option bag; must not be {@code null}
     * @return generated full name string
     */
    public String generate(NameOptions options) {
        Objects.requireNonNull(options, "options must not be null");

        Locale locale = options.nationality() == null
                        ? config.getLocale()
                        : resolveNationalityLocale(options.nationality());

        NameGenerators generators = generatorsFor(locale);
        Gender gender = options.gender();

        StringBuilder sb = new StringBuilder();

        if (options.prefix()) {
            sb.append(generators.titleGenerator().generate()).append(' ');
        }

        String firstName = gender == null
                           ? generators.firstNameGenerator().generate()
                           : generators.firstNameGenerator().generate(gender);
        String middlePart = null;
        String lastName = generators.lastNameGenerator().generate();

        if (options.middleInitial()) {
            MiddleNameGenerator middle = generators.middleNameGenerator();
            middlePart = gender == null ? middle.generateInitial() : middle.generateInitial(gender);
        } else if (options.middle()) {
            MiddleNameGenerator middle = generators.middleNameGenerator();
            middlePart = gender == null ? middle.generate() : middle.generate(gender);
        }

        if (options.reverse()) {
            sb.append(lastName).append(' ').append(firstName);
            if (middlePart != null) {
                sb.append(' ').append(middlePart);
            }
        } else {
            sb.append(firstName);
            if (middlePart != null) {
                sb.append(' ').append(middlePart);
            }
            sb.append(' ').append(lastName);
        }

        if (options.suffix()) {
            sb.append(' ').append(generators.suffixGenerator().generate());
        }

        return sb.toString();
    }

    /**
     * Returns the locale this generator was configured with.
     */
    public Locale getLocale() {
        return config.getLocale();
    }

    /**
     * Returns {@code true} if the configured locale has registered name providers.
     */
    public boolean isLocaleExplicitlySupported() {
        return firstNameGenerator.isLocaleExplicitlySupported();
    }

    private NameGenerators generatorsFor(Locale locale) {
        NameGenerators existing = generatorsByLocale.get(locale);
        if (existing != null) {
            return existing;
        }

        GeneratorConfig localeConfig = configForLocale(locale);
        NameGenerators created = new NameGenerators(
            locale,
            new FirstNameGenerator(localeConfig),
            new LastNameGenerator(localeConfig)
        );
        generatorsByLocale.put(locale, created);
        return created;
    }

    private GeneratorConfig configForLocale(Locale locale) {
        GeneratorConfig.Builder builder = GeneratorConfig.builder().locale(locale);
        OptionalLong seed = config.getSeed();
        if (seed.isPresent()) {
            long mixed = seed.getAsLong() ^ locale.toLanguageTag().hashCode();
            builder.seed(mixed);
        }
        return builder.build();
    }


    /**
     * Option bag for Chance-style name generation.
     *
     * @param middle        include a full middle name
     * @param middleInitial include middle initial (takes precedence over middle)
     * @param prefix        include title/prefix
     * @param suffix        include suffix
     * @param reverse       whether to reverse core name order (last name first)
     * @param gender        optional gender selector
     * @param nationality   optional nationality/locale token (for example {@code "en"}, {@code "it"})
     */
    public record NameOptions(
        boolean middle,
        boolean middleInitial,
        boolean prefix,
        boolean suffix,
        boolean reverse,
        Gender gender,
        String nationality
    ) {

        /**
         * Backward-compatible constructor with {@code reverse=false}.
         */
        public NameOptions(
            boolean middle,
            boolean middleInitial,
            boolean prefix,
            boolean suffix,
            Gender gender,
            String nationality
        ) {
            this(middle, middleInitial, prefix, suffix, false, gender, nationality);
        }
    }


    private static final class NameGenerators {

        private final Locale              locale;
        private final FirstNameGenerator  firstNameGenerator;
        private final LastNameGenerator   lastNameGenerator;
        private       TitleGenerator      titleGenerator;
        private       SuffixGenerator     suffixGenerator;
        private       MiddleNameGenerator middleNameGenerator;

        private NameGenerators(Locale locale,
                               FirstNameGenerator firstNameGenerator,
                               LastNameGenerator lastNameGenerator) {
            this.locale = locale;
            this.firstNameGenerator = firstNameGenerator;
            this.lastNameGenerator = lastNameGenerator;
        }

        private FirstNameGenerator firstNameGenerator() {
            return firstNameGenerator;
        }

        private LastNameGenerator lastNameGenerator() {
            return lastNameGenerator;
        }

        private TitleGenerator titleGenerator() {
            if (titleGenerator == null) {
                titleGenerator = new TitleGenerator(locale);
            }
            return titleGenerator;
        }

        private SuffixGenerator suffixGenerator() {
            if (suffixGenerator == null) {
                suffixGenerator = new SuffixGenerator(locale);
            }
            return suffixGenerator;
        }

        private MiddleNameGenerator middleNameGenerator() {
            if (middleNameGenerator == null) {
                middleNameGenerator = new MiddleNameGenerator(locale);
            }
            return middleNameGenerator;
        }
    }
}
