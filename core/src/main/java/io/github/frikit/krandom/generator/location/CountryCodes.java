/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

/**
 * The three ISO 3166-1 codes of one country, so a generated alpha-2, alpha-3 and numeric code always
 * belong to the same country.
 *
 * @param alpha2  two-letter code, for example {@code "GB"}
 * @param alpha3  three-letter code, for example {@code "GBR"}
 * @param numeric three-digit numeric code, for example {@code "826"}
 */
public record CountryCodes(String alpha2, String alpha3, String numeric) {}
