/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.ContextualGenerator;
import io.github.frikit.krandom.generator.GenerationContext;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationFailureDiagnostic;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Object construction policy")
class ObjectConstructionPolicyTest {

    @BeforeEach
    void resetConstructorCounters() {
        UniqueConstructorFixture.constructorCalls = 0;
        AmbiguousConstructorFixture.constructorCalls = 0;
        TestObjectConstructionAdapter.calls = 0;
    }

    @Test
    @DisplayName("safe constructors are the default and round-trip through configuration")
    void safePolicyIsTheRoundTrippingDefault() {
        GeneratorConfig defaults = GeneratorConfig.defaults();
        assertSame(ObjectConstructionPolicy.SAFE_CONSTRUCTORS, defaults.getObjectConstructionPolicy());
        assertSame(ObjectConstructionPolicy.SAFE_CONSTRUCTORS,
                   defaults.toBuilder().build().getObjectConstructionPolicy());

        GeneratorConfig unsafe = GeneratorConfig.builder()
                                                .objectConstructionPolicy(
                                                    ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS)
                                                .build();
        ObjectGeneratorConfig mapped = ObjectGeneratorConfig.builder().generatorConfig(unsafe).build();

        assertSame(ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS, mapped.getConstructionPolicy());
        assertSame(ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS,
                   mapped.toGeneratorConfig().getObjectConstructionPolicy());
        assertThrows(NullPointerException.class,
                     () -> GeneratorConfig.builder().objectConstructionPolicy(null));
    }

    @Test
    @DisplayName("safe mode invokes one declared constructor and preserves its final invariant")
    void safeModeInvokesUniqueDeclaredConstructor() {
        UniqueConstructorFixture value = new ObjectGenerator<>(UniqueConstructorFixture.class).generate();

        assertEquals(1, UniqueConstructorFixture.constructorCalls);
        assertNotNull(value.required);
        assertTrue(value.invariantEstablished);
    }

    @Test
    @DisplayName("safe mode rejects ambiguous declared constructors contextually")
    void safeModeRejectsAmbiguousConstructors() {
        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(AmbiguousConstructorFixture.class).generate());

