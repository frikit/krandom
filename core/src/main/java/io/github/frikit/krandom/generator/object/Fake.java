/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declarative annotation that maps a field to a named fake-data generator.
 *
 * <p>The {@link #value()} is a semantic key resolved against the same provider
 * registry used by the semantic field-name heuristics. Common keys include:
 *
 * <ul>
 *   <li>{@code "email"} — generates a realistic email address</li>
 *   <li>{@code "firstName"} — generates a first name</li>
 *   <li>{@code "lastName"} — generates a last name</li>
 *   <li>{@code "fullName"} — generates a full name</li>
 *   <li>{@code "phone"} — generates a phone number</li>
 *   <li>{@code "city"} — generates a city name</li>
 *   <li>{@code "country"} — generates a country name</li>
 *   <li>{@code "streetAddress"} — generates a street address</li>
 *   <li>{@code "postalCode"} — generates a postal/zip code</li>
 *   <li>{@code "state"} — generates a state/province</li>
 *   <li>{@code "companyName"} — generates a company name</li>
 *   <li>{@code "url"} — generates a URL</li>
 *   <li>{@code "domain"} — generates a domain name</li>
 *   <li>{@code "username"} — generates a username</li>
 *   <li>{@code "password"} — generates a password</li>
 *   <li>{@code "uuid"} — generates a UUID string</li>
 *   <li>{@code "currency"} — generates a currency code</li>
 *   <li>{@code "industry"} — generates an industry name</li>
 * </ul>
 *
 * <p><b>Usage</b>
 * <pre>{@code
 *   public class ContactForm {
 *       @Fake("email")
 *       private String contactEmail;
 *
 *       @Fake("phone")
 *       private String officePhone;
 *
 *       @Fake("city")
 *       private String hometown;
 *   }
 *
 *   ContactForm form = new ObjectGenerator<>(ContactForm.class).generate();
 *   // form.contactEmail → "alice.jones@example.com"
 *   // form.officePhone  → "+1-555-1234567"
 *   // form.hometown     → "Springfield"
 * }</pre>
 *
 * <p>This annotation takes precedence over the automatic semantic field-name resolution
 * but is overridden by programmatic overrides registered via
 * {@link ObjectGeneratorConfig.Builder}.
 *
 * @see Randomizer
 * @see FakeRange
 */
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Fake {

    /**
     * Semantic key identifying the fake-data generator to use.
     * Case-insensitive; underscores and hyphens are stripped during matching.
     */
    String value();
}
