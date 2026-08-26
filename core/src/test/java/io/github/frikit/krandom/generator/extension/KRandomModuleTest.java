/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.extension;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.object.ObjectGenerationSemanticMode;
import io.github.frikit.krandom.generator.provider.ProviderDescriptor;
import io.github.frikit.krandom.generator.provider.ProviderHub;
import io.github.frikit.krandom.generator.provider.ProviderSchemaProjection;
import io.github.frikit.krandom.generator.provider.ProviderSafetyMetadata;
import io.github.frikit.krandom.generator.provider.ProviderTestSafety;
import io.github.frikit.krandom.generator.provider.ProviderValidity;
import io.github.frikit.krandom.generator.schema.FieldLookup;
import io.github.frikit.krandom.generator.schema.SchemaContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("KRandom modules")
class KRandomModuleTest {

    @Test
    @DisplayName("one config-scoped descriptor powers provider, schema, and semantic APIs")
    void descriptorMetadataIsAppliedAcrossPublicApis() {
        GeneratorConfig config = GeneratorConfig.builder().locale(Locale.UK).install(new TrackingModule()).build();

        ProviderHub hub = new ProviderHub(config);
        FieldLookup fields = new FieldLookup(config);
        Shipment fixture = Generators.ofObject(Shipment.class, config).generate();

        assertEquals("TRK-GB", hub.get("tracking", Generator.class).generate());
        assertEquals("TRK-GB",
                     fields.resolve("shipping.tracking_code")
                           .generate(new SchemaContext(Locale.UK, new Random(1L), 0)));
        assertEquals("string", fields.resolve("tracking_code").jsonSchema().get("type"));
        assertEquals("NON_ROUTABLE",
                     ((Map<?, ?>) fields.resolve("tracking_code").jsonSchema().get("x-krandom-safety"))
                         .get("testSafety"));
        assertEquals("TRK-GB", fixture.trackingCode);
        assertEquals(List.of("example.shipping"), config.getExtensionRegistry().getModuleIds());
    }

    @Test
    @DisplayName("module contributions remain isolated to the config that installed them")
    void modulesAreConfigurationScoped() {
        GeneratorConfig configured = GeneratorConfig.builder().install(new TrackingModule()).build();
        GeneratorConfig defaults = GeneratorConfig.defaults();

        assertTrue(new ProviderHub(configured).has("tracking"));
        assertFalse(new ProviderHub(defaults).has("tracking"));
        assertTrue(new FieldLookup(configured).has("tracking_code"));
        assertFalse(new FieldLookup(defaults).has("tracking_code"));
    }

    @Test
    @DisplayName("installed modules survive toBuilder without sharing mutable registry state")
    void modulesSurviveConfigCopying() {
        GeneratorConfig original = GeneratorConfig.builder().install(new TrackingModule()).build();
        GeneratorConfig copy = original.toBuilder().locale(Locale.CANADA_FRENCH).build();

        assertEquals(List.of("example.shipping"), copy.getExtensionRegistry().getModuleIds());
        assertEquals("TRK-CA", new ProviderHub(copy).get("tracking", Generator.class).generate());
        assertThrows(UnsupportedOperationException.class,
                     () -> copy.getExtensionRegistry().getModuleIds().add("other"));
    }

