/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import java.util.Locale;
import org.jspecify.annotations.Nullable;

/**
 * The country an address names: the configured locale's country; for a locale without one, the
 * country of the postal places it resolved; and otherwise a random country.
 */
final class AddressCountry {

    private final CountryGenerator countryGenerator;
    private final Locale locale;
    private final boolean localeHasCountry;
    private final @Nullable String placeCountryCode;

    AddressCountry(CountryGenerator countryGenerator, Locale locale, @Nullable PostalPlaces places) {
        this.countryGenerator = countryGenerator;
        this.locale = locale;
        this.localeHasCountry = !locale.getCountry().isBlank();
        this.placeCountryCode = places == null || places.countryCode().isEmpty() ? null : places.countryCode();
    }

    /** Reseeds the country stream, as a new build from a configuration with this seed would seed it. */
    void reseedTo(long seed) {
        countryGenerator.reseedTo(seed);
    }

    /** The country's name in the locale's language. */
    String name() {
        if (localeHasCountry) {
            return countryGenerator.currentCountry();
        }
        return placeCountryCode != null
                ? Locale.of("", placeCountryCode).getDisplayCountry(locale)
                : countryGenerator.generate();
    }

    /** The country's ISO 3166-1 alpha-2 code. */
    String code() {
        if (localeHasCountry) {
            return countryGenerator.currentCountryCode();
        }
        return placeCountryCode != null ? placeCountryCode : countryGenerator.generateCode();
    }
}
