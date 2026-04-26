/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.location.AddressInfo;

/**
 * GoFakeit-style composed person payload.
 *
 * @param contact  contact payload
 * @param address  address payload
 * @param username username
 * @param password password generated with default policy
 */
public record PersonInfo(
    ContactInfo contact,
    AddressInfo address,
    String username,
    String password
) {

}
