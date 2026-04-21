/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import org.github.krandom.generator.DataRegistryContext;
import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates structured address payloads with coherent sibling fields.
 */
public final class AddressInfoGenerator implements Generator<AddressInfo> {

    private final GeneratorConfig config;
    private final Random          random;
    private final StreetAddressGenerator streetAddressGenerator;
    private final PostalCodeGenerator postalCodeGenerator;
    private final CountryGenerator    countryGenerator;
    private final String[]            cities;
    private final String[]            states;
    private final String[]            stateAbbreviations;

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
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates an address-info generator using explicit configuration.
     *
     * @param config generator configuration
     */
    public AddressInfoGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.streetAddressGenerator = new StreetAddressGenerator(config);
        this.postalCodeGenerator = new PostalCodeGenerator(config);
        this.countryGenerator = new CountryGenerator(config);

        DataRegistryContext registryContext = config.getRegistryContext();
        Locale locale = config.getLocale();
        this.cities = registryContext.isCityRegistered(locale)
                      ? registryContext.cityProvider(locale).getCities()
                      : new String[0];

        if (registryContext.isStateRegistered(locale)) {
            StateDataProvider provider = registryContext.stateProvider(locale);
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
        String street = joinNonBlank(" ", streetNumber, streetPrefix, streetName, streetSuffix);

        String city = pick(cities);

        int stateIndex = states.length == 0 ? -1 : random.nextInt(states.length);
        String state = stateIndex >= 0 ? states[stateIndex] : "";
        String stateAbbr = stateIndex >= 0 && stateIndex < stateAbbreviations.length
                           ? safe(stateAbbreviations[stateIndex])
                           : "";

        String zip = postalCodeGenerator.generate();
        String country = currentOrRandomCountry();
        String countryAbbr = currentOrRandomCountryCode();
        String address = formatAddress(street, streetUnit, city, stateAbbr.isBlank() ? state : stateAbbr, zip, country);

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
            country,
            countryAbbr
        );
    }

    /**
     * Returns the configured locale.
     */
    public Locale getLocale() {
        return config.getLocale();
    }

    private String currentOrRandomCountry() {
        try {
            return countryGenerator.currentCountry();
        } catch (UnsupportedOperationException ignored) {
            return countryGenerator.generate();
        }
    }

    private String currentOrRandomCountryCode() {
        try {
            return countryGenerator.currentCountryCode();
        } catch (UnsupportedOperationException ignored) {
            return countryGenerator.generateCode();
        }
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
        String street,
        String streetUnit,
        String city,
        String stateOrAbbreviation,
        String zip,
        String country
    ) {
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
