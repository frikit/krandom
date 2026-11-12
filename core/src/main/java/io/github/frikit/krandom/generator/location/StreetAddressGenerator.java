/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import io.github.frikit.krandom.generator.DataRegistryContext;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Vocabulary;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware street addresses (for example {@code "742 Main Street"}).
 *
 * <p>The house number is drawn from [1, 9999]. Street names and short/long suffix lists come
 * from {@link StreetAddressDataRegistry} for the configured locale.
 */
public final class StreetAddressGenerator implements Generator<String>, io.github.frikit.krandom.generator.Seedable {

    /**
     * Reseeds this generator and each of its child streams (city, state, postal code, postal place, country),
     * which then give what ones built from its configuration with this seed give.
     *
     * @param seed deterministic seed
     */
    @Override
    public void reseed(long seed) {
        random.setSeed(seed);
        CityGenerator city = cityGenerator;
        if (city != null) {
            city.reseed(io.github.frikit.krandom.internal.Reseeding.childSeed(config, seed, "city"));
        }
        StateGenerator state = stateGenerator;
        if (state != null) {
            state.reseed(io.github.frikit.krandom.internal.Reseeding.childSeed(config, seed, "state"));
        }
        postalCodeGenerator.reseed(io.github.frikit.krandom.internal.Reseeding.childSeed(config, seed, "postalCode"));
        PlaceDraws draws = placeDraws;
        if (draws != null) {
            draws.random().setSeed(io.github.frikit.krandom.internal.Reseeding.childSeed(config, seed, "postalPlace"));
        }
        AddressCountry addressCountry = country;
        if (addressCountry != null) {
            addressCountry.reseed(io.github.frikit.krandom.internal.Reseeding.childSeed(config, seed, "country"));
        }
    }

    private final GeneratorConfig config;
    private final Random random;
    private final String[] streetNames;
    private final String[] streetTypesShort;
    private final String[] streetTypesLong;
    private final String[] secondaryUnits;

    private final @org.jspecify.annotations.Nullable CityGenerator cityGenerator;
    private final @org.jspecify.annotations.Nullable StateGenerator stateGenerator;
    private final PostalCodeGenerator postalCodeGenerator;
    private final @org.jspecify.annotations.Nullable AddressCountry country;
    private final @org.jspecify.annotations.Nullable PlaceDraws placeDraws;

    /** The postal places of the locale's country, and the child stream their draws come from. */
    private record PlaceDraws(PostalPlaces places, Random random) {}

    /**
     * Creates a street-address generator with default configuration (Locale.US).
     */
    public StreetAddressGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates an unseeded generator for the given locale.
     */
    public StreetAddressGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /**
     * Creates a street-address generator with the specified configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException          if {@code config} is {@code null}
     * @throws UnsupportedOperationException if the configured locale has no registered data
     * @throws IllegalStateException         if the postal places the locale resolves are empty
     */
    public StreetAddressGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        DataRegistryContext registryContext = config.getRegistryContext();
        Locale locale = config.getLocale();
        if (!registryContext.isRegistered(Vocabulary.STREET_ADDRESS, locale)) {
            throw new UnsupportedOperationException("Locale " + locale + " is not supported. Registered locales: "
                    + registryContext.registeredKeys(Vocabulary.STREET_ADDRESS));
        }

        this.random = config.createRandom();

        StreetAddressDataProvider provider =
                java.util.Objects.requireNonNull(registryContext.provider(Vocabulary.STREET_ADDRESS, locale));
        this.streetNames = provider.getStreetNames();
        this.streetTypesShort = provider.getStreetTypesShort();
        this.streetTypesLong = provider.getStreetTypesLong();
        this.secondaryUnits = loadSecondaryUnits(locale);

