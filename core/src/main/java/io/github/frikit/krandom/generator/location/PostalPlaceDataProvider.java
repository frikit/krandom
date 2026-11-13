/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import io.github.frikit.krandom.generator.LocaleDataProvider;
import java.util.List;
import java.util.Locale;

/**
 * Contract for a country's postal places: cities with the region they lie in and the postal codes
 * that serve them. {@link AddressInfoGenerator} and {@link StreetAddressGenerator#generateFullAddress()}
 * draw one place for each address, so that its city, region and postal code agree.
 *
 * <p>Postal geography is national rather than linguistic, so a registration also serves the other
 * locales of its country: {@code es_US} uses the {@code en_US} places. A locale whose country has no
 * places never borrows another country's through its language; {@code en_CA} resolves none, and its
 * addresses are composed as before, from independent city, region and postal-code draws.
 *
 * <pre>{@code
 * DataRegistryContext context = DataRegistryContext.builder()
 *     .register(Vocabulary.POSTAL_PLACE, new PostalPlaceDataProvider() {
 *         public Locale getLocale() { return Locale.forLanguageTag("es-MX"); }
 *         public List<PostalPlace> getPlaces() {
 *             return List.of(new PostalPlace("Guadalajara", "Jalisco", "JAL", List.of("44100", "44160")));
 *         }
 *     })
 *     .build();
 * }</pre>
 *
 * <p>The built-in places are seeded by {@link PostalPlaceDataRegistry}. A registration in a context
 * takes precedence over the built-in places for the same locale key.
 */
public interface PostalPlaceDataProvider extends LocaleDataProvider {

    /**
     * The locale this provider supplies places for; its country is the country of the places.
     *
     * @return non-null locale
     */
    @Override
    Locale getLocale();

    /**
     * Returns the places, each with at least one postal code.
     *
     * @return non-null, non-empty list without {@code null} entries
     */
    List<PostalPlace> getPlaces();
}
