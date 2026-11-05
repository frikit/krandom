/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import java.util.List;
import java.util.Objects;

/**
 * A city or town with the first-level region it lies in and postal codes that serve it: one entry of
 * the {@link io.github.frikit.krandom.generator.Vocabulary#POSTAL_PLACE} data from which
 * {@link AddressInfoGenerator} draws an address whose city, region and postal code agree.
 *
 * @param city        the city or town
 * @param region      the first-level region the city lies in, such as a state, province or, in the
 *                    United Kingdom, a country
 * @param regionCode  the region's official short code, such as {@code MI}, or {@code ""} where the
 *                    locale's regions have no codes
 * @param postalCodes postal codes that serve the city, each written in full; at least one
 */
public record PostalPlace(String city, String region, String regionCode, List<String> postalCodes) {

    /**
     * Creates a postal place, copying {@code postalCodes}.
     *
     * @throws NullPointerException     if an argument is {@code null}
     * @throws IllegalArgumentException if {@code city} or {@code region} is blank, or
     *                                  {@code postalCodes} is empty or holds a blank or {@code null}
     *                                  code
     */
    public PostalPlace {
        requireText(city, "city");
        requireText(region, "region");
        Objects.requireNonNull(regionCode, "regionCode");
        Objects.requireNonNull(postalCodes, "postalCodes");
        if (postalCodes.isEmpty()) {
            throw new IllegalArgumentException("postalCodes of " + city + " must not be empty");
        }
        for (int i = 0; i < postalCodes.size(); i++) {
            String code = postalCodes.get(i);
            if (code == null || code.isBlank()) {
                throw new IllegalArgumentException("postal code at index " + i + " of " + city + " must not be blank");
            }
        }
        postalCodes = List.copyOf(postalCodes);
    }

    private static void requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
