/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;

/**
 * Contract for a locale-specific national identity number generator.
 *
 * <p>Implement this interface and register an instance on a configuration-scoped context with
 * {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder#registerNationalIdProvider(NationalIdProvider)}
 * to extend or override national ID generation for any locale — including locales not built into the
 * library.
 *
 * <pre>{@code
 * DataRegistryContext context = DataRegistryContext.builder()
 *     .registerNationalIdProvider(new NationalIdProvider() {
 *         public Locale getLocale() { return Locale.of("ko", "KR"); }
 *         public String generate(Random random) { return "..."; }
 *     })
 *     .build();
 * GeneratorConfig config = GeneratorConfig.builder()
 *     .locale(Locale.of("ko", "KR"))
 *     .nationalIdSafetyPolicy(NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED)
 *     .registryContext(context)
 *     .build();
 * NationalIdGenerator gen = new NationalIdGenerator(config);
 * }</pre>
 *
 * <p>Implementations must be stateless with respect to the generation logic; all randomness is
 * supplied via the {@link Random} argument so that seeding is controlled by the caller.
 */
public interface NationalIdProvider {

    /**
     * The locale this provider generates national IDs for.
     *
     * @return non-null locale
     */
    Locale getLocale();

    /**
     * Generates a single national ID string using the supplied random source.
     *
     * @param random the PRNG to use; must not be {@code null}
     * @return a valid-format national ID for this provider's locale
     */
    String generate(Random random);
}
