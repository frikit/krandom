/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FieldGeneratorResolver semantic aliases")
class FieldGeneratorResolverSemanticAliasTest {

    @Test
    @DisplayName("normalization strips punctuation and casing for semantic lookup")
    void normalizationStripsPunctuationAndCasing() {
        assertEquals("createdat", FieldGeneratorResolver.normalizeSemanticFieldName("Created_At"));
        assertEquals("accountid", FieldGeneratorResolver.normalizeSemanticFieldName("account-id"));
        assertEquals("isenabled", FieldGeneratorResolver.normalizeSemanticFieldName("isEnabled"));
    }

    @Test
    @DisplayName("alias lookup maps common business names to canonical semantic keys")
    void aliasLookupMapsBusinessNamesToCanonicalSemanticKeys() {
        assertEquals("createdat", FieldGeneratorResolver.semanticKeyForFieldName("created_at"));
        assertEquals("updatedat", FieldGeneratorResolver.semanticKeyForFieldName("updatedTimestamp"));
        assertEquals("birthdate", FieldGeneratorResolver.semanticKeyForFieldName("dateOfBirth"));
        assertEquals("id", FieldGeneratorResolver.semanticKeyForFieldName("accountId"));
        assertEquals("active", FieldGeneratorResolver.semanticKeyForFieldName("is_enabled"));
        assertEquals("latitude", FieldGeneratorResolver.semanticKeyForFieldName("lat"));
        assertEquals("longitude", FieldGeneratorResolver.semanticKeyForFieldName("lon"));
    }

    @Test
    @DisplayName("canonical semantic keys expose their registered aliases")
    void canonicalSemanticKeysExposeAliases() {
        assertTrue(FieldGeneratorResolver.semanticAliasesFor("id").containsAll(Set.of("id", "userid", "accountid")));
        assertTrue(FieldGeneratorResolver.semanticAliasesFor("createdAt").containsAll(Set.of("createdat", "createdtimestamp")));
        assertTrue(FieldGeneratorResolver.semanticAliasesFor("latitude").containsAll(Set.of("latitude", "lat")));
    }
}
