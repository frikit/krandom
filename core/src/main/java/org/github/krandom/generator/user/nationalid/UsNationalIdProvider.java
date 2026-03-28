/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;

/**
 * Generates US Social Security Numbers (SSNs).
 *
 * <p>Generated numbers follow the SSN format {@code AAA-GG-SSSS} where:
 * <ul>
 *   <li>Area (AAA): 001–665 or 667–899 (666 is never issued)
 *   <li>Group (GG): 01–99
 *   <li>Serial (SSSS): 0001–9999
 * </ul>
 *
 * <p>Use the fluent methods {@link #withoutDashes()} and {@link #lastFourOnly()} to configure
 * the output format:
 *
 * <pre>{@code
 * NationalIdGenerator gen      = new NationalIdGenerator(Locale.US);
 * String full     = gen.generate();                                     // "411-90-0070"
 * String noDashes = new UsNationalIdProvider().withoutDashes().generate(random); // "411900070"
 * String lastFour = new UsNationalIdProvider().lastFourOnly().generate(random);  // "2938"
 * }</pre>
 */
public final class UsNationalIdProvider implements NationalIdProvider {

    private final boolean includeDashes;
    private final boolean lastFourOnly;

    /**
     * Creates a provider that generates full SSNs with dashes (e.g., {@code "411-90-0070"}).
     */
    public UsNationalIdProvider() {
        this(true, false);
    }

    private UsNationalIdProvider(boolean includeDashes, boolean lastFourOnly) {
        this.includeDashes = includeDashes;
        this.lastFourOnly = lastFourOnly;
    }

    /**
     * Returns a new provider that generates SSNs without dashes (e.g., {@code "411900070"}).
     *
     * @return a new provider with the same options except dashes are omitted
     */
    public UsNationalIdProvider withoutDashes() {
        return new UsNationalIdProvider(false, false);
    }

    /**
     * Returns a new provider that generates only the last 4 digits of an SSN
     * (e.g., {@code "2938"}).
     *
     * @return a new provider configured to emit last-four-only output
     */
    public UsNationalIdProvider lastFourOnly() {
        return new UsNationalIdProvider(false, true);
    }

    @Override
    public Locale getLocale() {
        return Locale.US;
    }

    @Override
    public String generate(Random random) {
        if (lastFourOnly) {
            return String.format("%04d", random.nextInt(9999) + 1);
        }
        int area = random.nextInt(898) + 1;
        if (area >= 666) area++;
        int group = random.nextInt(99) + 1;
        int serial = random.nextInt(9999) + 1;
        if (includeDashes) {
            return String.format("%03d-%02d-%04d", area, group, serial);
        }
        return String.format("%03d%02d%04d", area, group, serial);
    }
}
