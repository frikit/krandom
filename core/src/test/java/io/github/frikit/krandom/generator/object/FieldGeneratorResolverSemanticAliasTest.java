/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        assertEquals("industry", FieldGeneratorResolver.semanticKeyForFieldName("sector"));
        assertEquals("companyemail", FieldGeneratorResolver.semanticKeyForFieldName("companyEmail"));
        assertEquals("companyurl", FieldGeneratorResolver.semanticKeyForFieldName("companyWebsite"));
    }

    @Test
    @DisplayName("canonical semantic keys expose their registered aliases")
    void canonicalSemanticKeysExposeAliases() {
        assertTrue(FieldGeneratorResolver.semanticAliasesFor("id").containsAll(Set.of("id", "userid", "accountid")));
        assertTrue(FieldGeneratorResolver.semanticAliasesFor("createdAt").containsAll(Set.of("createdat", "createdtimestamp")));
        assertTrue(FieldGeneratorResolver.semanticAliasesFor("latitude").containsAll(Set.of("latitude", "lat")));
        assertTrue(FieldGeneratorResolver.semanticAliasesFor("companyEmail").containsAll(Set.of("companyemail", "businessemail")));
    }

    @Test
    @DisplayName("provider-backed semantic keys expose their provider mapping")
    void providerBackedSemanticKeysExposeProviderMapping() {
        assertEquals("person.first_name", FieldGeneratorResolver.semanticProviderNameFor("firstName"));
        assertEquals("address.city", FieldGeneratorResolver.semanticProviderNameFor("city"));
        assertEquals("company.name", FieldGeneratorResolver.semanticProviderNameFor("companyName"));
        assertEquals("company.industry", FieldGeneratorResolver.semanticProviderNameFor("industry"));
        assertEquals("company.email", FieldGeneratorResolver.semanticProviderNameFor("companyEmail"));
        assertEquals("company.url", FieldGeneratorResolver.semanticProviderNameFor("companyWebsite"));
        assertEquals("security.password", FieldGeneratorResolver.semanticProviderNameFor("password"));
        assertEquals("internet.url", FieldGeneratorResolver.semanticProviderNameFor("url"));
        assertEquals("finance.currency", FieldGeneratorResolver.semanticProviderNameFor("currencyCode"));
        assertEquals("code.uuid", FieldGeneratorResolver.semanticProviderNameFor("uuid"));
    }

    @Test
    @DisplayName("custom registry maps project aliases to built-in semantics")
    void customRegistryMapsProjectAliasesToBuiltInSemantics() {
        SemanticFieldRegistry registry = SemanticFieldRegistry.defaults().toBuilder()
                                                              .alias("email", "contactMail")
                                                              .build();

        assertEquals("email", registry.semanticKeyForFieldName("contact_mail"));
        assertTrue(registry.semanticAliasesFor("email").contains("contactmail"));
    }

    @Test
    @DisplayName("custom registry drives object semantic generation")
    void customRegistryDrivesObjectSemanticGeneration() {
        SemanticFieldRegistry registry = SemanticFieldRegistry.defaults().toBuilder()
                                                              .alias("email", "contactMail")
                                                              .build();
        GeneratorConfig config = GeneratorConfig.builder()
                                                .seed(7L)
                                                .objectSemanticRegistry(registry)
                                                .build();

        ContactAliasHolder holder = new ObjectGenerator<>(ContactAliasHolder.class, config).generate();

        assertTrue(holder.contactMail.contains("@"));
    }

    @Test
    @DisplayName("registry builder validates aliases and provider names")
    void registryBuilderValidatesAliasesAndProviderNames() {
        assertThrows(IllegalArgumentException.class,
                     () -> SemanticFieldRegistry.builder().alias("email"));
        assertThrows(IllegalArgumentException.class,
                     () -> SemanticFieldRegistry.builder().alias("___", "mail"));
        assertThrows(IllegalArgumentException.class,
                     () -> SemanticFieldRegistry.builder().provider("email", " "));
    }

    static final class ContactAliasHolder {

        String contactMail;
    }
}