    @Test
    @DisplayName("duplicate module, provider, alias, and schema names fail during config build")
    void conflictsFailFast() {
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder()
                                          .install(new TrackingModule())
                                          .install(new TrackingModule()));
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().install(module("duplicate.builtin", "person.email",
                                                                    "custom.email")).build());
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().install(module("duplicate.alias", "custom.person", "email"))
                                          .build());
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().install(module("duplicate.schema", "custom.schema",
                                                                    "person.full_name")).build());
    }

    private static KRandomModule module(String id, String providerName, String schemaReference) {
        return new KRandomModule() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public void configure(KRandomModuleContext context) {
                context.registerProvider(
                    ProviderDescriptor.builder(providerName, FixedTrackingGenerator.class, FixedTrackingGenerator::new)
                                      .schemaProjection(ProviderSchemaProjection
                                          .builder(schemaReference,
                                                   (FixedTrackingGenerator provider, GeneratorConfig config) ->
                                                       provider.generate())
                                          .build())
                                          .build());
            }
        };
    }

    @Test
    @DisplayName("registry resolution validates module identity independently of GeneratorConfig")
    void registryResolutionValidatesModuleIdentity() {
        KRandomModule first = module("duplicate", "custom.first", "custom.first");
        KRandomModule second = module("duplicate", "custom.second", "custom.second");

        assertThrows(IllegalArgumentException.class,
                     () -> KRandomExtensionRegistry.resolve(List.of(first, second)));
        assertThrows(IllegalArgumentException.class,
                     () -> KRandomExtensionRegistry.resolve(List.of(module(" ", "custom.bad", "custom.bad"))));
        assertThrows(IllegalArgumentException.class,
                     () -> KRandomExtensionRegistry.resolve(List.of(module("bad\nid", "custom.bad", "custom.bad"))));
        assertThrows(IllegalArgumentException.class,
                     () -> KRandomExtensionRegistry.resolve(List.of(module("bad\rid", "custom.bad", "custom.bad"))));
    }

    @Test
    @DisplayName("empty extension registries use one immutable value")
    void emptyRegistryValue() {
        KRandomExtensionRegistry first = KRandomExtensionRegistry.resolve(List.of());
        KRandomExtensionRegistry second = KRandomExtensionRegistry.resolve(List.of());

        assertSame(first, second);
    }

    @Test
    @DisplayName("semantic contributions reject missing and ambiguous vocabulary")
    void semanticContributionsAreValidated() {
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().install(semanticModule("blank.key", "", "field")).build());
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().install(semanticModule("blank.field", "custom", " ")).build());
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().install(new KRandomModule() {
                         @Override
                         public String id() {
                             return "empty.aliases";
                         }

                         @Override
                         public void configure(KRandomModuleContext context) {
                             context.registerSemanticAliases("custom");
                         }
                     }).build());
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().install(new KRandomModule() {
                         @Override
                         public String id() {
                             return "provider.semantic.conflict";
                         }

                         @Override
                         public void configure(KRandomModuleContext context) {
                             context.registerProvider(
                                 ProviderDescriptor.builder("custom.semantic", FixedTrackingGenerator.class,
                                                            FixedTrackingGenerator::new)
                                                   .semanticKeys("email")
                                                   .build());
                         }
                     }).build());
        GeneratorConfig.builder().install(new KRandomModule() {
            @Override
            public String id() {
                return "duplicate.same.semantic.alias";
            }

            @Override
            public void configure(KRandomModuleContext context) {
                context.registerSemanticAliases("custom", "shared");
                context.registerSemanticAliases("custom", "shared");
            }
        }).build();
        GeneratorConfig.builder().install(new KRandomModule() {
            @Override
            public String id() {
                return "distinct.semantic.aliases";
            }

            @Override
            public void configure(KRandomModuleContext context) {
                context.registerSemanticAliases("first", "firstAlias");
                context.registerSemanticAliases("second", "secondAlias");
            }
        }).build();
        GeneratorConfig.builder().install(new KRandomModule() {
            @Override
            public String id() {
                return "same.provider.normalized.semantic";
            }

            @Override
            public void configure(KRandomModuleContext context) {
                context.registerProvider(
                    ProviderDescriptor.builder("custom.normalized", FixedTrackingGenerator.class,
                                               FixedTrackingGenerator::new)
                                      .semanticKeys("custom-key", "custom.key")
                                      .build());
            }
        }).build();
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().install(new KRandomModule() {
                         @Override
                         public String id() {
                             return "ambiguous.alias";
                         }

                         @Override
                         public void configure(KRandomModuleContext context) {
                             context.registerSemanticAliases("first", "shared");
                             context.registerSemanticAliases("second", "shared");
                         }
                     }).build());
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().install(semanticModule("builtin.alias", "custom", "email"))
                                          .build());
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().install(new KRandomModule() {
                         @Override
                         public String id() {
                             return "descriptor.alias.conflict";
                         }

                         @Override
                         public void configure(KRandomModuleContext context) {
                             context.registerProvider(
                                 ProviderDescriptor.builder("custom.first", FixedTrackingGenerator.class,
                                                            FixedTrackingGenerator::new)
                                                   .semanticKeys("first")
                                                   .build());
                             context.registerSemanticAliases("second", "first");
                         }
                     }).build());
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().install(new KRandomModule() {
                         @Override
                         public String id() {
                             return "descriptor.provider.conflict";
                         }

                         @Override
                         public void configure(KRandomModuleContext context) {
                             context.registerProvider(
                                 ProviderDescriptor.builder("custom.first", FixedTrackingGenerator.class,
                                                            FixedTrackingGenerator::new)
                                                   .semanticKeys("shared-key")
                                                   .build());
                             context.registerProvider(
                                 ProviderDescriptor.builder("custom.second", FixedTrackingGenerator.class,
                                                            FixedTrackingGenerator::new)
                                                   .semanticKeys("shared_key")
                                                   .build());
                         }
                     }).build());

        ProviderDescriptor<FixedTrackingGenerator> descriptor =
            ProviderDescriptor.builder("custom.same", FixedTrackingGenerator.class, FixedTrackingGenerator::new)
                              .semanticKeys("customsame")
                              .build();
        KRandomExtensionRegistry registry = KRandomExtensionRegistry.resolve(List.of(providerModule(descriptor)));
        registry.applyTo(io.github.frikit.krandom.generator.object.SemanticFieldRegistry.builder()
                                                                                        .provider("customsame",
                                                                                                  "custom.same")
                                                                                        .build());
    }

    @Test
    @DisplayName("module installation rejects invalid IDs before build")
    void moduleIdsAreValidatedAtInstallation() {
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().install(module(" ", "custom.bad", "custom.bad")));
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().install(module("bad\nid", "custom.bad", "custom.bad")));
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().install(module("bad\rid", "custom.bad", "custom.bad")));
        assertThrows(NullPointerException.class, () -> GeneratorConfig.builder().install(null));
    }

    @Test
    @DisplayName("modules make seeded configurations intentionally non-portable")
    void modulesAreNotSerializedIntoPortableRecipes() {
        GeneratorConfig config = GeneratorConfig.builder().seed(42L).install(new TrackingModule()).build();

        assertTrue(config.getGenerationRecipe().isEmpty());
    }

    private static KRandomModule semanticModule(String id, String semanticKey, String fieldName) {
        return new KRandomModule() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public void configure(KRandomModuleContext context) {
                context.registerSemanticAliases(semanticKey, fieldName);
            }
        };
    }

    private static KRandomModule providerModule(ProviderDescriptor<?> descriptor) {
        return new KRandomModule() {
            @Override
            public String id() {
                return "provider.module";
            }

            @Override
            public void configure(KRandomModuleContext context) {
                context.registerProvider(descriptor);
            }
        };
    }

    private static final class TrackingModule implements KRandomModule {

        @Override
        public String id() {
            return "example.shipping";
        }

        @Override
        public void configure(KRandomModuleContext context) {
            context.registerProvider(
                ProviderDescriptor.builder("shipping.tracking", FixedTrackingGenerator.class,
                                           FixedTrackingGenerator::new)
                                  .aliases("tracking")
                                  .semanticKeys("trackingcode")
                                  .safetyMetadata(new ProviderSafetyMetadata(ProviderValidity.GUARANTEED,
                                                                             ProviderValidity.NOT_APPLICABLE,
                                                                             ProviderValidity.GUARANTEED,
                                                                             ProviderTestSafety.NON_ROUTABLE))
                                  .schemaProjection(ProviderSchemaProjection
                                      .builder("shipping.tracking_code",
                                               (FixedTrackingGenerator provider, GeneratorConfig config) ->
                                                   provider.generate())
                                      .aliases("tracking_code")
                                      .build())
                                  .build());
            context.registerSemanticAliases("trackingcode", "trackingCode", "shipmentTrackingCode");
        }
    }

    private static final class FixedTrackingGenerator implements Generator<String> {

        private final String country;

        private FixedTrackingGenerator(GeneratorConfig config) {
            this.country = config.getLocale().getCountry().isEmpty() ? "XX" : config.getLocale().getCountry();
        }

        @Override
        public String generate() {
            return "TRK-" + country;
        }
    }

    private static final class Shipment {

        private String trackingCode;
    }
}
