/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.gofakeit;

/**
 * GoFakeit-style composed contact payload.
 *
 * @param firstName first name
 * @param lastName last name
 * @param name full name
 * @param gender localized gender label
 * @param age age in years
 * @param phone unformatted phone number
 * @param phoneFormatted formatted phone number
 * @param email email address
 */
public record ContactInfo(
        String firstName,
        String lastName,
        String name,
        String gender,
        int age,
        String phone,
        String phoneFormatted,
        String email
) {
}
