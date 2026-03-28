/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user.nationalid;

import org.github.krandom.generator.Generator;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.Random;

/**
 * Generates locale-aware national identity numbers.
 *
 * <p>Built-in support covers 10 locales: US (SSN), GB (NI), AU (TFN), FR (NIR), DE (Steuer-ID),
 * JA (My Number), ES (DNI), IT (Codice Fiscale), BR (CPF), and CN (Resident ID). Additional
 * locales — and overrides of built-in ones — can be registered at runtime via
 * {@link NationalIdRegistry#register(NationalIdProvider)}.
 *
 * <pre>{@code
 * String ssn    = new NationalIdGenerator(Locale.US).generate();        // "411-90-0070"
 * String ni     = new NationalIdGenerator(Locale.UK).generate();        // "AB 12 34 56 C"
 * String seeded = new NationalIdGenerator(Locale.GERMANY, 42L).generate(); // reproducible
 * }</pre>
 */
public final class NationalIdGenerator implements Generator<String> {

    private final Random             random;
    private final NationalIdProvider provider;
    private final Locale             locale;

    /**
     * Creates a generator for the given locale using a secure random source.
     *
     * @param locale the locale identifying which national ID format to use; must not be {@code null}
     * @throws UnsupportedOperationException if no provider is registered for the locale
     */
    public NationalIdGenerator(Locale locale) {
        this(locale, OptionalLong.empty());
    }

    /**
     * Creates a generator for the given locale with a fixed seed for reproducible output.
     *
     * @param locale the locale identifying which national ID format to use; must not be {@code null}
     * @param seed   PRNG seed for reproducible output
     * @throws UnsupportedOperationException if no provider is registered for the locale
     */
    public NationalIdGenerator(Locale locale, long seed) {
        this(locale, OptionalLong.of(seed));
    }

    private NationalIdGenerator(Locale locale, OptionalLong seed) {
        Objects.requireNonNull(locale, "locale must not be null");
        if (!NationalIdRegistry.isRegistered(locale)) {
            throw new UnsupportedOperationException(
                "Locale " + locale + " is not supported. Registered locales: " +
                NationalIdRegistry.registeredKeys());
        }
        this.locale = locale;
        this.provider = NationalIdRegistry.forLocale(locale);
        this.random = seed.isPresent() ? new Random(seed.getAsLong()) : new SecureRandom();
    }

    /**
     * Generates a national ID string in the format appropriate for this generator's locale.
     *
     * @return a formatted national ID string; never {@code null}
     */
    @Override
    public String generate() {
        return provider.generate(random);
    }

    /**
     * Returns the locale this generator was configured with.
     *
     * @return the locale; never {@code null}
     */
    public Locale getLocale() {
        return locale;
    }
}
