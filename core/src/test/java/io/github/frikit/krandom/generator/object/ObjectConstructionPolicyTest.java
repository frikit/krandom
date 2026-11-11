/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.ContextualGenerator;
import io.github.frikit.krandom.generator.GenerationContext;
import io.github.frikit.krandom.generator.GenerationRecipe;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationFailureDiagnostic;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
        assertSame(
                ObjectConstructionPolicy.SAFE_CONSTRUCTORS,
                defaults.toBuilder().build().getObjectConstructionPolicy());

        GeneratorConfig unsafe = GeneratorConfig.builder()
                .objectConstructionPolicy(ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS)
                .build();
        ObjectGeneratorConfig mapped =
                ObjectGeneratorConfig.builder().generatorConfig(unsafe).build();

        assertSame(ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS, mapped.getConstructionPolicy());
        assertSame(
                ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS,
                mapped.toGeneratorConfig().getObjectConstructionPolicy());
        assertThrows(NullPointerException.class, () -> GeneratorConfig.builder().objectConstructionPolicy(null));
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
        assertEquals(
                GenerationFailureCategory.CONSTRUCTION,
                ex.getContext().orElseThrow().category());
        assertTrue(ex.getCause().getMessage().contains(ObjectConstructionPolicy.SAFE_CONSTRUCTORS.name()));
    }

    @Test
    @DisplayName("unsafe bypass remains explicit and preserves legacy constructor skipping")
    void unsafeModeExplicitlyBypassesConstructors() {
        GeneratorConfig config = GeneratorConfig.builder()
                .objectConstructionPolicy(ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS)
                .build();

        UniqueConstructorFixture value = new ObjectGenerator<>(UniqueConstructorFixture.class, config).generate();

        assertEquals(0, UniqueConstructorFixture.constructorCalls);
        assertNull(value.required);
    }

    @Test
    @DisplayName("unsafe construction policy replays from a portable recipe")
    void unsafeModeReplaysFromPortableRecipe() {
        GeneratorConfig config = GeneratorConfig.builder()
                .seed(17L)
                .objectConstructionPolicy(ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS)
                .build();
        GenerationRecipe recipe = config.getGenerationRecipe().orElseThrow();

        UniqueConstructorFixture value =
                new ObjectGenerator<>(UniqueConstructorFixture.class, recipe.toGeneratorConfig()).generate();

        assertSame(ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS, recipe.getConstructionPolicy());
        assertEquals(0, UniqueConstructorFixture.constructorCalls);
        assertNull(value.required);
    }

    @Test
    @DisplayName("declared constructor parameters use Bean Validation normalization")
    void constructorParametersUseConstraintNormalization() {
        ConstrainedConstructorFixture value = new ObjectGenerator<>(ConstrainedConstructorFixture.class).generate();

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

        class LocalFixture {}
        assertUnsupportedRoot(LocalFixture.class, ObjectConstructionPolicy.SAFE_CONSTRUCTORS);

        Object anonymous = new Object() {};
        assertUnsupportedRoot(anonymous.getClass(), ObjectConstructionPolicy.SAFE_CONSTRUCTORS);
    }

    @Test
    @DisplayName("unsafe policy is named when an unsupported root cannot be allocated")
    void unsafeUnsupportedRootNamesPolicy() {
        GeneratorConfig config = GeneratorConfig.builder()
                .objectConstructionPolicy(ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS)
                .build();

        ObjectGenerationException ex = assertThrows(
                ObjectGenerationException.class, () -> new ObjectGenerator<>(AbstractFixture.class, config).generate());

        assertEquals(
                GenerationFailureCategory.CONSTRUCTION,
                ex.getContext().orElseThrow().category());
        assertTrue(ex.getCause().getMessage().contains(ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS.name()));
    }

    @Test
    @DisplayName("closed Java modules fail with an actionable qualified opens directive")
    void closedModuleFailsWithActionableOpensDirective() {
        ObjectGenerationException generationFailure =
                assertThrows(ObjectGenerationException.class, () -> new ObjectGenerator<>(Thread.class).generate());
        ObjectGenerationException populationFailure = assertThrows(
                ObjectGenerationException.class, () -> new ObjectGenerator<>(Thread.class).populate(new Thread()));

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
                                .objectOverride(AdapterFixture.class, "value", (ContextualGenerator<String>)
                                        context -> "contextual-field")
                                .build())
                .generate();
        AdapterFixture contextualType = new ObjectGenerator<>(
                        AdapterFixture.class,
                        GeneratorConfig.builder()
                                .objectOverride(
                                        String.class, (ContextualGenerator<String>) context -> "contextual-type")
                                .build())
                .generate();
        AdapterFixture plainType = new ObjectGenerator<>(
                        AdapterFixture.class,
                        GeneratorConfig.builder()
                                .objectOverride(String.class, () -> "plain-type")
                                .build())
                .generate();

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
    @DisplayName("construction adapters see field exclusions on the type and its superclasses")
    void constructionAdapterSeesExclusions() {
        GeneratorConfig config = GeneratorConfig.builder()
                .objectExcludeField("own")
                .objectExcludeField("inherited")
                .build();
        ExclusionAdapterFixture excluded = new ObjectGenerator<>(ExclusionAdapterFixture.class, config).generate();
        assertTrue(excluded.ownExcluded);
        assertTrue(excluded.inheritedExcluded);
        assertFalse(excluded.missingExcluded);

        ExclusionAdapterFixture plain = new ObjectGenerator<>(ExclusionAdapterFixture.class).generate();
        assertFalse(plain.ownExcluded);
        assertFalse(plain.inheritedExcluded);
        assertFalse(plain.missingExcluded);
    }

    @Test
    @DisplayName("an adapter stands in for the constructor, and the type's other mutable fields are filled as usual")
    void adapterBuiltTypesFillTheirOtherFields() {
        AdapterBodyFixture kept = new ObjectGenerator<>(AdapterBodyFixture.class).generate();
        AdapterBodyFixture generated = new ObjectGenerator<>(
                        AdapterBodyFixture.class,
                        GeneratorConfig.builder()
                                .objectOverrideDefaultInitialization(true)
                                .build())
                .generate();
        AdapterBodyFixture overridden = new ObjectGenerator<>(
                        AdapterBodyFixture.class,
                        GeneratorConfig.builder()
                                .objectOverride(AdapterBodyFixture.class, "label", () -> "rule")
                                .build())
                .generate();

        // An initializer stays unless default initialization is overridden or a field override targets it.
        assertEquals("initial", kept.label);
        assertNotNull(generated.label);
        assertFalse(generated.label.equals("initial"), generated.label);
        assertEquals("rule", overridden.label);
        // A member that the adapter resolved keeps the value it was constructed with.
        assertEquals(kept.constructedValue, kept.value);
        assertEquals(generated.constructedValue, generated.value);
    }

    @Test
    @DisplayName("an adapter names a parameter compiled without its name, which then meets its field's constraints")
    void adapterNamesAParameterCompiledWithoutItsName(@TempDir Path directory) throws Exception {
        Class<?> nameless = compileWithoutParameterNames(directory, NAMELESS_CODE_SOURCE);
        Parameter code = nameless.getDeclaredConstructors()[0].getParameters()[0];
        Field codeField = nameless.getDeclaredField("code");
        codeField.setAccessible(true);
        assertFalse(code.isNamePresent());
        // Read outside an adapter's call, the parameter has its own @NotBlank alone, before generation and after.
        assertNull(BeanValidationSupport.initializedField(code));
        assertNotEquals(
                new BeanValidationSupport.SizeRange(2, 2),
                BeanValidationSupport.constraintModelFor(code, String.class)
                        .text()
                        .size());

        ObjectGenerator<?> generator = new ObjectGenerator<>(
                nameless, GeneratorConfig.builder().seed(42L).build());
        for (int i = 0; i < 50; i++) {
            String value = (String) codeField.get(generator.generate());
            assertEquals(2, value.length(), value);
            assertFalse(value.isBlank(), value);
        }
        assertNull(BeanValidationSupport.initializedField(code));
        assertNotEquals(
                new BeanValidationSupport.SizeRange(2, 2),
                BeanValidationSupport.constraintModelFor(code, String.class)
                        .text()
                        .size());
    }

    @Test
    @DisplayName("a field predicate override targets an adapter's parameter through the field behind it")
    void fieldPredicateOverridesReachAnAdaptersParameter() {
        Predicate<Field> names = field -> field.getName().equals("name");
        AdapterDefaultFixture kept = new ObjectGenerator<>(AdapterDefaultFixture.class).generate();
        AdapterDefaultFixture plain = new ObjectGenerator<>(
                        AdapterDefaultFixture.class,
                        GeneratorConfig.builder()
                                .objectOverride(names, () -> "predicate")
                                .build())
                .generate();
        AdapterDefaultFixture contextual = new ObjectGenerator<>(
                        AdapterDefaultFixture.class,
                        GeneratorConfig.builder()
                                .objectOverride(names, (ContextualGenerator<String>) context -> "contextual")
                                .build())
                .generate();

        // The adapter keeps its default unless an override targets the parameter, as the Kotlin adapter does.
        assertEquals("default", kept.name);
        // A constructor parameter inside an argument the adapter names reads its own constraints.
        assertEquals(3, kept.inner.text.length(), kept.inner.text);
        assertEquals("predicate", plain.name);
        assertEquals("contextual", contextual.name);
        // A parameter with no field behind it has nothing for a field predicate to match.
        assertFalse(plain.unbackedOverridden);
        assertFalse(new ObjectGenerator<>(
                        AdapterDefaultFixture.class,
                        GeneratorConfig.builder()
                                .objectOverride(field -> field.getType() == String.class, () -> "any")
                                .build())
                .generate()
                .unbackedOverridden);
    }

    @Test
    @DisplayName("a field predicate override reaches an adapter's parameter only through a field of its type")
    void fieldPredicateOverridesNeedAFieldOfTheParametersType() {
        // The body's String code shares its name with the int parameter, which it is not filled from.
        AdapterShadowFixture value = new ObjectGenerator<>(
                        AdapterShadowFixture.class,
                        GeneratorConfig.builder()
                                .objectOverride(field -> field.getName().equals("code"), () -> "text")
                                .build())
                .generate();

        assertFalse(value.overridden);
        assertEquals("initial", value.code);
    }

    @Test
    @DisplayName("an adapter's Kotlin object is returned as it is")
    void adapterSingletonKeepsItsState() {
        AdapterSingletonFixture value = new ObjectGenerator<>(
                        AdapterSingletonFixture.class,
                        GeneratorConfig.builder()
                                .objectOverrideDefaultInitialization(true)
                                .build())
                .generate();

        assertSame(AdapterSingletonFixture.INSTANCE, value);
        assertEquals("kept", value.state);
    }

    @Test
    @DisplayName("an adapter's companion object, which its enclosing class holds, is returned as it is")
    void adapterCompanionKeepsItsState() {
        AdapterCompanionHolder.Companion value = new ObjectGenerator<>(
                        AdapterCompanionHolder.Companion.class,
                        GeneratorConfig.builder()
                                .objectOverrideDefaultInitialization(true)
                                .build())
                .generate();

        assertSame(AdapterCompanionHolder.Companion, value);
        assertEquals("kept", value.state);
    }

    @Test
    @DisplayName("an adapter's instance that a static field of its class holds is returned as it is, whatever the"
            + " field's declared type")
    void adapterSharedInstanceKeepsItsState() {
        AdapterSharedView value = new ObjectGenerator<>(
                        AdapterSharedView.class,
                        GeneratorConfig.builder()
                                .objectOverrideDefaultInitialization(true)
                                .build())
                .generate();

        assertSame(AdapterSharedView.INSTANCE, value);
        assertEquals("kept", value.state);
    }

    @Test
    @DisplayName("looking for a held instance reads no field of an enclosing class but a companion object's")
    void heldInstanceCheckLeavesEnclosingStaticsAlone() {
        // Reading InitHost.UNRELATED or InitHost.SAMPLE would run InitHost's static initializer, which fails.
        InitHost.Child child = new ObjectGenerator<>(InitHost.Child.class).generate();

        assertNotNull(child.state);
    }

    @Test
    @DisplayName("reading a static field the engine cannot access fails as a programming error")
    void inaccessibleStaticFieldFails() throws Exception {
        Method staticValue = ObjectGenerator.class.getDeclaredMethod("staticValue", Field.class);
        staticValue.setAccessible(true);
        // Not made accessible, so reading it is refused.
        Field hidden = AdapterCompanionHolder.class.getDeclaredField("HIDDEN");

        InvocationTargetException failure =
                assertThrows(InvocationTargetException.class, () -> staticValue.invoke(null, hidden));
        assertInstanceOf(IllegalStateException.class, failure.getCause());
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

        assertEquals(
                GenerationFailureCategory.CONSTRUCTION,
                failure.getContext().orElseThrow().category());
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
        KotlinWrongInstanceFixture wrongInstance = new ObjectGenerator<>(KotlinWrongInstanceFixture.class).generate();
        KotlinInstanceFieldFixture instanceField = new ObjectGenerator<>(KotlinInstanceFieldFixture.class).generate();

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
                .objectOverride(FactoryProduct.class, () -> new FactoryProductValue("plain-factory"))
                .objectOverride(FactoryProduct.class, (ContextualGenerator<FactoryProduct>) context -> {
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

        assertEquals(
                GenerationFailureCategory.CUSTOM_GENERATOR,
                nullFailure.getContext().orElseThrow().category());

        ObjectGenerationException wrongTypeFailure = assertThrows(
                ObjectGenerationException.class,
                () -> new ObjectGenerator<>(FactoryProduct.class, wrongTypeFactoryConfig()).generate());
        assertEquals(
                GenerationFailureCategory.CUSTOM_GENERATOR,
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
        assertEquals(
                GenerationFailureCategory.CUSTOM_GENERATOR,
                observed.get().context().category());
        assertEquals("FactoryProduct", observed.get().context().path());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static GeneratorConfig wrongTypeFactoryConfig() {
        Generator wrongType = () -> "wrong-type";
        return GeneratorConfig.builder()
                .objectOverride((Class) FactoryProduct.class, wrongType)
                .build();
    }

    private static void assertClosedJavaBaseFailure(ObjectGenerationException failure) {
        assertEquals(
                GenerationFailureCategory.REFLECTION,
                failure.getContext().orElseThrow().category());
        assertSame(Thread.class, failure.getContext().orElseThrow().ownerType());
        assertTrue(failure.getMessage().contains("opens java.lang to io.github.frikit.krandom;"));
    }

    private static void assertConstructionAdapterFailure(Class<?> type, Class<? extends Throwable> causeType) {
        ObjectGenerationException failure =
                assertThrows(ObjectGenerationException.class, () -> new ObjectGenerator<>(type).generate());

        assertEquals(
                GenerationFailureCategory.CONSTRUCTION,
                failure.getContext().orElseThrow().category());
        assertTrue(causeType.isInstance(failure.getCause()));
    }

    private static void assertKotlinAdapterRequired(Class<?> type) {
        ObjectGenerationException failure =
                assertThrows(ObjectGenerationException.class, () -> new ObjectGenerator<>(type).generate());

        assertTrue(failure.getCause().getMessage().contains("krandom-kotlin-dsl"));
    }

    private static void assertUnsupportedRoot(Class<?> type, ObjectConstructionPolicy policy) {
        ObjectGenerationException ex =
                assertThrows(ObjectGenerationException.class, () -> new ObjectGenerator<>(type).generate());

        assertEquals(
                GenerationFailureCategory.CONSTRUCTION,
                ex.getContext().orElseThrow().category());
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

    abstract static class AbstractFixture {}

    interface InterfaceFixture {}

    enum EnumFixture {
        VALUE
    }

    @interface AnnotationFixture {}

    interface FactoryProduct {

        String value();
    }

    record FactoryProductValue(String value) implements FactoryProduct {}

    static class ExclusionBase {

        String inherited;
    }

    static final class ExclusionAdapterFixture extends ExclusionBase {

        final String own = "own";
        final boolean ownExcluded;
        final boolean inheritedExcluded;
        final boolean missingExcluded;

        ExclusionAdapterFixture(boolean ownExcluded, boolean inheritedExcluded, boolean missingExcluded) {
            this.ownExcluded = ownExcluded;
            this.inheritedExcluded = inheritedExcluded;
            this.missingExcluded = missingExcluded;
        }
    }

    static final class AdapterFixture {

        final String value;
        final boolean explicitlyOverridden;

        AdapterFixture(String value, boolean explicitlyOverridden) {
            this.value = value;
            this.explicitlyOverridden = explicitlyOverridden;
        }
    }

    /** A class compiled without parameter names, as Kotlin compiles one, whose field and parameter are constrained. */
    private static final String NAMELESS_CODE_SOURCE = """
            public final class NamelessCode {

                @jakarta.validation.constraints.Size(min = 2, max = 2) private final String code;

                private final String note;

                public NamelessCode(@jakarta.validation.constraints.NotBlank String code, String note) {
                    this.code = code;
                    this.note = note;
                }
            }
            """;

    private static Class<?> compileWithoutParameterNames(Path directory, String source) throws Exception {
        Path file = directory.resolve("NamelessCode.java");
        Files.writeString(file, source);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "tests must run on a JDK with javac");
        int status = compiler.run(
                null,
                null,
                null,
                "-cp",
                System.getProperty("java.class.path"),
                "-d",
                directory.toString(),
                file.toString());
        assertEquals(0, status, "fixture compilation failed");
        try (URLClassLoader loader = new URLClassLoader(
                new URL[] {directory.toUri().toURL()}, ObjectConstructionPolicyTest.class.getClassLoader())) {
            return loader.loadClass("NamelessCode");
        }
    }

    static final class AdapterShadowFixture {

        final int number;
        final boolean overridden;
        String code = "initial";

        AdapterShadowFixture(int code, boolean overridden) {
            this.number = code;
            this.overridden = overridden;
        }
    }

    static final class AdapterDefaultFixture {

        final String name;
        final boolean unbackedOverridden;
        final AdapterInner inner;

        AdapterDefaultFixture(String name, boolean unbackedOverridden, AdapterInner inner) {
            this.name = name;
            this.unbackedOverridden = unbackedOverridden;
            this.inner = inner;
        }
    }

    /** Built through its constructor, inside an argument that an adapter names, whose name it does not take. */
    static final class AdapterInner {

        final String text;

        AdapterInner(@Size(min = 3, max = 3) String text) {
            this.text = text;
        }
    }

    static final class AdapterBodyFixture {

        /** A shared instance of the type, which a generated one is not. */
        static final AdapterBodyFixture TEMPLATE = new AdapterBodyFixture("template");

        final String constructedValue;
        String value;
        String label = "initial";

        AdapterBodyFixture(String value) {
            this.constructedValue = value;
            this.value = value;
        }
    }

    static final class AdapterSingletonFixture {

        static final AdapterSingletonFixture INSTANCE = new AdapterSingletonFixture();

        /** A property of the object, which Kotlin compiles to a static field of its class. */
        static final String NAME = "singleton";

        String state = "kept";
    }

    /** A view that is shared through a static field declared with its interface. */
    interface AdapterView {}

    static final class AdapterSharedView implements AdapterView {

        static final AdapterView INSTANCE = new AdapterSharedView();

        String state = "kept";
    }

    /** An enclosing class whose static initializer fails, as one with a failing companion property does. */
    static final class InitHost {

        static final Object UNRELATED = fail();

        /** A static field of the nested class's own type, which is not where Kotlin keeps a companion object. */
        static final Child SAMPLE = fail();

        /** Named after the nested class but not declared with it, so not a companion object's field either. */
        static final Object Child = fail();

        private static <V> V fail() {
            throw new IllegalStateException("InitHost must not be initialized");
        }

        static final class Child {

            String state;
        }
    }

    /**
     * As Kotlin compiles a companion object: its one instance is the static field of the enclosing class that has
     * its class's name.
     */
    static final class AdapterCompanionHolder {

        static final Companion Companion = new Companion();

        private static final String HIDDEN = "hidden";

        static final class Companion {

            String state = "kept";
        }
    }

    static final class NullAdapterFixture {}

    static final class WrongTypeAdapterFixture {}

    static final class FailingAdapterFixture {}

    static final class StructuredFailingAdapterFixture {}

    @kotlin.Metadata
    static final class MissingKotlinAdapterFixture {

        final String value;

        MissingKotlinAdapterFixture(String value) {
            this.value = value;
        }
    }

    @kotlin.Metadata
    abstract static class MissingKotlinAbstractFixture {}

    @kotlin.Metadata
    static final class MissingKotlinObjectFixture {

        static final MissingKotlinObjectFixture INSTANCE = new MissingKotlinObjectFixture();

        private MissingKotlinObjectFixture() {}
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

    final class NonStaticInnerFixture {}
}
