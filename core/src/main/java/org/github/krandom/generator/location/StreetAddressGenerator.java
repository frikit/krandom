/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.DataRegistryContext;

import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware street addresses (for example {@code "742 Main Street"}).
 *
 * <p>The house number is drawn from [1, 9999]. Street names and short/long suffix lists come
 * from {@link StreetAddressDataRegistry} for the configured locale.
 */
public final class StreetAddressGenerator implements Generator<String> {

    private final GeneratorConfig config;
    private final Random          random;
    private final String[]        streetNames;
    private final String[]        streetTypesShort;
    private final String[]        streetTypesLong;
    private final String[]        secondaryUnits;

    private final CityGenerator       cityGenerator;
    private final StateGenerator      stateGenerator;
    private final PostalCodeGenerator postalCodeGenerator;
    private final CountryGenerator    countryGenerator;

    /**
     * Creates a street-address generator with default configuration (Locale.US).
     */
    public StreetAddressGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates an unseeded generator for the given locale.
     */
    public StreetAddressGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /**
     * Creates a street-address generator with the specified configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException          if {@code config} is {@code null}
     * @throws UnsupportedOperationException if the configured locale has no registered data
     */
    public StreetAddressGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        DataRegistryContext registryContext = config.getRegistryContext();
        Locale locale = config.getLocale();
        if (!registryContext.isStreetAddressRegistered(locale)) {
            throw new UnsupportedOperationException(
                "Locale " + locale + " is not supported. Registered locales: "
                + registryContext.streetAddressRegisteredKeys());
        }

        this.random = config.createRandom();

        StreetAddressDataProvider provider = registryContext.streetAddressProvider(locale);
        this.streetNames = provider.getStreetNames();
        this.streetTypesShort = provider.getStreetTypesShort();
        this.streetTypesLong = provider.getStreetTypesLong();
        this.secondaryUnits = loadSecondaryUnits(locale);

        this.cityGenerator = registryContext.isCityRegistered(locale) ? new CityGenerator(config) : null;
        this.stateGenerator = registryContext.isStateRegistered(locale) ? new StateGenerator(config) : null;
        this.postalCodeGenerator = new PostalCodeGenerator(config);
        this.countryGenerator = registryContext.isCountryRegistered(locale) ? new CountryGenerator(config) : null;
    }

    private static String[] loadSecondaryUnits(Locale locale) {
        String key = locale.getLanguage() + "_" + locale.getCountry();
        String resourcePath = "krandom/streets/" + key + "_secondary_units.txt";
        InputStream is = StreetAddressGenerator.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is != null) {
            return StreetAddressResourceLoader.load(is, resourcePath);
        }
        return new String[] { "Apt", "Suite", "Unit", "Floor", "Room" };
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates address with short suffix form by default (Chance-style short_suffix=true).
     */
    @Override
    public String generate() {
        return generate(true);
    }

    /**
     * Generates a random street address of the form {@code "<number> <streetName> <streetType>"}.
     *
     * @param shortSuffix {@code true} for abbreviated suffixes ({@code "St"}, {@code "Ave"}),
     *                    {@code false} for full suffixes ({@code "Street"}, {@code "Avenue"})
     * @return a street-address string; never {@code null}
     */
    public String generate(boolean shortSuffix) {
        return generateStreetAddressNumber()
               + " "
               + generateStreetName()
               + " "
               + generateStreetSuffix(shortSuffix);
    }

    /**
     * Generates a locale-aware street name.
     *
     * @return street name
     */
    public String generateStreetName() {
        return streetNames[random.nextInt(streetNames.length)];
    }

    /**
     * Generates a street suffix using short form by default (for example, {@code St}, {@code Ave}).
     *
     * @return short street suffix
     */
    public String generateStreetSuffix() {
        return generateStreetSuffix(true);
    }

    /**
     * Generates a street suffix in short or long form.
     *
     * @param shortSuffix true for short form, false for long form
     * @return street suffix
     */
    public String generateStreetSuffix(boolean shortSuffix) {
        String[] types = shortSuffix ? streetTypesShort : streetTypesLong;
        return types[random.nextInt(types.length)];
    }

    /**
     * Generates a numeric street/building number in [1, 9999].
     *
     * @return building number as text
     */
    public String generateStreetAddressNumber() {
        return Integer.toString(random.nextInt(1, 10000));
    }

    /**
     * Alias of {@link #generateStreetAddressNumber()}.
     *
     * @return building number as text
     */
    public String generateBuildingNumber() {
        return generateStreetAddressNumber();
    }

    /**
     * Generates a secondary address component (for example, {@code Apt 12}, {@code Suite 201}).
     *
     * @return secondary address component
     */
    public String generateSecondaryAddress() {
        String unit = secondaryUnits[random.nextInt(secondaryUnits.length)];
        int number = random.nextInt(1, 1000);
        return unit + " " + number;
    }

    /**
     * Generates a full address string combining street, city, state, postal code and country
     * where locale providers are available.
     *
     * @return full address string
     */
    public String generateFullAddress() {
        StringBuilder sb = new StringBuilder(generate());

        if (cityGenerator != null) {
            sb.append(", ").append(cityGenerator.generate());
        }
        if (stateGenerator != null) {
            sb.append(", ").append(stateGenerator.generate(true));
        }
        sb.append(" ").append(postalCodeGenerator.generate());
        if (countryGenerator != null) {
            sb.append(", ").append(countryGenerator.generate());
        }
        return sb.toString();
    }

    /**
     * Returns configured locale.
     */
    public Locale getLocale() {
        return config.getLocale();
    }

    /**
     * Returns number of locale street names available to this generator instance.
     */
    public int getStreetNameCount() {
        return streetNames.length;
    }

    /**
     * Returns true when configured locale is registered.
     */
    public boolean isLocaleExplicitlySupported() {
        return config.getRegistryContext().isStreetAddressRegistered(config.getLocale());
    }
}
