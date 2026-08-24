/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.extension.KRandomModule;
import io.github.frikit.krandom.generator.extension.KRandomModuleContext;
import io.github.frikit.krandom.generator.provider.ProviderDescriptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("semantic extension provider resolution")
class BuiltInProviderResolverExtensionTest {

    @Test
    @DisplayName("extension aliases resolve and unknown names fail")
    void extensionAliasesAndUnknownNames() {
        GeneratorConfig config = GeneratorConfig.builder().install(new AliasModule()).build();

        Generator<?> generator = BuiltInProviderResolver.generator("extension_alias", config);

        assertEquals("extension", generator.generate());
        assertThrows(IllegalArgumentException.class,
                     () -> BuiltInProviderResolver.generator("missing.extension", config));
    }

    private static final class AliasModule implements KRandomModule {

        @Override
        public String id() {
            return "test.alias";
        }

        @Override
        public void configure(KRandomModuleContext context) {
            context.registerProvider(
                ProviderDescriptor.builder("extension.provider", FixedGenerator.class, config -> new FixedGenerator())
                                  .aliases("extension_alias")
                                  .build());
        }
    }

    private static final class FixedGenerator implements Generator<String> {

        @Override
        public String generate() {
            return "extension";
        }
    }
}
