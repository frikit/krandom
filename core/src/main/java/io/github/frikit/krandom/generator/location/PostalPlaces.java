/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Vocabulary;
import java.util.List;
import java.util.Random;
import org.jspecify.annotations.Nullable;

/**
 * The postal places a configuration resolves, from which an address takes its city, region and
 * postal code together.
 *
 * @param places      the places, never empty
 * @param countryCode the country of the provider's locale, or {@code ""} for a language-only provider
 */
record PostalPlaces(List<PostalPlace> places, String countryCode) {

    /**
     * Resolves the postal places of a configuration's locale.
     *
     * @return the places, or {@code null} when the locale's country has none
     * @throws IllegalStateException if the resolved provider returns no places
     */
    static @Nullable PostalPlaces resolve(GeneratorConfig config) {
        PostalPlaceDataProvider provider =
                config.getRegistryContext().provider(Vocabulary.POSTAL_PLACE, config.getLocale());
        if (provider == null) {
            return null;
        }
        List<PostalPlace> places = List.copyOf(provider.getPlaces());
        if (places.isEmpty()) {
            throw new IllegalStateException("The postal places for " + provider.getLocale() + " that locale "
                    + config.getLocale() + " resolves are empty");
        }
        return new PostalPlaces(places, provider.getLocale().getCountry());
    }

    /** Draws a place. */
    PostalPlace pick(Random random) {
        return places.get(random.nextInt(places.size()));
    }

    /** Draws one of a place's postal codes. */
    static String postalCode(PostalPlace place, Random random) {
        List<String> codes = place.postalCodes();
        return codes.get(random.nextInt(codes.size()));
    }
}
