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
    @DisplayName("descriptor creates only its declared provider type")
    void descriptorCreatesDeclaredType() {
        ProviderDescriptor<String> descriptor = descriptor("text", List.of());
        ProviderDescriptor<String> invalid = new ProviderDescriptor<>("invalid",
                                                                       String.class,
                                                                       config -> 42,
                                                                       List.of(),
                                                                       Set.of());

        assertEquals("text", descriptor.create(GeneratorConfig.defaults()));
        assertEquals(String.class, descriptor.getProviderType());
        assertThrows(ClassCastException.class, () -> invalid.create(GeneratorConfig.defaults()));
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
        return new ProviderDescriptor<>(key, String.class, config -> key, aliases, Set.of());
    }
}
