/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.provider;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ProviderCatalog")
class ProviderCatalogTest {

    @Test
    @DisplayName("built-in catalog completely defines hub providers and aliases")
    void builtInsDefineHubProvidersAndAliases() {
        ProviderHub hub = new ProviderHub(GeneratorConfig.defaults());
        Map<String, String> aliases = new LinkedHashMap<>();

        for (ProviderDescriptor<?> descriptor : ProviderCatalog.builtIns()) {
            for (String alias : descriptor.getAliases()) {
                aliases.put(alias, descriptor.getKey());
            }
        }

        assertEquals(ProviderCatalog.builtIns().stream().map(ProviderDescriptor::getKey).collect(java.util.stream.Collectors.toSet()),
                     hub.providerNames());
        assertEquals(aliases, hub.aliases());
    }

    @Test
    @DisplayName("catalog rejects canonical and alias collisions")
    void catalogRejectsNameCollisions() {
        ProviderDescriptor<String> alpha = descriptor("alpha", List.of("shared"));
        ProviderDescriptor<String> duplicateKey = descriptor("alpha", List.of());
        ProviderDescriptor<String> duplicateAlias = descriptor("beta", List.of("shared"));

        assertThrows(IllegalArgumentException.class, () -> ProviderCatalog.validate(List.of(alpha, duplicateKey)));
        assertThrows(IllegalArgumentException.class, () -> ProviderCatalog.validate(List.of(alpha, duplicateAlias)));
    }

    @Test
    @DisplayName("catalog rejects schema-reference and schema-alias collisions")
    void catalogRejectsSchemaCollisions() {
        ProviderDescriptor<String> alpha = descriptor("alpha", List.of())
            .withSchemaProjections(List.of(projection("schema.alpha", List.of("shared"))));
        ProviderDescriptor<String> duplicateReference = descriptor("beta", List.of())
            .withSchemaProjections(List.of(projection("schema.alpha", List.of())));
        ProviderDescriptor<String> duplicateAlias = descriptor("gamma", List.of())
            .withSchemaProjections(List.of(projection("schema.gamma", List.of("shared"))));

        assertThrows(IllegalArgumentException.class,
                     () -> ProviderCatalog.validateSchema(List.of(alpha, duplicateReference)));
        assertThrows(IllegalArgumentException.class,
                     () -> ProviderCatalog.validateSchema(List.of(alpha, duplicateAlias)));
    }

    @Test
    @DisplayName("descriptor creates only its declared provider type")
    void descriptorCreatesDeclaredType() {
        ProviderDescriptor<String> descriptor = descriptor("text", List.of());
        ProviderDescriptor<String> invalid = new ProviderDescriptor<>("invalid",
                                                                       String.class,
                                                                       config -> 42,
                                                                       List.of(),
                                                                       Set.of(),
                                                                       List.of());

        assertEquals("text", descriptor.create(GeneratorConfig.defaults()));
        assertEquals(String.class, descriptor.getProviderType());
        assertThrows(ClassCastException.class, () -> invalid.create(GeneratorConfig.defaults()));
    }

    @Test
    @DisplayName("sensitive built-in providers expose configuration-aware safety metadata")
    void sensitiveProvidersExposeSafetyMetadata() {
        ProviderDescriptor<?> cards = descriptorFor("finance.credit_card");
        ProviderDescriptor<?> cardInfo = descriptorFor("finance.credit_card_info");
        ProviderDescriptor<?> phones = descriptorFor("address.phone_number");

        assertEquals(ProviderValidity.GUARANTEED, cards.getSafetyMetadata().formatValidity());
        assertEquals(ProviderValidity.CONFIGURATION_DEPENDENT, cards.getSafetyMetadata().checksumValidity());
        assertEquals(ProviderValidity.GUARANTEED, cards.getSafetyMetadata().semanticPlausibility());
        assertEquals(ProviderTestSafety.CONFIGURATION_DEPENDENT, cards.getSafetyMetadata().testSafety());
        assertEquals(cards.getSafetyMetadata(), cardInfo.getSafetyMetadata());
        assertEquals(ProviderValidity.GUARANTEED, phones.getSafetyMetadata().formatValidity());
        assertEquals(ProviderValidity.NOT_APPLICABLE, phones.getSafetyMetadata().checksumValidity());
        assertEquals(ProviderValidity.CONFIGURATION_DEPENDENT, phones.getSafetyMetadata().semanticPlausibility());
        assertEquals(ProviderTestSafety.CONFIGURATION_DEPENDENT, phones.getSafetyMetadata().testSafety());
    }

    @Test
    @DisplayName("unclassified descriptors make no validity or test-safety claim")
    void unclassifiedDescriptorsMakeNoSafetyClaim() {
        ProviderSafetyMetadata metadata = descriptor("text", List.of()).getSafetyMetadata();

        assertEquals(ProviderValidity.UNCLASSIFIED, metadata.formatValidity());
        assertEquals(ProviderValidity.UNCLASSIFIED, metadata.checksumValidity());
        assertEquals(ProviderValidity.UNCLASSIFIED, metadata.semanticPlausibility());
        assertEquals(ProviderTestSafety.UNCLASSIFIED, metadata.testSafety());
    }

    @Test
    @DisplayName("utility catalog cannot be instantiated")
    void utilityCatalogCannotBeInstantiated() throws ReflectiveOperationException {
        Constructor<ProviderCatalog> constructor = ProviderCatalog.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);

        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
    }

    private static ProviderDescriptor<String> descriptor(String key, List<String> aliases) {
        return new ProviderDescriptor<>(key, String.class, config -> key, aliases, Set.of(), List.of());
    }

    private static ProviderSchemaProjection<String> projection(String reference, List<String> aliases) {
        return new ProviderSchemaProjection<>(reference,
                                              aliases,
                                              (provider, config) -> provider,
                                              false,
                                              null,
                                              null,
                                              Set.of());
    }

    private static ProviderDescriptor<?> descriptorFor(String key) {
        return ProviderCatalog.schemaBuiltIns().stream()
                              .filter(descriptor -> descriptor.getKey().equals(key))
                              .findFirst()
                              .orElseThrow();
    }
}
