/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TypePredicates")
class TypePredicatesTest {

    @Test
    @DisplayName("inPackage matches package prefixes including sub-packages")
    void inPackageMatchesPrefix() {
        assertTrue(TypePredicates.inPackage("java.time").test(java.time.LocalDate.class));
        assertTrue(TypePredicates.inPackage("java").test(java.time.LocalDate.class));
        assertFalse(TypePredicates.inPackage("java.util").test(java.time.LocalDate.class));
    }

    @Test
    @DisplayName("inPackage returns false for primitive classes with no package")
    void inPackageFalseForPrimitiveTypes() {
        assertFalse(TypePredicates.inPackage("java.lang").test(int.class));
    }

    @Test
    @DisplayName("inPackage(null) throws NullPointerException")
    void inPackageNullThrows() {
        assertThrows(NullPointerException.class, () -> TypePredicates.inPackage(null));
    }

    @Test
    @DisplayName("inPackage(blank) throws IllegalArgumentException")
    void inPackageBlankThrows() {
        assertThrows(IllegalArgumentException.class, () -> TypePredicates.inPackage("   "));
    }
}
