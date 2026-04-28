/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.namespace;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.location.*;

import java.util.Locale;

/**
 * Fluent namespace for location-related generators.
 *
 * <p>Usage: {@code Generators.location().city().generate()}
 */
public final class LocationGenerators {

    private final GeneratorConfig config;

    public LocationGenerators() {
        this(GeneratorConfig.builder().build());
    }

    public LocationGenerators(GeneratorConfig config) {
        this.config = config;
    }

    public CityGenerator city() { return new CityGenerator(config); }
    public CityGenerator city(Locale locale) { return new CityGenerator(withLocale(locale)); }

    public StateGenerator state() { return new StateGenerator(config); }
    public StateGenerator state(Locale locale) { return new StateGenerator(withLocale(locale)); }

    public CountryGenerator country() { return new CountryGenerator(config); }
    public CountryGenerator country(Locale locale) { return new CountryGenerator(withLocale(locale)); }

    public PostalCodeGenerator postalCode() { return new PostalCodeGenerator(config); }
    public PostalCodeGenerator postalCode(Locale locale) { return new PostalCodeGenerator(withLocale(locale)); }

    public PhoneNumberGenerator phoneNumber() { return new PhoneNumberGenerator(config); }
    public PhoneNumberGenerator phoneNumber(Locale locale) { return new PhoneNumberGenerator(withLocale(locale)); }

    public StreetAddressGenerator streetAddress() { return new StreetAddressGenerator(config); }
    public StreetAddressGenerator streetAddress(Locale locale) { return new StreetAddressGenerator(withLocale(locale)); }

    public AddressInfoGenerator addressInfo() { return new AddressInfoGenerator(config); }
    public AddressInfoGenerator addressInfo(Locale locale) { return new AddressInfoGenerator(withLocale(locale)); }

    public CoordinatesGenerator coordinates() { return new CoordinatesGenerator(config); }

    private GeneratorConfig withLocale(Locale locale) {
        return config.toBuilder().locale(locale).build();
    }
}
