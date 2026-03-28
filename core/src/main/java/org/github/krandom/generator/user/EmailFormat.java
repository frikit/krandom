/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

/**
 * Enumeration of email address formats.
 *
 * <p>Defines various common formats for constructing email local parts
 * (the part before the @ symbol) from first and last names.
 *
 * <p><strong>Examples for "John Smith":</strong>
 * <ul>
 *   <li><strong>FIRSTNAME_DOT_LASTNAME</strong>: john.smith@example.com</li>
 *   <li><strong>FIRSTNAME_LASTNAME</strong>: johnsmith@example.com</li>
 *   <li><strong>FIRSTINITIAL_LASTNAME</strong>: jsmith@example.com</li>
 *   <li><strong>FIRSTNAME_UNDERSCORE_LASTNAME</strong>: john_smith@example.com</li>
 *   <li><strong>LASTNAME_DOT_FIRSTNAME</strong>: smith.john@example.com</li>
 * </ul>
 *
 * <p><strong>Usage Example:</strong>
 * <pre>{@code
 * EmailGenerator gen = new EmailGenerator();
 *
 * // Use specific format
 * String email1 = gen.generate(EmailFormat.FIRSTNAME_DOT_LASTNAME);
 * // "john.smith@gmail.com"
 *
 * String email2 = gen.generate(EmailFormat.FIRSTINITIAL_LASTNAME);
 * // "jsmith@yahoo.com"
 * }</pre>
 */
public enum EmailFormat {

    /**
     * Format: firstname.lastname
     * <p>Example: john.smith@example.com
     * <p>Most common professional email format.
     */
    FIRSTNAME_DOT_LASTNAME,

    /**
     * Format: firstnamelastname
     * <p>Example: johnsmith@example.com
     * <p>Common for personal email addresses.
     */
    FIRSTNAME_LASTNAME,

    /**
     * Format: flastname (first initial + last name)
     * <p>Example: jsmith@example.com
     * <p>Common for corporate email addresses.
     */
    FIRSTINITIAL_LASTNAME,

    /**
     * Format: firstname_lastname
     * <p>Example: john_smith@example.com
     * <p>Alternative separator format.
     */
    FIRSTNAME_UNDERSCORE_LASTNAME,

    /**
     * Format: lastname.firstname
     * <p>Example: smith.john@example.com
     * <p>Common in some European countries.
     */
    LASTNAME_DOT_FIRSTNAME;
}
