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
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import org.jspecify.annotations.Nullable;

/**
 * Generates structured address payloads with coherent sibling fields.
 *
 * <p>Where the locale's country has postal places ({@link Vocabulary#POSTAL_PLACE}), each address
 * takes its city, state and postal code from one place, so they agree: an {@code en_US} address in
 * Grand Rapids is in Michigan, with a Grand Rapids ZIP code. {@link PostalPlaceDataRegistry} holds the
 * built-in places. Elsewhere the city, the state and the postal code are drawn independently, from
 * the locale's city and state data and its postal-code format.
 *
 * <p>The country is the locale's country. A locale without one takes the country of its postal
 * places, and without those a random country.
 *
 * <p>With {@link GeneratorConfig.Builder#germanStreetLayout(boolean)} on, a German locale's
 * {@link AddressInfo#street()}, and so its address, puts the street before the house number, such as
 * {@code "Hauptstr. 12"}; the street's parts stay as they are.
 */
public final class AddressInfoGenerator implements Generator<AddressInfo> {

    static {
        io.github.frikit.krandom.internal.Reseeding.register(
                AddressInfoGenerator.class, AddressInfoGenerator::reseedTo);
    }

    /**
     * Reseeds this generator and its street-address, postal-code and country streams, which then give what
     * ones built from its configuration with this seed give. Its postal places and its city and state data
     * are read only.
     */
    void reseedTo(long seed) {
        random.setSeed(seed);
        streetAddressGenerator.reseedTo(
                io.github.frikit.krandom.internal.Reseeding.childSeed(config, seed, "streetAddress"));
        postalCodeGenerator.reseedTo(io.github.frikit.krandom.internal.Reseeding.childSeed(config, seed, "postalCode"));
        country.reseedTo(io.github.frikit.krandom.internal.Reseeding.childSeed(config, seed, "country"));
    }

    private final GeneratorConfig config;
    private final Random random;
    private final StreetAddressGenerator streetAddressGenerator;
    private final PostalCodeGenerator postalCodeGenerator;
    private final AddressCountry country;
    private final @Nullable PostalPlaces places;
    private final String[] cities;
    private final String[] states;
    private final String[] stateAbbreviations;

    /**
     * Creates an address-info generator using default configuration ({@link Locale#US}).
     */
    public AddressInfoGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates an address-info generator for the specified locale.
     *
     * @param locale locale to use
     */
    public AddressInfoGenerator(Locale locale) {
        this(GeneratorConfig.builder()
                .locale(Objects.requireNonNull(locale, "locale must not be null"))
                .build());
    }

    /**
     * Creates an address-info generator using explicit configuration.
     *
     * @param config generator configuration
     * @throws IllegalStateException if the postal places the locale resolves are empty
     */
    public AddressInfoGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.streetAddressGenerator = new StreetAddressGenerator(config.forChildStream("streetAddress"));
        this.postalCodeGenerator = new PostalCodeGenerator(config.forChildStream("postalCode"));
        CountryGenerator countryGenerator = new CountryGenerator(config.forChildStream("country"));
        this.places = PostalPlaces.resolve(config);
        this.country = new AddressCountry(countryGenerator, config.getLocale(), places);

        DataRegistryContext registryContext = config.getRegistryContext();
        Locale locale = config.getLocale();
        this.cities = registryContext.isRegistered(Vocabulary.CITY, locale)
                ? java.util.Objects.requireNonNull(registryContext.provider(Vocabulary.CITY, locale))
                        .getCities()
                : new String[0];

        if (registryContext.isRegistered(Vocabulary.STATE, locale)) {
            StateDataProvider provider =
                    java.util.Objects.requireNonNull(registryContext.provider(Vocabulary.STATE, locale));
            this.states = provider.getStates();
            this.stateAbbreviations = provider.getAbbreviations();
        } else {
            this.states = new String[0];
            this.stateAbbreviations = new String[0];
        }
    }

    @Override
    public AddressInfo generate() {
        String streetNumber = streetAddressGenerator.generateStreetAddressNumber();
        String streetName = streetAddressGenerator.generateStreetName();
        String streetSuffix = streetAddressGenerator.generateStreetSuffix();
        String streetPrefix = "";
        String streetUnit = random.nextInt(3) == 0 ? streetAddressGenerator.generateSecondaryAddress() : "";
        String street = streetAddressGenerator.streetFirst()
                ? StreetAddressGenerator.streetFirstLine(streetNumber, streetName, streetSuffix)
                : joinNonBlank(" ", streetNumber, streetPrefix, streetName, streetSuffix);

        String city;
        String state;
        String stateAbbr;
        String zip;
        PostalPlaces resolved = places;
        if (resolved != null) {
            PostalPlace place = resolved.pick(random);
            city = place.city();
            state = place.region();
            stateAbbr = place.regionCode();
            zip = PostalPlaces.postalCode(place, random);
        } else {
            city = pick(cities);
            int stateIndex = states.length == 0 ? -1 : random.nextInt(states.length);
            state = stateIndex >= 0 ? states[stateIndex] : "";
            stateAbbr = stateIndex >= 0 && stateIndex < stateAbbreviations.length
                    ? safe(stateAbbreviations[stateIndex])
                    : "";
            zip = postalCodeGenerator.generate();
        }

        String countryName = country.name();
        String countryAbbr = country.code();
        String address =
                formatAddress(street, streetUnit, city, stateAbbr.isBlank() ? state : stateAbbr, zip, countryName);

        return new AddressInfo(
                address,
                street,
                streetNumber,
                streetName,
                streetSuffix,
                streetPrefix,
                streetUnit,
                city,
                state,
                stateAbbr,
                zip,
                countryName,
                countryAbbr);
    }

    /**
     * Returns the configured locale.
     */
    public Locale getLocale() {
        return config.getLocale();
    }

    private String pick(String[] values) {
        if (values.length == 0) {
            return "";
        }
        return values[random.nextInt(values.length)];
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String formatAddress(
            String street, String streetUnit, String city, String stateOrAbbreviation, String zip, String country) {
        String streetLine = street;
        if (!streetUnit.isBlank()) {
            streetLine = streetLine + ", " + streetUnit;
        }
        String locality = joinNonBlank(" ", stateOrAbbreviation, zip);
        return joinNonBlank(", ", streetLine, city, locality, country);
    }

    private static String joinNonBlank(String delimiter, String... values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(delimiter);
            }
            builder.append(value);
        }
        return builder.toString();
    }
}
