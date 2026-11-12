/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.internal;

import io.github.frikit.krandom.generator.ChildStreamPolicy;
import io.github.frikit.krandom.generator.GenerationRecipe;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.datetime.DateGenerator;
import io.github.frikit.krandom.generator.finance.CurrencyGenerator;
import io.github.frikit.krandom.generator.finance.MoneyGenerator;
import io.github.frikit.krandom.generator.identifier.UUIDGenerator;
import io.github.frikit.krandom.generator.location.CityGenerator;
import io.github.frikit.krandom.generator.location.CountryGenerator;
import io.github.frikit.krandom.generator.location.PhoneNumberGenerator;
import io.github.frikit.krandom.generator.location.PostalCodeGenerator;
import io.github.frikit.krandom.generator.location.StateGenerator;
import io.github.frikit.krandom.generator.location.StreetAddressGenerator;
import io.github.frikit.krandom.generator.text.SentenceGenerator;
import io.github.frikit.krandom.generator.text.WordGenerator;
import io.github.frikit.krandom.generator.user.EmailGenerator;
import io.github.frikit.krandom.generator.user.FirstNameGenerator;
import io.github.frikit.krandom.generator.user.FullNameGenerator;
import io.github.frikit.krandom.generator.user.LastNameGenerator;
import java.util.Set;

/**
 * The generators whose {@code reseed(seed)} leaves them as a new one built from their configuration seeded
 * {@code seed} would be, so that a Schema can build one for each column and reseed it for each value rather
 * than build one for each value.
 *
 * <p>This package is not exported, so this is not public API. A class joins the set only with a test that
 * compares its reseeded output with a new build's for every public method without arguments.
 */
public final class Reseeding {

    private static final Set<Class<?>> RESEEDS_AS_BUILT = Set.of(
            UUIDGenerator.class,
            FullNameGenerator.class,
            FirstNameGenerator.class,
            LastNameGenerator.class,
            EmailGenerator.class,
            PhoneNumberGenerator.class,
            CurrencyGenerator.class,
            MoneyGenerator.class,
            StreetAddressGenerator.class,
            CityGenerator.class,
            StateGenerator.class,
            PostalCodeGenerator.class,
            CountryGenerator.class,
            DateGenerator.class,
            SentenceGenerator.class,
            WordGenerator.class);

    private Reseeding() {}

    /**
     * Whether a provider resets as a new build does.
     *
     * @param provider provider to check
     * @return whether its class is in the set
     */
    public static boolean reseedsAsBuilt(Object provider) {
        return RESEEDS_AS_BUILT.contains(provider.getClass());
    }

    /** The classes in the set, for the test that checks each of them. */
    static Set<Class<?>> classes() {
        return RESEEDS_AS_BUILT;
    }

    /**
     * The seed of the child stream {@code streamName} of a configuration like {@code config} seeded {@code seed}:
     * what {@code forChildStream} gives the child, its parent's seed under {@link ChildStreamPolicy#LEGACY}.
     *
     * @param config     configuration whose child-stream policy applies
     * @param seed       seed of the parent
     * @param streamName name of the child stream
     * @return the child's seed
     */
    public static long childSeed(GeneratorConfig config, long seed, String streamName) {
        return config.getChildStreamPolicy() == ChildStreamPolicy.LEGACY
                ? seed
                : GenerationRecipe.deriveChildSeed(seed, streamName);
    }
}
