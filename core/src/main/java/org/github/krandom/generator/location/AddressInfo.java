/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

/**
 * GoFakeit-style composed address payload.
 *
 * @param address      full formatted address line
 * @param street       street line (number + name + suffix)
 * @param streetNumber building number component
 * @param streetName   street name component
 * @param streetSuffix street suffix component
 * @param streetPrefix street prefix/direction component
 * @param streetUnit   secondary address unit component
 * @param city         city/town component
 * @param state        state/province full name
 * @param stateAbbr    state/province abbreviation
 * @param zip          postal/zip code
 * @param country      country full name
 * @param countryAbbr  country alpha-2 code
 */
public record AddressInfo(
    String address,
    String street,
    String streetNumber,
    String streetName,
    String streetSuffix,
    String streetPrefix,
    String streetUnit,
    String city,
    String state,
    String stateAbbr,
    String zip,
    String country,
    String countryAbbr
) {

}
