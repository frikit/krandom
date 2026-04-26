/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.location.AddressInfo;

/**
 * Structured company payload for end-to-end fixtures.
 *
 * @param name        company display name
 * @param industry    industry/category label
 * @param catchPhrase catch phrase or tagline
 * @param buzzword    business buzzword phrase
 * @param email       company contact email
 * @param website     company website URL
 * @param phone       company contact phone number
 * @param address     company address payload
 */
public record CompanyInfo(
    String name,
    String industry,
    String catchPhrase,
    String buzzword,
    String email,
    String website,
    String phone,
    AddressInfo address
) {

}
