/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("UniversityData")
class UniversityDataTest {

    @Test
    @DisplayName("requires every university field to be non-blank")
    void requiresEveryUniversityFieldToBeNonBlank() {
        assertThrows(NullPointerException.class,
                     () -> new UniversityData(null, "BSc", "School", "University", "Northbridge"));
        assertThrows(IllegalArgumentException.class,
                     () -> new UniversityData("Northbridge", " ", "School", "University", "Northbridge"));
        assertThrows(IllegalArgumentException.class,
                     () -> new UniversityData("Northbridge", "BSc", "", "University", "Northbridge"));
        assertThrows(IllegalArgumentException.class,
                     () -> new UniversityData("Northbridge", "BSc", "School", "\t", "Northbridge"));
        assertThrows(IllegalArgumentException.class,
                     () -> new UniversityData("Northbridge", "BSc", "School", "University", "\n"));
    }
}
