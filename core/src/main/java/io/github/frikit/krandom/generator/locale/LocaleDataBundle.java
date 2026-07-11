/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.locale;

import io.github.frikit.krandom.generator.DataRegistryContext;
import io.github.frikit.krandom.generator.location.CityDataProvider;
import io.github.frikit.krandom.generator.location.CityDataRegistry;
import io.github.frikit.krandom.generator.location.CountryDataProvider;
import io.github.frikit.krandom.generator.location.CountryDataRegistry;
import io.github.frikit.krandom.generator.location.StateDataProvider;
import io.github.frikit.krandom.generator.location.StateDataRegistry;
import io.github.frikit.krandom.generator.location.StreetAddressDataProvider;
import io.github.frikit.krandom.generator.location.StreetAddressDataRegistry;
import io.github.frikit.krandom.generator.user.FirstNameDataProvider;
import io.github.frikit.krandom.generator.user.FirstNameDataRegistry;
import io.github.frikit.krandom.generator.user.GenderDataProvider;
import io.github.frikit.krandom.generator.user.GenderDataRegistry;
import io.github.frikit.krandom.generator.user.LastNameDataProvider;
import io.github.frikit.krandom.generator.user.LastNameDataRegistry;
import io.github.frikit.krandom.generator.user.ProfessionDataProvider;
import io.github.frikit.krandom.generator.user.ProfessionDataRegistry;
import io.github.frikit.krandom.generator.user.SuffixDataProvider;
import io.github.frikit.krandom.generator.user.SuffixDataRegistry;
import io.github.frikit.krandom.generator.user.TitleDataProvider;
import io.github.frikit.krandom.generator.user.TitleDataRegistry;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdProvider;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdRegistry;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * Immutable application-level locale pack for registering multiple locale datasets in one step.
 *
 * <p>Use this when you want to add or override a locale without hand-registering every provider:
 *
 * <pre>{@code
 * LocaleDataBundle bundle = LocaleDataBundle.builder(Locale.of("es", "MX"))
 *     .firstNames(new String[]{"Mateo"}, new String[]{"Sofía"})
 *     .lastNames("Hernández", "Ramírez")
 *     .cities("Ciudad de México", "Guadalajara")
 *     .states(new String[]{"Jalisco", "Ciudad de México"}, new String[]{"JAL", "CDMX"})
 *     .countries("México")
 *     .streetAddress(new String[]{"Juárez"}, new String[]{"Av"}, new String[]{"Avenida"})
 *     .build();
 *
 * DataRegistryContext context = DataRegistryContext.builder()
 *     .isolated()
 *     .registerLocaleData(bundle)
 *     .build();
 * }</pre>
 */
public final class LocaleDataBundle {

    private final Locale           locale;
    private final String[]         maleFirstNames;
    private final String[]         femaleFirstNames;
    private final String[]         lastNames;
    private final String           maleLabel;
    private final String           femaleLabel;
    private final String[]         titles;
    private final String[]         suffixes;
    private final String[]         professions;
    private final int[]            professionWeights;
    private final String[]         cities;
    private final String[]         states;
    private final String[]         stateAbbreviations;
    private final String[]         countries;
    private final String[]         streetNames;
    private final String[]         streetTypesShort;
    private final String[]         streetTypesLong;
    private final NationalIdProvider nationalIdProvider;

    private LocaleDataBundle(Builder builder) {
        this.locale = builder.locale;
        this.maleFirstNames = cloneOrNull(builder.maleFirstNames);
        this.femaleFirstNames = cloneOrNull(builder.femaleFirstNames);
        this.lastNames = cloneOrNull(builder.lastNames);
        this.maleLabel = builder.maleLabel;
        this.femaleLabel = builder.femaleLabel;
        this.titles = cloneOrNull(builder.titles);
        this.suffixes = cloneOrNull(builder.suffixes);
        this.professions = cloneOrNull(builder.professions);
        this.professionWeights = builder.professionWeights == null ? null : builder.professionWeights.clone();
        this.cities = cloneOrNull(builder.cities);
        this.states = cloneOrNull(builder.states);
        this.stateAbbreviations = cloneOrNull(builder.stateAbbreviations);
        this.countries = cloneOrNull(builder.countries);
        this.streetNames = cloneOrNull(builder.streetNames);
        this.streetTypesShort = cloneOrNull(builder.streetTypesShort);
        this.streetTypesLong = cloneOrNull(builder.streetTypesLong);
        this.nationalIdProvider = builder.nationalIdProvider;
    }

    /**
     * Creates a builder for a specific locale.
     */
    public static Builder builder(Locale locale) {
        return new Builder(locale);
    }

