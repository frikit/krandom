/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.ContextualGenerator;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.provider.ConflictPolicy;
import io.github.frikit.krandom.generator.provider.ProviderHub;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("object generator extension model")
class ObjectGeneratorExtensionModelTest {

    @Test
    @DisplayName("type override accepts Generator implementations")
    void typeOverrideAcceptsGeneratorImplementation() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                                                .objectOverride(String.class, new FixedStringGenerator("type-randomizer"))
                                                .build();

        ExtensionFixture fixture = Generators.ofObject(ExtensionFixture.class, config).generate();

        assertEquals("type-randomizer", fixture.name);
        assertEquals("type-randomizer", fixture.accessToken);
        assertEquals("type-randomizer", fixture.refreshToken);
    }

    @Test
    @DisplayName("contextual override receives field metadata")
    void contextualOverrideReceivesFieldMetadata() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                                                .objectOverride(String.class,
                                                    (ContextualGenerator<String>) ctx ->
                                                        ctx.getOwnerType().getSimpleName() + ":"
                                                        + ctx.getFieldName() + ":" + ctx.getDepth())
                                                .build();

        ExtensionFixture fixture = Generators.ofObject(ExtensionFixture.class, config).generate();

        assertEquals("ExtensionFixture:name:0", fixture.name);
        assertEquals("NestedExtension:nestedCode:1", fixture.nested.nestedCode);
    }

    @Test
    @DisplayName("contextual override receives path, declared type, declaration, and active config")
    void contextualOverrideReceivesRichGenerationContext() {
        AtomicReference<String> path = new AtomicReference<>();
        AtomicReference<Type> declaredType = new AtomicReference<>();
        AtomicReference<Field> declaration = new AtomicReference<>();
        AtomicReference<GeneratorConfig> observedConfig = new AtomicReference<>();
        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(Locale.CANADA)
                                                .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                                                .objectOverride(NestedExtension.class, "nestedCode",
                                                    (ContextualGenerator<String>) ctx -> {
                                                        path.set(ctx.getPath());
                                                        declaredType.set(ctx.getDeclaredType().orElseThrow());
                                                        declaration.set((Field) ctx.getDeclaration().orElseThrow());
                                                        observedConfig.set(ctx.getConfig().orElseThrow());
                                                        return "rich-context";
                                                    })
                                                .build();

        ExtensionFixture fixture = Generators.ofObject(ExtensionFixture.class, config).generate();

        assertEquals("rich-context", fixture.nested.nestedCode);
        assertEquals("ExtensionFixture.nested.nestedCode", path.get());
        assertEquals(String.class, declaredType.get());
        assertEquals("nestedCode", declaration.get().getName());
        assertEquals(config, observedConfig.get());
    }

    @Test
    @DisplayName("predicate field override applies to matching fields")
    void predicateFieldOverrideAppliesToMatchingFields() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                                                .objectOverride(FieldPredicates.nameMatches(".*Token"),
                                                                new FixedStringGenerator("predicate-randomizer"))
                                                .build();

        ExtensionFixture fixture = Generators.ofObject(ExtensionFixture.class, config).generate();

        assertEquals("predicate-randomizer", fixture.accessToken);
        assertEquals("predicate-randomizer", fixture.refreshToken);
        assertNotNull(fixture.name);
    }

    @Test
    @DisplayName("contextual predicate override receives owner, field, and depth")
    void contextualPredicateOverrideReceivesGenerationContext() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                                                .objectOverride(
                                                    FieldPredicates.nameMatches(".*Code"),
                                                    (ContextualGenerator<String>) ctx ->
                                                        ctx.getOwnerType().getSimpleName() + ":"
                                                        + ctx.getFieldName() + ":" + ctx.getDepth())
                                                .build();

        ExtensionFixture fixture = Generators.ofObject(ExtensionFixture.class, config).generate();

        assertEquals("ExtensionFixture:rootCode:0", fixture.rootCode);
        assertEquals("NestedExtension:nestedCode:1", fixture.nested.nestedCode);
    }

    @Test
    @DisplayName("predicate field override wins over type override")
    void predicateFieldOverrideWinsOverTypeOverride() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                                                .objectOverride(String.class, new FixedStringGenerator("type"))
                                                .objectOverride(FieldPredicates.nameMatches(".*Token"),
                                                                new FixedStringGenerator("predicate"))
                                                .build();

        ExtensionFixture fixture = Generators.ofObject(ExtensionFixture.class, config).generate();

        assertEquals("predicate", fixture.accessToken);
        assertEquals("predicate", fixture.refreshToken);
        assertEquals("type", fixture.name);
    }

    @Test
    @DisplayName("exact field override wins over predicate field override")
    void exactFieldOverrideWinsOverPredicateFieldOverride() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                                                .objectOverride(FieldPredicates.ofType(String.class),
                                                                new FixedStringGenerator("predicate"))
                                                .objectOverride(ExtensionFixture.class, "accessToken",
                                                                new FixedStringGenerator("exact"))
                                                .build();

        ExtensionFixture fixture = Generators.ofObject(ExtensionFixture.class, config).generate();

        assertEquals("exact", fixture.accessToken);
        assertEquals("predicate", fixture.refreshToken);
        assertEquals("predicate", fixture.name);
    }

    @Test
    @DisplayName("ProviderHub registrations can supply object overrides")
    @SuppressWarnings("unchecked")
    void providerHubRegistrationsCanSupplyObjectOverrides() {
        ProviderHub hub = new ProviderHub(GeneratorConfig.builder().locale(Locale.UK).build());
        hub.register("tokens.session", cfg -> (Generator<String>) () -> "session-" + cfg.getLocale().getCountry());
        hub.registerAlias("session_token", "tokens.session");
        hub.register("tokens.session",
                     cfg -> (Generator<String>) () -> "replacement-" + cfg.getLocale().getCountry(),
                     ConflictPolicy.REPLACE);

        Generator<String> sessionToken = hub.get("session_token", Generator.class);
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                                                .objectOverride(ExtensionFixture.class, "accessToken", sessionToken)
                                                .build();

        ExtensionFixture fixture = Generators.ofObject(ExtensionFixture.class, config).generate();

        assertEquals("replacement-GB", fixture.accessToken);
    }

    @Test
    @DisplayName("special construction uses explicit object overrides")
    void specialConstructionUsesExplicitObjectOverrides() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                                                .objectOverride(ExternalId.class,
                                                                () -> new ExternalId("factory-like-id"))
                                                .build();

        FactoryFixture fixture = Generators.ofObject(FactoryFixture.class, config).generate();

        assertEquals("factory-like-id", fixture.externalId.value);
        assertNotNull(fixture.allArgsOnly);
    }

    @Test
    @DisplayName("object config predicate overrides can be exported to GeneratorConfig")
    void objectConfigPredicateOverridesExportToGeneratorConfig() {
        ObjectGeneratorConfig objectConfig = ObjectGeneratorConfig.builder()
                                                                  .semanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                                                                  .override(FieldPredicates.named("accessToken"),
                                                                            new FixedStringGenerator("local"))
                                                                  .override(FieldPredicates.named("rootCode"),
                                                                            (ContextualGenerator<String>) ctx ->
                                                                                "ctx-" + ctx.getFieldName())
                                                                  .build();

        GeneratorConfig rootConfig = objectConfig.toGeneratorConfig();
        ExtensionFixture fixture = Generators.ofObject(ExtensionFixture.class, rootConfig).generate();

        assertEquals("local", fixture.accessToken);
        assertEquals("ctx-rootCode", fixture.rootCode);
    }

    @Test
    @DisplayName("object-scoped predicate overrides apply without exporting to GeneratorConfig")
    void objectConfigPredicateOverridesApplyLocally() {
        ObjectGeneratorConfig objectConfig = ObjectGeneratorConfig.builder()
                                                                  .semanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                                                                  .override(FieldPredicates.named("accessToken"),
                                                                            new FixedStringGenerator("local"))
                                                                  .override(FieldPredicates.named("rootCode"),
                                                                            (ContextualGenerator<String>) ctx ->
                                                                                "ctx-" + ctx.getDepth())
                                                                  .build();

        ExtensionFixture fixture = new ObjectGenerator<>(ExtensionFixture.class, objectConfig).generate();

        assertEquals("local", fixture.accessToken);
        assertEquals("ctx-0", fixture.rootCode);
    }

    @Test
    @DisplayName("predicate override accessors fall back when local predicates do not match")
    void predicateOverrideAccessorsFallBackWhenLocalPredicatesDoNotMatch() throws NoSuchFieldException {
        Field nameField = ExtensionFixture.class.getDeclaredField("name");
        ObjectGeneratorConfig objectConfig = ObjectGeneratorConfig.builder()
                                                                  .override(FieldPredicates.named("missing"),
                                                                            new FixedStringGenerator("local"))
                                                                  .override(FieldPredicates.named("alsoMissing"),
                                                                            (ContextualGenerator<String>) ctx -> "ctx")
                                                                  .build();

        assertTrue(objectConfig.getFieldPredicateOverride(nameField).isEmpty());
        assertTrue(objectConfig.getContextualFieldPredicateOverride(nameField).isEmpty());
    }

    @Test
    @DisplayName("predicate overrides protect values from semantic coherence rewrites")
    void predicateOverridesProtectValuesFromSemanticCoherenceRewrites() {
        GeneratorConfig plainConfig = GeneratorConfig.builder()
                                                     .objectOverride(NameCoherenceFixture.class, "firstName",
                                                                     new FixedStringGenerator("Alice"))
                                                     .objectOverride(NameCoherenceFixture.class, "lastName",
                                                                     new FixedStringGenerator("Smith"))
                                                     .objectOverride(FieldPredicates.named("fullName"),
                                                                     new FixedStringGenerator("Manual Name"))
                                                     .build();

        NameCoherenceFixture plain = Generators.ofObject(NameCoherenceFixture.class, plainConfig).generate();

        assertEquals("Manual Name", plain.fullName);

        GeneratorConfig contextualConfig = GeneratorConfig.builder()
                                                          .objectOverride(NameCoherenceFixture.class, "firstName",
                                                                          new FixedStringGenerator("Alice"))
                                                          .objectOverride(NameCoherenceFixture.class, "lastName",
                                                                          new FixedStringGenerator("Smith"))
                                                          .objectOverride(FieldPredicates.named("fullName"),
                                                                          (ContextualGenerator<String>) ctx ->
                                                                              "Manual " + ctx.getFieldName())
                                                          .build();

        NameCoherenceFixture contextual = Generators.ofObject(NameCoherenceFixture.class, contextualConfig).generate();

        assertEquals("Manual fullName", contextual.fullName);
    }

    @Test
    @DisplayName("predicate override APIs validate null arguments")
    void predicateOverrideApisValidateNullArguments() {
        Generator<String> generator = new FixedStringGenerator("x");
        ContextualGenerator<String> contextualGenerator = ctx -> "x";
        Predicate<Field> predicate = FieldPredicates.named("name");

        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().objectOverride((Predicate<Field>) null, generator));
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().objectOverride(predicate, (Generator<String>) null));
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().objectOverride((Predicate<Field>) null, contextualGenerator));
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().objectOverride(predicate, (ContextualGenerator<String>) null));

        ObjectGeneratorConfig.Builder objectBuilder = ObjectGeneratorConfig.builder();
        assertThrows(NullPointerException.class,
                     () -> objectBuilder.override((Predicate<Field>) null, generator));
        assertThrows(NullPointerException.class,
                     () -> objectBuilder.override(predicate, (Generator<String>) null));
        assertThrows(NullPointerException.class,
                     () -> objectBuilder.override((Predicate<Field>) null, contextualGenerator));
        assertThrows(NullPointerException.class,
                     () -> objectBuilder.override(predicate, (ContextualGenerator<String>) null));

        GeneratorConfig rootConfig = GeneratorConfig.defaults();
        ObjectGeneratorConfig objectConfig = ObjectGeneratorConfig.builder().build();
        assertThrows(NullPointerException.class, () -> rootConfig.getObjectFieldPredicateOverride(null));
        assertThrows(NullPointerException.class, () -> rootConfig.getObjectContextualFieldPredicateOverride(null));
        assertThrows(NullPointerException.class, () -> objectConfig.getFieldPredicateOverride(null));
        assertThrows(NullPointerException.class, () -> objectConfig.getContextualFieldPredicateOverride(null));
    }

    private static final class FixedStringGenerator implements Generator<String> {

        private final String value;

        private FixedStringGenerator(String value) {
            this.value = value;
        }

        @Override
        public String generate() {
            return value;
        }
    }

    static class ExtensionFixture {

        String          name;
        String          accessToken;
        String          refreshToken;
        String          rootCode;
        NestedExtension nested;
    }

    static class NestedExtension {

        String nestedCode;
    }

    static class NameCoherenceFixture {

        String firstName;
        String lastName;
        String fullName;
    }

    static class FactoryFixture {

        ExternalId  externalId;
        AllArgsOnly allArgsOnly;
    }

    static final class ExternalId {

        final String value;

        ExternalId(String value) {
            this.value = value;
        }
    }

    static final class AllArgsOnly {

        String value;

        AllArgsOnly(String value) {
            this.value = value;
        }
    }
}
