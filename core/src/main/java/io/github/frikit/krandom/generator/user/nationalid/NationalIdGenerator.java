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
 * {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder}.
 *
 * <p>Use an explicit {@link GeneratorConfig} and select
 * {@link NationalIdSafetyPolicy#REALISTIC_UNCLASSIFIED} only for isolated compatibility fixtures.
 * The default configured policy is {@link NationalIdSafetyPolicy#DISABLED}.
 */
public final class NationalIdGenerator implements Generator<String> {

    private final GeneratorConfig    config;
    private final Random             random;
    private final NationalIdProvider provider;



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
     * @throws IllegalStateException when the configured policy fails closed
     */
    @Override
    public String generate() {
        if (config.getNationalIdSafetyPolicy() == NationalIdSafetyPolicy.DISABLED) {
            throw new IllegalStateException(
                "National-ID generation is disabled by default; select nationalIdSafetyPolicy(REALISTIC_UNCLASSIFIED) "
                + "only for isolated fixtures");
        }
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