    /**
     * Returns the bundle locale.
     */
    public Locale locale() {
        return locale;
    }

    /**
     * Applies the bundle to a config-scoped registry context builder.
     */
    public DataRegistryContext.Builder applyTo(DataRegistryContext.Builder builder) {
        Objects.requireNonNull(builder, "builder must not be null");
        if (hasFirstNames()) {
            builder.registerFirstNameProvider(firstNameProvider());
        }
        if (hasLastNames()) {
            builder.registerLastNameProvider(lastNameProvider());
        }
        if (hasGenderLabels()) {
            builder.registerGenderProvider(genderProvider());
        }
        if (hasTitles()) {
            builder.registerTitleProvider(titleProvider());
        }
        if (hasSuffixes()) {
            builder.registerSuffixProvider(suffixProvider());
        }
        if (hasProfessions()) {
            builder.registerProfessionProvider(professionProvider());
        }
        if (hasCities()) {
            builder.registerCityProvider(cityProvider());
        }
        if (hasStates()) {
            builder.registerStateProvider(stateProvider());
        }
        if (hasCountries()) {
            builder.registerCountryProvider(countryProvider());
        }
        if (hasStreetAddress()) {
            builder.registerStreetAddressProvider(streetAddressProvider());
        }
        if (nationalIdProvider != null) {
            builder.registerNationalIdProvider(nationalIdProvider);
        }
        return builder;
    }


    private boolean hasFirstNames() {
        return maleFirstNames != null;
    }

    private boolean hasLastNames() {
        return lastNames != null;
    }

    private boolean hasGenderLabels() {
        return maleLabel != null;
    }

    private boolean hasTitles() {
        return titles != null;
    }

    private boolean hasSuffixes() {
        return suffixes != null;
    }

    private boolean hasProfessions() {
        return professions != null;
    }

    private boolean hasCities() {
        return cities != null;
    }

    private boolean hasStates() {
        return states != null;
    }

    private boolean hasCountries() {
        return countries != null;
    }

    private boolean hasStreetAddress() {
        return streetNames != null;
    }

