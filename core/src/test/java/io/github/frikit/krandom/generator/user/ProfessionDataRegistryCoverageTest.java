/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import java.util.Locale;
import org.junit.jupiter.api.DisplayName;

@DisplayName("ProfessionDataRegistry coverage")
class ProfessionDataRegistryCoverageTest {

    private static ProfessionDataProvider provider(Locale locale, String[] professions, int[] weights) {
        return new ProfessionDataProvider() {

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String[] getProfessions() {
                return professions;
            }

            @Override
            public int[] getWeights() {
                return weights;
            }
        };
    }
}