        assertEquals(0, AmbiguousConstructorFixture.constructorCalls);
        assertEquals(GenerationFailureCategory.CONSTRUCTION, ex.getContext().orElseThrow().category());
        assertTrue(ex.getCause().getMessage().contains(ObjectConstructionPolicy.SAFE_CONSTRUCTORS.name()));
    }

    @Test
    @DisplayName("unsafe bypass remains explicit and preserves legacy constructor skipping")
    void unsafeModeExplicitlyBypassesConstructors() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectConstructionPolicy(
                                                    ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS)
                                                .build();

        UniqueConstructorFixture value =
            new ObjectGenerator<>(UniqueConstructorFixture.class, config).generate();

        assertEquals(0, UniqueConstructorFixture.constructorCalls);
        assertNull(value.required);
    }

    @Test
    @DisplayName("declared constructor parameters use Bean Validation normalization")
    void constructorParametersUseConstraintNormalization() {
        ConstrainedConstructorFixture value =
            new ObjectGenerator<>(ConstrainedConstructorFixture.class).generate();

        assertNotNull(value.code);
        assertEquals(4, value.code.length());
        assertFalse(value.code.isBlank());
    }

    @Test
    @DisplayName("unsupported root shapes fail before allocation under the selected policy")
    void unsupportedRootShapesFailBeforeAllocation() {
        assertUnsupportedRoot(AbstractFixture.class, ObjectConstructionPolicy.SAFE_CONSTRUCTORS);
        assertUnsupportedRoot(InterfaceFixture.class, ObjectConstructionPolicy.SAFE_CONSTRUCTORS);
        assertUnsupportedRoot(NonStaticInnerFixture.class, ObjectConstructionPolicy.SAFE_CONSTRUCTORS);
        assertUnsupportedRoot(String[].class, ObjectConstructionPolicy.SAFE_CONSTRUCTORS);
        assertUnsupportedRoot(int.class, ObjectConstructionPolicy.SAFE_CONSTRUCTORS);
        assertUnsupportedRoot(EnumFixture.class, ObjectConstructionPolicy.SAFE_CONSTRUCTORS);
        assertUnsupportedRoot(AnnotationFixture.class, ObjectConstructionPolicy.SAFE_CONSTRUCTORS);

        class LocalFixture {
        }
        assertUnsupportedRoot(LocalFixture.class, ObjectConstructionPolicy.SAFE_CONSTRUCTORS);

        Object anonymous = new Object() {};
        assertUnsupportedRoot(anonymous.getClass(), ObjectConstructionPolicy.SAFE_CONSTRUCTORS);
    }

    @Test
    @DisplayName("unsafe policy is named when an unsupported root cannot be allocated")
    void unsafeUnsupportedRootNamesPolicy() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectConstructionPolicy(
                                                    ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS)
                                                .build();

        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(AbstractFixture.class, config).generate());

        assertEquals(GenerationFailureCategory.CONSTRUCTION, ex.getContext().orElseThrow().category());
        assertTrue(ex.getCause().getMessage().contains(
            ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS.name()));
    }

    @Test
    @DisplayName("closed Java modules fail with an actionable qualified opens directive")
    void closedModuleFailsWithActionableOpensDirective() {
        ObjectGenerationException generationFailure = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(Thread.class).generate());
        ObjectGenerationException populationFailure = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(Thread.class).populate(new Thread()));

        assertClosedJavaBaseFailure(generationFailure);
        assertClosedJavaBaseFailure(populationFailure);
    }

    @Test
    @DisplayName("construction adapters resolve parameters through standard override handling")
    void constructionAdapterResolvesParametersThroughCorePipeline() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectOverride(AdapterFixture.class, "value", () -> "configured")
                                                .build();

        AdapterFixture value = new ObjectGenerator<>(AdapterFixture.class, config).generate();

        assertEquals(1, TestObjectConstructionAdapter.calls);
        assertEquals("configured", value.value);
        assertTrue(value.explicitlyOverridden);
    }

    @Test
    @DisplayName("construction adapters report an absent explicit override")
    void constructionAdapterReportsAbsentExplicitOverride() {
        AdapterFixture value = new ObjectGenerator<>(AdapterFixture.class).generate();

        assertNotNull(value.value);
        assertFalse(value.explicitlyOverridden);
        assertEquals(1, TestObjectConstructionAdapter.calls);
    }

    @Test
    @DisplayName("construction adapters recognize contextual and type overrides")
    void constructionAdapterRecognizesEveryExplicitOverrideKind() {
        AdapterFixture contextualField = new ObjectGenerator<>(
            AdapterFixture.class,
            GeneratorConfig.builder()
                           .objectOverride(
                               AdapterFixture.class,
                               "value",
                               (ContextualGenerator<String>) context -> "contextual-field")
                           .build()).generate();
        AdapterFixture contextualType = new ObjectGenerator<>(
            AdapterFixture.class,
            GeneratorConfig.builder()
                           .objectOverride(
                               String.class,
                               (ContextualGenerator<String>) context -> "contextual-type")
                           .build()).generate();
        AdapterFixture plainType = new ObjectGenerator<>(
            AdapterFixture.class,
            GeneratorConfig.builder().objectOverride(String.class, () -> "plain-type").build()).generate();

        assertEquals("contextual-field", contextualField.value);
        assertTrue(contextualField.explicitlyOverridden);
        assertEquals("contextual-type", contextualType.value);
        assertTrue(contextualType.explicitlyOverridden);
        assertEquals("plain-type", plainType.value);
        assertTrue(plainType.explicitlyOverridden);
        assertEquals(3, TestObjectConstructionAdapter.calls);
    }

    @Test
    @DisplayName("root factories remain stronger than construction adapters")
    void rootFactoryWinsOverConstructionAdapter() {
        AdapterFixture expected = new AdapterFixture("factory", true);
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectOverride(AdapterFixture.class, () -> expected)
                                                .build();

        AdapterFixture value = new ObjectGenerator<>(AdapterFixture.class, config).generate();

        assertSame(expected, value);
        assertEquals(0, TestObjectConstructionAdapter.calls);
    }

    @Test
    @DisplayName("construction adapters validate output and preserve structured failures")
    void constructionAdapterFailuresAreContextual() {
        assertConstructionAdapterFailure(NullAdapterFixture.class, IllegalStateException.class);
        assertConstructionAdapterFailure(WrongTypeAdapterFixture.class, IllegalArgumentException.class);
        assertConstructionAdapterFailure(FailingAdapterFixture.class, IllegalStateException.class);

        ObjectGenerationException structured = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(StructuredFailingAdapterFixture.class).generate());
        assertEquals("structured adapter failure", structured.getMessage());
    }

    @Test
    @DisplayName("immutable Kotlin metadata fails clearly without the Kotlin construction adapter")
    void immutableKotlinTypeRequiresKotlinAdapter() {
        ObjectGenerationException failure = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(MissingKotlinAdapterFixture.class).generate());

        assertEquals(GenerationFailureCategory.CONSTRUCTION, failure.getContext().orElseThrow().category());
        assertTrue(failure.getCause().getMessage().contains("krandom-kotlin-dsl"));
    }

    @Test
    @DisplayName("Kotlin abstract and object shapes require the Kotlin construction adapter")
    void KotlinAbstractAndObjectShapesRequireAdapter() {
        assertKotlinAdapterRequired(MissingKotlinAbstractFixture.class);
        assertKotlinAdapterRequired(MissingKotlinObjectFixture.class);
    }

    @Test
    @DisplayName("mutable Kotlin metadata remains eligible for ordinary Java construction")
    void mutableKotlinTypeUsesOrdinaryConstruction() {
        KotlinMutableFixture value = new ObjectGenerator<>(KotlinMutableFixture.class).generate();
        KotlinWrongInstanceFixture wrongInstance =
            new ObjectGenerator<>(KotlinWrongInstanceFixture.class).generate();
        KotlinInstanceFieldFixture instanceField =
            new ObjectGenerator<>(KotlinInstanceFieldFixture.class).generate();

        assertNotNull(value.value);
        assertNotNull(wrongInstance.value);
        assertNotNull(instanceField.value);
    }

    @Test
    @DisplayName("synthetic Kotlin metadata fields do not make a mutable type immutable")
    void syntheticKotlinFieldDoesNotRequireAdapter() {
        ObjectGenerationException failure = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(KotlinSyntheticFieldFixture.class).generate());

        assertFalse(failure.getCause().getMessage().contains("krandom-kotlin-dsl"));
    }

    @Test
    @DisplayName("plain type overrides act as root factories before reflection")
    void plainTypeOverrideActsAsRootFactory() {
        FactoryProduct expected = new FactoryProductValue("plain-factory");
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectOverride(FactoryProduct.class, () -> expected)
                                                .build();

        FactoryProduct value = new ObjectGenerator<>(FactoryProduct.class, config).generate();

        assertSame(expected, value);
    }

    @Test
    @DisplayName("contextual root factories receive root context and win over plain overrides")
    void contextualRootFactoryReceivesContextAndWins() {
        AtomicReference<GenerationContext> observed = new AtomicReference<>();
        FactoryProduct contextual = new FactoryProductValue("contextual-factory");
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectOverride(
                                                    FactoryProduct.class,
                                                    () -> new FactoryProductValue("plain-factory"))
                                                .objectOverride(
                                                    FactoryProduct.class,
                                                    (ContextualGenerator<FactoryProduct>) context -> {
                                                        observed.set(context);
                                                        return contextual;
                                                    })
                                                .build();

        FactoryProduct value = new ObjectGenerator<>(FactoryProduct.class, config).generate();

        assertSame(contextual, value);
        assertEquals("$root", observed.get().getFieldName());
        assertSame(FactoryProduct.class, observed.get().getOwnerType());
        assertEquals(0, observed.get().getDepth());
    }

    @Test
    @DisplayName("invalid root factory values fail with custom-generator context")
    void invalidRootFactoryValuesFailContextually() {
        GeneratorConfig nullConfig = GeneratorConfig.builder()
                                                    .objectOverride(FactoryProduct.class, () -> null)
                                                    .build();
        ObjectGenerationException nullFailure = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(FactoryProduct.class, nullConfig).generate());

        assertEquals(GenerationFailureCategory.CUSTOM_GENERATOR,
                     nullFailure.getContext().orElseThrow().category());

        ObjectGenerationException wrongTypeFailure = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(FactoryProduct.class, wrongTypeFactoryConfig()).generate());
        assertEquals(GenerationFailureCategory.CUSTOM_GENERATOR,
                     wrongTypeFailure.getContext().orElseThrow().category());
    }

    @Test
    @DisplayName("lenient root factory failure returns null and emits a diagnostic")
    void lenientRootFactoryFailureReturnsNullAndEmitsDiagnostic() {
        AtomicReference<GenerationFailureDiagnostic> observed = new AtomicReference<>();
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectOverride(FactoryProduct.class, () -> null)
                                                .objectIgnoreErrors(true)
                                                .generationFailureListener(observed::set)
                                                .build();

        FactoryProduct value = new ObjectGenerator<>(FactoryProduct.class, config).generate();

        assertNull(value);
        assertEquals(GenerationFailureCategory.CUSTOM_GENERATOR, observed.get().context().category());
        assertEquals("FactoryProduct", observed.get().context().path());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static GeneratorConfig wrongTypeFactoryConfig() {
        Generator wrongType = () -> "wrong-type";
        return GeneratorConfig.builder()
                              .objectOverride((Class) FactoryProduct.class, wrongType)
                              .build();
    }

    private static void assertClosedJavaBaseFailure(ObjectGenerationException failure) {
        assertEquals(GenerationFailureCategory.REFLECTION,
                     failure.getContext().orElseThrow().category());
        assertSame(Thread.class, failure.getContext().orElseThrow().ownerType());
        assertTrue(failure.getMessage().contains(
            "opens java.lang to io.github.frikit.krandom;"));
    }

    private static void assertConstructionAdapterFailure(Class<?> type, Class<? extends Throwable> causeType) {
        ObjectGenerationException failure = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(type).generate());

        assertEquals(GenerationFailureCategory.CONSTRUCTION, failure.getContext().orElseThrow().category());
        assertTrue(causeType.isInstance(failure.getCause()));
    }

    private static void assertKotlinAdapterRequired(Class<?> type) {
        ObjectGenerationException failure = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(type).generate());

        assertTrue(failure.getCause().getMessage().contains("krandom-kotlin-dsl"));
    }

    private static void assertUnsupportedRoot(Class<?> type, ObjectConstructionPolicy policy) {
        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(type).generate());

        assertEquals(GenerationFailureCategory.CONSTRUCTION, ex.getContext().orElseThrow().category());
        assertTrue(ex.getCause().getMessage().contains(policy.name()));
    }

    static final class UniqueConstructorFixture {

        static int constructorCalls;

        final String required;
        final boolean invariantEstablished;

        private UniqueConstructorFixture(String required) {
            constructorCalls++;
            this.required = java.util.Objects.requireNonNull(required);
            this.invariantEstablished = true;
        }
    }

    static final class AmbiguousConstructorFixture {

        static int constructorCalls;

        String value;

        AmbiguousConstructorFixture(String value) {
            constructorCalls++;
            this.value = value;
        }

        AmbiguousConstructorFixture(int value) {
            constructorCalls++;
            this.value = Integer.toString(value);
        }
    }

    static final class ConstrainedConstructorFixture {

        final String code;

        ConstrainedConstructorFixture(@NotBlank @Size(min = 4, max = 4) String code) {
            this.code = code;
        }
    }

    abstract static class AbstractFixture {
    }

    interface InterfaceFixture {
    }

    enum EnumFixture { VALUE }

    @interface AnnotationFixture {
    }

    interface FactoryProduct {

        String value();
    }

    record FactoryProductValue(String value) implements FactoryProduct {
    }

    static final class AdapterFixture {

        final String  value;
        final boolean explicitlyOverridden;

        AdapterFixture(String value, boolean explicitlyOverridden) {
            this.value = value;
            this.explicitlyOverridden = explicitlyOverridden;
        }
    }

    static final class NullAdapterFixture {
    }

    static final class WrongTypeAdapterFixture {
    }

    static final class FailingAdapterFixture {
    }

    static final class StructuredFailingAdapterFixture {
    }

    @kotlin.Metadata
    static final class MissingKotlinAdapterFixture {

        final String value;

        MissingKotlinAdapterFixture(String value) {
            this.value = value;
        }
    }

    @kotlin.Metadata
    abstract static class MissingKotlinAbstractFixture {
    }

    @kotlin.Metadata
    static final class MissingKotlinObjectFixture {

        static final MissingKotlinObjectFixture INSTANCE = new MissingKotlinObjectFixture();

        private MissingKotlinObjectFixture() {
        }
    }

    @kotlin.Metadata
    static final class KotlinMutableFixture {

        static final Object MARKER = new Object();

        String value;
    }

    @kotlin.Metadata
    static final class KotlinWrongInstanceFixture {

        static final Object INSTANCE = new Object();

        String value;
    }

    @kotlin.Metadata
    static final class KotlinInstanceFieldFixture {

        String INSTANCE;
        String value;
    }

    @kotlin.Metadata
    final class KotlinSyntheticFieldFixture {

        String value = ObjectConstructionPolicyTest.this.toString();
    }

    final class NonStaticInnerFixture {
    }
}
