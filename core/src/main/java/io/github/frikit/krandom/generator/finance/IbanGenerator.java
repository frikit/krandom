/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates ISO 13616 IBANs: a country code, ISO 7064 MOD 97-10 check digits, and a BBAN that
 * follows the IBAN-registry structure of that same country.
 *
 * <p><strong>Country:</strong> the IBAN country is the country of the configured locale when that
 * country has an IBAN format built in: AT, BE, BG, BR, CH, CZ, DE, DK, ES, FI, FR, GB, GR, HR, HU,
 * IE, IL, IT, NL, NO, PL, PT, RO, RU, SA, SE, SK, TR, and UA. Every other locale, including
 * countries without IBANs (for example {@code en_US}, {@code ja_JP}, {@code zh_CN},
 * {@code en_AU}), locales without a country, and the default {@code en_US} configuration, produces
 * German ({@code DE}) IBANs. All values of one generator therefore share one country.
 *
 * <p><strong>BBAN:</strong> the BBAN has the registry length and character classes of the emitted
 * country; for BE, CZ, ES, FI, FR, HR, HU, IT, NO, PL, PT, and SK its national check digits are
 * valid as well. It is built exactly like {@link BbanGenerator} output for the same configuration,
 * so a seeded {@code BbanGenerator} returns the BBAN part of the corresponding IBAN.
 *
 * <p>Structural validity is not evidence that an account or institution does not exist; never send
 * generated IBANs to payment or banking systems. Use an explicit {@link GeneratorConfig} and select
 * {@link BankingSafetyPolicy#REALISTIC_UNCLASSIFIED} only for isolated compatibility fixtures.
 * The default configured policy is {@link BankingSafetyPolicy#DISABLED}.
 */
public final class IbanGenerator implements Generator<String> {

    private final Random              random;
    private final IbanCountryFormat   format;
    private final BankingSafetyPolicy bankingSafetyPolicy;

    /**
     * Creates an IBAN generator for the country resolved from the configured locale.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public IbanGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.random = effective.createRandom();
        this.format = IbanCountryFormat.forLocale(effective.getLocale());
        this.bankingSafetyPolicy = effective.getBankingSafetyPolicy();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if the configured banking safety policy is
     *                               {@link BankingSafetyPolicy#DISABLED}
     */
    @Override
    public String generate() {
        bankingSafetyPolicy.requireRealisticOutput();
        String country = format.name();
        String bban = format.randomBban(random);
        // Locale.ROOT keeps the check digits ASCII under JVM default locales with native digits.
        return country + String.format(Locale.ROOT, "%02d", computeCheckDigits(country, bban)) + bban;
    }

    /** ISO 7064 MOD 97-10 check digits (2 to 98) of {@code bban + country + "00"}. */
    private int computeCheckDigits(String country, String bban) {
        String rearranged = bban + country + "00";
        int remainder = 0;
        for (int i = 0; i < rearranged.length(); i++) {
            char ch = rearranged.charAt(i);
            remainder = ch <= '9'
                        ? (remainder * 10 + ch - '0') % 97
                        : (remainder * 100 + ch - 'A' + 10) % 97;
        }
        return 98 - remainder;
    }
}
