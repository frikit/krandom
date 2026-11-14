/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

/**
 * A city with the region it lies in and one of its postal codes, all taken from one
 * {@link PostalPlace} of the {@link io.github.frikit.krandom.generator.Vocabulary#POSTAL_PLACE} data,
 * so that the postal code serves the city: {@code Grand Rapids}, {@code Michigan}, {@code MI} and
 * {@code 49503}, say.
 *
 * @param city       the city or town
 * @param region     the first-level region the city lies in, such as a state, province or, in the
 *                   United Kingdom, a country
 * @param regionCode the region's official short code, such as {@code MI}, or {@code ""} where the
 *                   locale's regions have no codes
 * @param postalCode one of the postal codes that serve the city, written in full
 * @see PostalLocalityGenerator
 */
public record PostalLocality(String city, String region, String regionCode, String postalCode) {}
