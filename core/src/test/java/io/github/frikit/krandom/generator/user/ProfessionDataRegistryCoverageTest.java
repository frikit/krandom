/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
