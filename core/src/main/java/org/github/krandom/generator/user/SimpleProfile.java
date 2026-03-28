/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import java.time.LocalDate;

/**
 * Minimal user profile similar to Faker's {@code simple_profile()} output.
 *
 * @param username  generated username
 * @param name      full name
 * @param sex       sex marker ({@code "M"} or {@code "F"})
 * @param address   full address
 * @param mail      email address
 * @param birthdate date of birth
 */
public record SimpleProfile(
    String username,
    String name,
    String sex,
    String address,
    String mail,
    LocalDate birthdate
) {

}
