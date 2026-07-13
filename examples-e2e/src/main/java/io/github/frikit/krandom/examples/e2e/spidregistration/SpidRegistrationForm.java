/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples.e2e.spidregistration;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdSafetyPolicy;
import io.github.frikit.krandom.generator.datetime.DateGenerator;
import io.github.frikit.krandom.generator.datetime.YearGenerator;
import io.github.frikit.krandom.generator.identifier.IdentifierMaskGenerator;
import io.github.frikit.krandom.generator.object.ObjectFaker;
import io.github.frikit.krandom.generator.object.SemanticFieldRegistry;
import io.github.frikit.krandom.generator.user.BirthdayGenerator;
import io.github.frikit.krandom.generator.user.EmailGenerator;
import io.github.frikit.krandom.generator.user.GenderGenerator;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdGenerator;
import io.github.frikit.krandom.jackson.KrandomJackson;

import java.time.LocalDate;
import java.util.Locale;

/**
 * End-to-end example: the profile a citizen submits to enrol an Italian <strong>SPID</strong> digital
 * identity (<em>Sistema Pubblico di Identità Digitale</em>) -- the single login used across every
 * Italian public-administration service. The fields mirror the official SPID attribute set defined by
 * AgID: {@code spidCode}, {@code name}, {@code familyName}, {@code gender}, {@code fiscalNumber},
 * {@code placeOfBirth}, {@code countyOfBirth}, {@code dateOfBirth}, residence {@code address}, contacts,
 * the electronic identity card, and the consents the applicant affirms.
 *
 * <p>The whole graph is filled with a single {@link ObjectFaker} call. SPID uses its own field
 * vocabulary, so instead of pinning every field we <em>teach</em> krandom that vocabulary once with a
 * custom {@link SemanticFieldRegistry} -- {@code name} resolves as a first name, {@code placeOfBirth}
 * as a city, {@code countyOfBirth} as a region, {@code address} as a street -- and everything keeps
 * auto-resolving by name and type ({@code familyName}, {@code mobilePhone}, {@code email}, residence
 * city/CAP, nested records included). Only the Italy-specific government identifiers are pinned, each to
 * a dedicated krandom generator: the <strong>Codice Fiscale</strong> via {@link NationalIdGenerator}
 * (carrying the official {@code TINIT-} prefix SPID assertions require), the SPID code and the
 * electronic ID-card (CIE) number via {@link IdentifierMaskGenerator}, the {@code M}/{@code F} gender
 * via {@link GenderGenerator}, the PEC digital address via {@link EmailGenerator} on the {@code pec.it}
 * domain, the card expiry via {@link YearGenerator}/{@link DateGenerator}, and a realistic 18..99 date
 * of birth via {@link BirthdayGenerator}. Wired to the Italian locale ({@code it_IT}).
 */
public final class SpidRegistrationForm {

    /** This example is demonstrated with the Italian locale. */
    public static final Locale DEFAULT_LOCALE = Locale.ITALY;

    private SpidRegistrationForm() {
    }

    /** The full SPID enrolment profile the portal submits. */
    public record SpidRegistration(
            Identity identity,
            BirthDetails birth,
            Residence residence,
            Contacts contacts,
            ElectronicIdCard idCard,
            Consents consents) {
    }

    public record Identity(
            String spidCode,
            String name,
            String familyName,
            String gender,
            String fiscalNumber) {
    }

    public record BirthDetails(String placeOfBirth, String countyOfBirth, String dateOfBirth) {
    }

    public record Residence(String address, String city, String province, String postalCode, String country) {
    }

    public record Contacts(String mobilePhone, String email, String digitalAddress) {
    }

    public record ElectronicIdCard(
            String documentType,
            String number,
            String issuingAuthority,
            String expirationDate) {
    }

    public record Consents(boolean acceptIdentityProviderTerms, boolean consentToDataProcessing) {
    }

    /** Fills the whole profile in one call, with each Italy-specific identifier pinned to its own generator. */
    public static SpidRegistration fake(Locale locale, long seed) {
        // Teach krandom SPID's field names so the bulk of the form auto-resolves by name and type.
        SemanticFieldRegistry spidVocabulary = SemanticFieldRegistry.defaults().toBuilder()
                .alias("firstname", "name")
                .alias("city", "placeofbirth")
                .alias("state", "countyofbirth")
                .alias("streetaddress", "address")
                .build();

        GeneratorConfig config = GeneratorConfig.builder()
                .locale(locale)
                .seed(seed)
                .objectSemanticRegistry(spidVocabulary)
                .build();

        BirthdayGenerator dateOfBirth = new BirthdayGenerator(18, 99, config);
        GenderGenerator gender = new GenderGenerator(config);
        EmailGenerator email = new EmailGenerator(config);
        IdentifierMaskGenerator identifier = new IdentifierMaskGenerator(config);
        DateGenerator date = new DateGenerator(config);
        // Codice Fiscale is always Italian regardless of the display locale of the rest of the form.
        NationalIdGenerator codiceFiscale = new NationalIdGenerator(GeneratorConfig.builder().locale(Locale.ITALY).seed(seed) .nationalIdSafetyPolicy(NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED) .build());
        // A CIE is valid for up to ten years.
        int thisYear = LocalDate.now().getYear();
        YearGenerator expiryYear = new YearGenerator(thisYear + 1, thisYear + 10, seed + 1);

        return new ObjectFaker<>(SpidRegistration.class, config)
                // Opaque SPID code assigned by the identity provider (4 letters + 10 digits).
                .ruleFor("identity.spidCode", () -> identifier.generate("????##########"))
                // SPID records gender as M/F; take the letter from the gender the generator picks.
                .ruleFor("identity.gender",
                        () -> gender.generate().equals(gender.getMaleLabel()) ? "M" : "F")
                // Codice Fiscale with the official TINIT- prefix SPID SAML assertions carry.
                .ruleFor("identity.fiscalNumber", () -> "TINIT-" + codiceFiscale.generate())
                // Realistic 18..99-year-old, ISO-8601.
                .ruleFor("birth.dateOfBirth", () -> dateOfBirth.generate().toString())
                // Residence is in Italy; the rest of the address auto-resolves to Italian data.
                .ruleFor("residence.country", () -> "Italia")
                // PEC (posta elettronica certificata) -- the SPID digitalAddress, on the pec.it domain.
                .ruleFor("contacts.digitalAddress", () -> email.generate("pec.it"))
                // Carta d'Identità Elettronica (CIE): 2 letters + 5 digits + 2 letters, issued by the Interior Ministry.
                .ruleFor("idCard.documentType", () -> "Carta d'Identità Elettronica")
                .ruleFor("idCard.number", () -> identifier.generate("??#####??"))
                .ruleFor("idCard.issuingAuthority", () -> "Ministero dell'Interno")
                .ruleFor("idCard.expirationDate",
                        () -> date.generateWithYear(expiryYear.generate().getValue()).toString())
                // Consents the applicant must affirm to activate the identity.
                .ruleFor("consents.acceptIdentityProviderTerms", () -> true)
                .ruleFor("consents.consentToDataProcessing", () -> true)
                .generate();
    }

    /** Serializes a freshly generated profile to its final JSON. */
    public static String toJson(Locale locale, long seed) {
        try {
            return KrandomJackson.newObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(fake(locale, seed));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize SPID registration", e);
        }
    }

    /** Convenience: serialize using this example's default (Italian) locale. */
    public static String toJson(long seed) {
        return toJson(DEFAULT_LOCALE, seed);
    }

    public static void main(String[] args) {
        System.out.println(toJson(DEFAULT_LOCALE, System.nanoTime()));
    }
}
