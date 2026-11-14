/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Vocabulary;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates a city with its region and one of its postal codes, taken together from one place of the
 * locale's {@link Vocabulary#POSTAL_PLACE} data, so they belong together. {@link CityGenerator} and
 * {@link PostalCodeGenerator} draw independently, so their values need not agree.
 *
 * <p>Each value draws a place, then one of its postal codes. Places resolve as
 * {@link AddressInfoGenerator}'s do: by the locale's country, or for a locale without a country by its
 * language. The bundled places cover the United States, Great Britain, Germany and France, so
 * {@code en_US}, {@code es_US}, {@code en_GB}, {@code de_DE}, {@code fr_FR}, {@code Locale.ENGLISH} and
 * the like have them; another locale needs places registered for its country.
 */
public final class PostalLocalityGenerator implements Generator<PostalLocality> {

    private final GeneratorConfig config;
    private final PostalPlaces places;
    private final Random random;

    /**
     * Creates a generator with the default configuration ({@link Locale#US}).
     */
    public PostalLocalityGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates an unseeded generator for the given locale.
     *
     * @param locale locale whose postal places to draw from
     * @throws UnsupportedOperationException if the locale has no postal places
     */
    public PostalLocalityGenerator(Locale locale) {
        this(GeneratorConfig.builder()
                .locale(Objects.requireNonNull(locale, "locale must not be null"))
                .build());
    }

    /**
     * Creates a generator with the given configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws UnsupportedOperationException if the configured locale has no postal places
     * @throws IllegalStateException         if the postal places the locale resolves are empty
     */
    public PostalLocalityGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        PostalPlaces resolved = PostalPlaces.resolve(config);
        if (resolved == null) {
            throw new UnsupportedOperationException("Locale " + config.getLocale() + " has no postal places."
                    + " Registered locales: "
                    + config.getRegistryContext().registeredKeys(Vocabulary.POSTAL_PLACE));
        }
        this.places = resolved;
        this.random = config.createRandom();
    }

    /**
     * Returns a city, its region and one of its postal codes, from one place.
     *
     * @return a postal locality
     */
    @Override
    public PostalLocality generate() {
        PostalPlace place = places.pick(random);
        return new PostalLocality(
                place.city(), place.region(), place.regionCode(), PostalPlaces.postalCode(place, random));
    }

    /**
     * Returns the configured locale.
     *
     * @return the locale
     */
    public Locale getLocale() {
        return config.getLocale();
    }
}
