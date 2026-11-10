/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The email shape, against Hibernate Validator's {@code @Email} run through the Bean Validation API. */
@DisplayName("Email shape")
class EmailShapeTest {

    private static final Validator VALIDATOR = Validation.byProvider(HibernateValidator.class)
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory()
            .getValidator();

    private static final String LONGEST_LOCAL_PART = "a".repeat(64);
    private static final String LONGEST_LABEL = "b".repeat(63);
    /** 128 labels of one letter: 255 characters. */
    private static final String LONGEST_DOMAIN = String.join(".", Collections.nCopies(128, "c"));

    private static final String LETTERS = "abcxyzABCXYZ0189";
    /** What an address may hold only in places, or not at all. */
    private static final String SPECIALS = "!#$%&'*+/=?^_`{|}~\"()[],:;<>@\\ \tü　";

    @Test
    @DisplayName("accepts the dot-atom addresses Hibernate Validator accepts")
    void acceptsDotAtomAddressesHibernateValidatorAccepts() {
        List<String> addresses = List.of(
                "a@example.com",
                "john.smith@example.com",
                "JOHN.SMITH@EXAMPLE.COM",
                "x_y-z+tag@sub.example.co.uk",
                "!#$%&'*+/=?^_`{|}~-@example.com",
                "a@b-c--d.e",
                "a@_#$.example",
                "a@localhost",
                "jürgen@bücher.example",
                LONGEST_LOCAL_PART + "@example.com",
                "a@" + LONGEST_LABEL + ".com",
                "a@" + LONGEST_DOMAIN);
        for (String address : addresses) {
            assertTrue(hibernateAccepts(address), address);
            assertTrue(EmailShape.accepts(address), address);
        }
    }

    @Test
    @DisplayName("rejects what Hibernate Validator rejects")
    void rejectsWhatHibernateValidatorRejects() {
        List<String> addresses = List.of(
                "plain",
                "@example.com",
                "a@",
                ".a@example.com",
                "a.@example.com",
                "a..b@example.com",
                "a b@example.com",
                "a(b)@example.com",
                "a,b@example.com",
                "a@b@example.com",
                "a@-example.com",
                "a@example-.com",
                "a@.example.com",
                "a@example..com",
                "a@example.com.",
                "a@exa mple.com",
                "a@example.com\n",
                // A private-use character, which IDN conversion refuses.
                "a@example.com",
                "a" + LONGEST_LOCAL_PART + "@example.com",
                "a@b" + LONGEST_LABEL + ".com",
                "a@c" + LONGEST_DOMAIN,
                // 79 characters, and 319 once converted to ASCII.
                "a@" + String.join(".", Collections.nCopies(40, "ü")));
        for (String address : addresses) {
            assertFalse(hibernateAccepts(address), address);
            assertFalse(EmailShape.accepts(address), address);
        }
    }

    @Test
    @DisplayName(
            "refuses the empty string, a quoted local part and an address literal, which Hibernate Validator accepts")
    void refusesWhatOnlyHibernateValidatorAccepts() {
        for (String address :
                List.of("", "\"a b\"@example.com", "\"a\"@example.com", "a@[192.0.2.1]", "a@[IPv6:2001:db8::1]")) {
            assertTrue(hibernateAccepts(address), address);
            assertFalse(EmailShape.accepts(address), address);
        }
    }

    @Test
    @DisplayName("agrees with Hibernate Validator on random candidates")
    void agreesWithHibernateValidatorOnRandomCandidates() {
        Random random = new Random(2026);
        int accepted = 0;
        for (int i = 0; i < 20_000; i++) {
            String candidate = part(random, 6) + "@" + part(random, 10);
            boolean shape = EmailShape.accepts(candidate);
            if (candidate.indexOf('"') < 0 && candidate.indexOf('[') < 0) {
                assertEquals(hibernateAccepts(candidate), shape, candidate);
            } else if (shape) {
                // Only a quoted local part or an address literal may be left to Hibernate Validator alone.
                assertTrue(hibernateAccepts(candidate), candidate);
            }
            accepted += shape ? 1 : 0;
        }
        assertTrue(accepted > 2_000, "only " + accepted + " candidates were addresses");
    }

    private static String part(Random random, int maxLength) {
        int length = random.nextInt(maxLength + 1);
        StringBuilder part = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int kind = random.nextInt(20);
            if (kind < 15) {
                part.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
            } else if (kind < 17) {
                part.append('.');
            } else if (kind < 18) {
                part.append('-');
            } else {
                part.append(SPECIALS.charAt(random.nextInt(SPECIALS.length())));
            }
        }
        return part.toString();
    }

    private static boolean hibernateAccepts(String address) {
        return VALIDATOR.validate(new Address(address)).isEmpty();
    }

    /** A consumer's bean with an {@code @Email} field. */
    static final class Address {

        @Email private final String value;

        Address(String value) {
            this.value = value;
        }
    }
}