        this.cityGenerator = registryContext.isRegistered(Vocabulary.CITY, locale)
                ? new CityGenerator(config.forChildStream("city"))
                : null;
        this.stateGenerator = registryContext.isRegistered(Vocabulary.STATE, locale)
                ? new StateGenerator(config.forChildStream("state"))
                : null;
        this.postalCodeGenerator = new PostalCodeGenerator(config.forChildStream("postalCode"));
        PostalPlaces places = PostalPlaces.resolve(config);
        this.placeDraws = places == null
                ? null
                : new PlaceDraws(places, config.forChildStream("postalPlace").createRandom());
        this.country = registryContext.isRegistered(Vocabulary.COUNTRY, locale)
                ? new AddressCountry(new CountryGenerator(config.forChildStream("country")), locale, places)
                : null;
    }

    /**
     * The secondary units of each locale key, read once: a generator only reads its array, so generators of a
     * locale can share it, and a Schema builds one for every value of a seeded street-address column.
     */
    private static final java.util.concurrent.ConcurrentMap<String, String[]> SECONDARY_UNITS =
            new java.util.concurrent.ConcurrentHashMap<>();

    private static String[] loadSecondaryUnits(Locale locale) {
        return SECONDARY_UNITS.computeIfAbsent(
                locale.getLanguage() + "_" + locale.getCountry(), StreetAddressGenerator::readSecondaryUnits);
    }

    private static String[] readSecondaryUnits(String key) {
        String resourcePath = "krandom/streets/secondary_units/" + key + ".txt";
        InputStream is = StreetAddressGenerator.class.getResourceAsStream("/" + resourcePath);
        if (is != null) {
            return StreetAddressResourceLoader.load(is, resourcePath);
        }
        return new String[] {"Apt", "Suite", "Unit", "Floor", "Room"};
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates address with short suffix form by default (Chance-style short_suffix=true).
     */
    @Override
    public String generate() {
        return generate(true);
    }

    /**
     * Generates a random street address of the form {@code "<number> <streetName> <streetType>"}.
     *
     * @param shortSuffix {@code true} for abbreviated suffixes ({@code "St"}, {@code "Ave"}),
     *                    {@code false} for full suffixes ({@code "Street"}, {@code "Avenue"})
     * @return a street-address string; never {@code null}
     */
    public String generate(boolean shortSuffix) {
        return generateStreetAddressNumber() + " " + generateStreetName() + " " + generateStreetSuffix(shortSuffix);
    }

    /**
     * Generates a locale-aware street name.
     *
     * @return street name
     */
    public String generateStreetName() {
        return streetNames[random.nextInt(streetNames.length)];
    }

    /**
     * Generates a street suffix using short form by default (for example, {@code St}, {@code Ave}).
     *
     * @return short street suffix
     */
    public String generateStreetSuffix() {
        return generateStreetSuffix(true);
    }

    /**
     * Generates a street suffix in short or long form.
     *
     * @param shortSuffix true for short form, false for long form
     * @return street suffix
     */
    public String generateStreetSuffix(boolean shortSuffix) {
        String[] types = shortSuffix ? streetTypesShort : streetTypesLong;
        return types[random.nextInt(types.length)];
    }

    /**
     * Generates a numeric street/building number in [1, 9999].
     *
     * @return building number as text
     */
    public String generateStreetAddressNumber() {
        return Integer.toString(random.nextInt(1, 10000));
    }

    /**
     * Alias of {@link #generateStreetAddressNumber()}.
     *
     * @return building number as text
     */
    public String generateBuildingNumber() {
        return generateStreetAddressNumber();
    }

    /**
     * Generates a secondary address component (for example, {@code Apt 12}, {@code Suite 201}).
     *
     * @return secondary address component
     */
    public String generateSecondaryAddress() {
        String unit = secondaryUnits[random.nextInt(secondaryUnits.length)];
        int number = random.nextInt(1, 1000);
        return unit + " " + number;
    }

    /**
     * Generates a full address string combining street, city, state, postal code and country
     * where locale providers are available.
     *
     * <p>Where the locale's country has postal places ({@link Vocabulary#POSTAL_PLACE}), the city,
     * the state (its code where it has one) and the postal code come from one place, so they agree.
     * Elsewhere they are drawn independently from the locale's city and state data and its
     * postal-code format. The country is the locale's country; a locale without one takes the
     * country of its postal places, and without those a random country.
     *
     * @return full address string
     */
    public String generateFullAddress() {
        StringBuilder sb = new StringBuilder(generate());

        PlaceDraws draws = placeDraws;
        if (draws != null) {
            PostalPlace place = draws.places().pick(draws.random());
            sb.append(", ").append(place.city());
            sb.append(", ").append(place.regionCode().isEmpty() ? place.region() : place.regionCode());
            sb.append(" ").append(PostalPlaces.postalCode(place, draws.random()));
        } else {
            if (cityGenerator != null) {
                sb.append(", ").append(cityGenerator.generate());
            }
            if (stateGenerator != null) {
                sb.append(", ").append(stateGenerator.generate(true));
            }
            sb.append(" ").append(postalCodeGenerator.generate());
        }
        if (country != null) {
            sb.append(", ").append(country.name());
        }
        return sb.toString();
    }

    /**
     * Returns configured locale.
     */
    public Locale getLocale() {
        return config.getLocale();
    }

    /**
     * Returns number of locale street names available to this generator instance.
     */
    public int getStreetNameCount() {
        return streetNames.length;
    }

    /**
     * Returns true when configured locale is registered.
     */
    public boolean isLocaleExplicitlySupported() {
        return config.getRegistryContext().isRegistered(Vocabulary.STREET_ADDRESS, config.getLocale());
    }
}