    private FirstNameDataProvider firstNameProvider() {
        return new FirstNameDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getMaleFirstNames() {
                return maleFirstNames.clone();
            }

            @Override
            public String[] getFemaleFirstNames() {
                return femaleFirstNames.clone();
            }
        };
    }

    private LastNameDataProvider lastNameProvider() {
        return new LastNameDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getLastNames() {
                return lastNames.clone();
            }
        };
    }

    private GenderDataProvider genderProvider() {
        return new GenderDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String getMaleLabel() {
                return maleLabel;
            }

            @Override
            public String getFemaleLabel() {
                return femaleLabel;
            }
        };
    }

    private TitleDataProvider titleProvider() {
        return new TitleDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getTitles() {
                return titles.clone();
            }
        };
    }

    private SuffixDataProvider suffixProvider() {
        return new SuffixDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getSuffixes() {
                return suffixes.clone();
            }
        };
    }

    private ProfessionDataProvider professionProvider() {
        return new ProfessionDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getProfessions() {
                return professions.clone();
            }

            @Override
            public int[] getWeights() {
                return professionWeights.clone();
            }
        };
    }

    private CityDataProvider cityProvider() {
        return new CityDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getCities() {
                return cities.clone();
            }
        };
    }

    private StateDataProvider stateProvider() {
        return new StateDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getStates() {
                return states.clone();
            }

            @Override
            public String[] getAbbreviations() {
                return stateAbbreviations.clone();
            }
        };
    }

    private CountryDataProvider countryProvider() {
        return new CountryDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getCountries() {
                return countries.clone();
            }
        };
    }

    private StreetAddressDataProvider streetAddressProvider() {
        return new StreetAddressDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getStreetNames() {
                return streetNames.clone();
            }

            @Override
            public String[] getStreetTypesShort() {
                return streetTypesShort.clone();
            }

            @Override
            public String[] getStreetTypesLong() {
                return streetTypesLong.clone();
            }
        };
    }

    private static String[] cloneOrNull(String[] values) {
        return values == null ? null : values.clone();
    }

    /**
     * Builder for {@link LocaleDataBundle}.
     */
    public static final class Builder {

        private final Locale locale;

        private String[]         maleFirstNames;
        private String[]         femaleFirstNames;
        private String[]         lastNames;
        private String           maleLabel;
        private String           femaleLabel;
        private String[]         titles;
        private String[]         suffixes;
        private String[]         professions;
        private int[]            professionWeights;
        private String[]         cities;
        private String[]         states;
        private String[]         stateAbbreviations;
        private String[]         countries;
        private String[]         streetNames;
        private String[]         streetTypesShort;
        private String[]         streetTypesLong;
        private NationalIdProvider nationalIdProvider;

        private Builder(Locale locale) {
            this.locale = Objects.requireNonNull(locale, "locale must not be null");
        }

        public Builder firstNames(String[] maleFirstNames, String[] femaleFirstNames) {
            validateArray("maleFirstNames", maleFirstNames);
            validateArray("femaleFirstNames", femaleFirstNames);
            this.maleFirstNames = maleFirstNames.clone();
            this.femaleFirstNames = femaleFirstNames.clone();
            return this;
        }

        public Builder lastNames(String... lastNames) {
            validateArray("lastNames", lastNames);
            this.lastNames = lastNames.clone();
            return this;
        }

        public Builder genderLabels(String maleLabel, String femaleLabel) {
            validateLabel("maleLabel", maleLabel);
            validateLabel("femaleLabel", femaleLabel);
            this.maleLabel = maleLabel;
            this.femaleLabel = femaleLabel;
            return this;
        }

        public Builder titles(String... titles) {
            validateArray("titles", titles);
            this.titles = titles.clone();
            return this;
        }

        public Builder suffixes(String... suffixes) {
            validateArray("suffixes", suffixes);
            this.suffixes = suffixes.clone();
            return this;
        }

        public Builder professions(String... professions) {
            Objects.requireNonNull(professions, "professions must not be null");
            int[] weights = new int[professions.length];
            Arrays.fill(weights, 1);
            return professions(professions, weights);
        }

        public Builder professions(String[] professions, int[] weights) {
            validateWeightedArray(professions, weights);
            this.professions = professions.clone();
            this.professionWeights = weights.clone();
            return this;
        }

        public Builder cities(String... cities) {
            validateArray("cities", cities);
            this.cities = cities.clone();
            return this;
        }

        public Builder states(String... states) {
            validateArray("states", states);
            this.states = states.clone();
            this.stateAbbreviations = new String[0];
            return this;
        }

        public Builder states(String[] states, String[] abbreviations) {
            validateArray("states", states);
            Objects.requireNonNull(abbreviations, "abbreviations must not be null");
            validateOptionalAlignedArray("abbreviations", states, abbreviations);
            this.states = states.clone();
            this.stateAbbreviations = abbreviations.clone();
            return this;
        }

        public Builder countries(String... countries) {
            validateArray("countries", countries);
            this.countries = countries.clone();
            return this;
        }

        public Builder streetAddress(String[] streetNames, String[] streetTypesShort, String[] streetTypesLong) {
            validateArray("streetNames", streetNames);
            validateArray("streetTypesShort", streetTypesShort);
            validateArray("streetTypesLong", streetTypesLong);
            this.streetNames = streetNames.clone();
            this.streetTypesShort = streetTypesShort.clone();
            this.streetTypesLong = streetTypesLong.clone();
            return this;
        }

        public Builder nationalIdProvider(NationalIdProvider nationalIdProvider) {
            this.nationalIdProvider = Objects.requireNonNull(nationalIdProvider, "nationalIdProvider must not be null");
            if (!locale.equals(nationalIdProvider.getLocale())) {
                throw new IllegalArgumentException("nationalIdProvider locale must match bundle locale");
            }
            return this;
        }

        public LocaleDataBundle build() {
            return new LocaleDataBundle(this);
        }

        private static void validateArray(String name, String[] values) {
            Objects.requireNonNull(values, name + " must not be null");
            if (values.length == 0) {
                throw new IllegalArgumentException(name + " must not be empty");
            }
            for (int i = 0; i < values.length; i++) {
                if (values[i] == null || values[i].isBlank()) {
                    throw new IllegalArgumentException(name + " at index " + i + " must not be blank");
                }
            }
        }

        private static void validateLabel(String name, String value) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(name + " must not be blank");
            }
        }

        private static void validateWeightedArray(String[] values, int[] weights) {
            validateArray("professions", values);
            Objects.requireNonNull(weights, "weights must not be null");
            if (values.length != weights.length) {
                throw new IllegalArgumentException("professions and weights length must match");
            }
            for (int i = 0; i < weights.length; i++) {
                if (weights[i] <= 0) {
                    throw new IllegalArgumentException("weight at index " + i + " must be > 0");
                }
            }
        }

        private static void validateOptionalAlignedArray(String name, String[] values, String[] optionalValues) {
            if (optionalValues.length != 0 && optionalValues.length != values.length) {
                throw new IllegalArgumentException(name + " must be empty or match the states length");
            }
            for (int i = 0; i < optionalValues.length; i++) {
                if (optionalValues[i] == null) {
                    throw new IllegalArgumentException(name + " at index " + i + " must not be null");
                }
            }
        }
    }
}
