/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user.nationalid;

import java.util.Locale;
import java.util.Random;

/**
 * Generates Saudi national ID style identifiers.
 *
 * <p>Saudi citizen identifiers are represented as ten digits beginning with {@code 1}.
 */
public final class ArSaNationalIdProvider implements NationalIdProvider {

    @Override
    public Locale getLocale() {
        return Locale.of("ar", "SA");
    }

    @Override
    public String generate(Random random) {
        StringBuilder builder = new StringBuilder("1");
        for (int i = 0; i < 9; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }
}
