/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user.nationalid;

import io.github.frikit.krandom.generator.DataRegistryContext;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generator;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware national identity numbers.
 *
 * <p>Built-in support covers every locale in
 * {@link io.github.frikit.krandom.generator.locale.SupportedLocale}. Additional locales and overrides of
 * built-in ones can be registered at runtime via
 * {@link NationalIdRegistry#register(NationalIdProvider)}.
 *
 * <pre>{@code
 * String ssn    = new NationalIdGenerator(Locale.US).generate();        // "411-90-0070"
 * String ni     = new NationalIdGenerator(Locale.UK).generate();        // "AB 12 34 56 C"
 * String seeded = new NationalIdGenerator(Locale.GERMANY, 42L).generate(); // reproducible
 * }</pre>
 */
public final class NationalIdGenerator implements Generator<String> {

    private final GeneratorConfig    config;
    private final Random             random;
    private final NationalIdProvider provider;

    /**
     * Creates a generator for the given locale using the default fast PRNG.
     *
     * @param locale the locale identifying which national ID format to use; must not be {@code null}
     * @throws UnsupportedOperationException if no provider is registered for the locale
     */
    public NationalIdGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /**
     * Creates a generator for the given locale with a fixed seed for reproducible output.
     *
     * @param locale the locale identifying which national ID format to use; must not be {@code null}
     * @param seed   PRNG seed for reproducible output
     * @throws UnsupportedOperationException if no provider is registered for the locale
     */
    public NationalIdGenerator(Locale locale, long seed) {
        this(GeneratorConfig.builder().locale(locale).seed(seed).build());
    }

    /**
     * Creates a generator with explicit configuration (locale + optional seed + registry context).
     */
    public NationalIdGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        Locale locale = config.getLocale();
        DataRegistryContext registryContext = config.getRegistryContext();
        if (!registryContext.isNationalIdRegistered(locale)) {
            throw new UnsupportedOperationException(
                "Locale " + locale + " is not supported. Registered locales: " +
                registryContext.nationalIdRegisteredKeys());
        }
        this.provider = registryContext.nationalIdProvider(locale);
        this.random = config.createRandom();
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
        return config.getLocale();
    }
}
