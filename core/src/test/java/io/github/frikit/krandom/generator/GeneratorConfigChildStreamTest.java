/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.extension.KRandomModule;
import io.github.frikit.krandom.generator.extension.KRandomModuleContext;
import io.github.frikit.krandom.generator.finance.BankingSafetyPolicy;
import io.github.frikit.krandom.generator.finance.CryptoAddressSafetyPolicy;
import io.github.frikit.krandom.generator.finance.PaymentCardSafetyPolicy;
import io.github.frikit.krandom.generator.finance.SecuritiesIdentifierSafetyPolicy;
import io.github.frikit.krandom.generator.location.PhoneNumberSafetyPolicy;
import io.github.frikit.krandom.generator.network.NetworkSafetyPolicy;
import io.github.frikit.krandom.generator.object.ObjectConstructionPolicy;
import io.github.frikit.krandom.generator.object.ObjectFieldStreamPolicy;
import io.github.frikit.krandom.generator.object.ObjectGenerationSemanticMode;
import io.github.frikit.krandom.generator.object.SemanticFieldRegistry;
import io.github.frikit.krandom.generator.user.IdentityDocumentSafetyPolicy;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdSafetyPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * A child stream of a seeded configuration must equal, in every field, what
 * {@code toBuilder().seed(GenerationRecipe.deriveChildSeed(seed, name)).build()} makes, which is how
 * {@link GeneratorConfig#forChildStream(String)} made every child before it learned to copy a parent
 * without modules directly. The fields are compared by reflection, so a field added later and missed by
 * that copy fails here.
 */
@DisplayName("Child-stream configurations")
class GeneratorConfigChildStreamTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    /** Names as composite generators and schemas give them, and one that is not ASCII. */
    private static final List<String> STREAM_NAMES = List.of("invoice", "schema|record=3|field=4:name", "straße");

    /**
     * Fields that keep their default value in every parent that hands its children a stream of their own
     * without modules: such a parent has a seed, so no other random source, and the legacy child-stream
     * policy would hand children the parent itself. A restored parent covers the recipe flag.
     */
    private static final Set<String> DEFAULT_IN_EVERY_SUCH_PARENT = Set.of(
            "random",
            "randomFactory",
            "secureRandom",
            "modules",
            "extensionRegistry",
            "childStreamPolicy",
            "restoredFromRecipe");

    static Stream<Arguments> seededParents() {
        return Stream.of(
                Arguments.of(Named.of(
                        "numeric seed", GeneratorConfig.builder().seed(42L).build())),
                Arguments.of(Named.of(
                        "textual seed",
                        GeneratorConfig.builder().seed("child-streams").build())),
                Arguments.of(Named.of(
                        "restored from a recipe with a numeric seed",
                        restored(portableState(ObjectFieldStreamPolicy.LEGACY)))),
                Arguments.of(Named.of(
                        "restored from a recipe with a textual seed",
                        restored(portableState(ObjectFieldStreamPolicy.INDEPENDENT)))),
                Arguments.of(Named.of(
                        "every kind of state, legacy object field streams",
                        everyKindOfState(ObjectFieldStreamPolicy.LEGACY))),
                Arguments.of(Named.of(
                        "every kind of state, independent object field streams",
                        everyKindOfState(ObjectFieldStreamPolicy.INDEPENDENT))));
    }

    static Stream<Arguments> portableParents() {
        return Stream.of(
                Arguments.of(Named.of(
                        "numeric seed",
                        GeneratorConfig.builder().seed(42L).clock(FIXED_CLOCK).build())),
                Arguments.of(Named.of(
                        "textual seed",
                        GeneratorConfig.builder()
                                .seed("child-streams")
                                .clock(FIXED_CLOCK)
                                .build())),
                Arguments.of(Named.of(
                        "restored from a recipe with a numeric seed",
                        restored(portableState(ObjectFieldStreamPolicy.LEGACY)))),
                Arguments.of(Named.of(
                        "restored from a recipe with a textual seed",
                        restored(portableState(ObjectFieldStreamPolicy.INDEPENDENT)))),
                Arguments.of(Named.of(
                        "every portable setting, legacy object field streams",
                        portableState(ObjectFieldStreamPolicy.LEGACY).build())),
                Arguments.of(Named.of(
                        "every portable setting, independent object field streams",
                        portableState(ObjectFieldStreamPolicy.INDEPENDENT).build())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("seededParents")
    @DisplayName("a child stream equals in every field the configuration rebuilt with the child's seed")
    void childEqualsTheRebuiltChild(GeneratorConfig parent) throws IllegalAccessException {
        for (String name : STREAM_NAMES) {
            GeneratorConfig child = parent.forChildStream(name);

            assertNotSame(parent, child, name);
            assertEquals(
                    GenerationRecipe.deriveChildSeed(parent.getSeed().getAsLong(), name),
                    child.getSeed().getAsLong(),
                    name);
            assertSameFields(rebuilt(parent, name), child);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("seededParents")
    @DisplayName("the internal seed copy equals in every field the configuration rebuilt with that seed")
    void internalSeedCopyEqualsTheRebuild(GeneratorConfig parent) throws IllegalAccessException {
        for (long seed : new long[] {0L, -1L, 7L, Long.MIN_VALUE}) {
            GeneratorConfig copy = io.github.frikit.krandom.internal.ConfigAccess.withSeed(parent, seed);

            assertNotSame(parent, copy);
            assertEquals(seed, copy.getSeed().getAsLong());
            assertSameFields(parent.toBuilder().seed(seed).build(), copy);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("seededParents")
    @DisplayName("a child of a child equals in every field the configuration rebuilt twice")
    void grandchildEqualsTheTwiceRebuiltGrandchild(GeneratorConfig parent) throws IllegalAccessException {
        GeneratorConfig grandchild = parent.forChildStream("order").forChildStream("line");

        assertSameFields(rebuilt(rebuilt(parent, "order"), "line"), grandchild);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("portableParents")
    @DisplayName("a child stream of a portable configuration records the same recipe either way")
    void childRecordsTheSameRecipe(GeneratorConfig parent) {
        for (String name : STREAM_NAMES) {
            assertEquals(recipe(rebuilt(parent, name)), recipe(parent.forChildStream(name)), name);
        }
        assertEquals(
                recipe(rebuilt(rebuilt(parent, "order"), "line")),
                recipe(parent.forChildStream("order").forChildStream("line")));
    }

    @Test
    @DisplayName("a child stream of a configuration with a module is built again, which configures the module again")
    void childOfAConfigurationWithAModuleIsBuiltAgain() {
        AtomicInteger configured = new AtomicInteger();
        GeneratorConfig parent = GeneratorConfig.builder()
                .seed(42L)
                .install(new CountingModule(configured))
                .build();
        assertEquals(1, configured.get());

        GeneratorConfig child = parent.forChildStream("invoice");
        assertEquals(2, configured.get());
        GeneratorConfig grandchild = child.forChildStream("line");
        assertEquals(3, configured.get());
        parent.forChildStream("order");
        assertEquals(4, configured.get());

        assertEquals(
                GenerationRecipe.deriveChildSeed(42L, "invoice"),
                child.getSeed().getAsLong());
        assertEquals(
                GenerationRecipe.deriveChildSeed(child.getSeed().getAsLong(), "line"),
                grandchild.getSeed().getAsLong());
        assertEquals(List.of("counting"), child.getExtensionRegistry().getModuleIds());
        assertNotSame(parent.getExtensionRegistry(), child.getExtensionRegistry());
    }

    @Test
    @DisplayName("a child stream of a configuration without modules shares its parent's collections")
    void childWithoutModulesSharesItsParentsCollections() throws IllegalAccessException {
        GeneratorConfig parent = everyKindOfState(ObjectFieldStreamPolicy.INDEPENDENT);
        GeneratorConfig child = parent.forChildStream("invoice");

        List<String> shared = new ArrayList<>();
        for (Field field : instanceFields()) {
            Object value = field.get(parent);
            if (value instanceof Collection<?> || value instanceof Map<?, ?>) {
                assertSame(value, field.get(child), field.getName());
                shared.add(field.getName());
            }
        }
        assertTrue(
                shared.containsAll(
                        List.of("modules", "objectSubtypes", "objectTypeOverrides", "objectUniqueFieldNames")),
                shared::toString);
        assertSame(parent.getExtensionRegistry(), child.getExtensionRegistry());
        assertSame(parent.getObjectSemanticRegistry(), child.getObjectSemanticRegistry());
    }

    @Test
    @DisplayName("the parents with every kind of state leave no field a child could carry at its default")
    void parentsWithEveryKindOfStateLeaveNoFieldAtItsDefault() throws IllegalAccessException {
        GeneratorConfig defaults = GeneratorConfig.defaults();
        GeneratorConfig legacy = everyKindOfState(ObjectFieldStreamPolicy.LEGACY);
        GeneratorConfig independent = everyKindOfState(ObjectFieldStreamPolicy.INDEPENDENT);

        for (Field field : instanceFields()) {
            if (DEFAULT_IN_EVERY_SUCH_PARENT.contains(field.getName())) {
                continue;
            }
            Object value = field.get(defaults);
            assertTrue(
                    !Objects.equals(value, field.get(legacy)) || !Objects.equals(value, field.get(independent)),
                    field.getName() + " keeps its default in both parents");
        }
    }

    @Test
    @DisplayName("the restored parents replay a recipe, and their children do not")
    void restoredParentsReplayARecipe() {
        GeneratorConfig numeric = restored(portableState(ObjectFieldStreamPolicy.LEGACY));
        GeneratorConfig textual = restored(portableState(ObjectFieldStreamPolicy.INDEPENDENT));

        assertTrue(numeric.isRestoredFromRecipe());
        assertTrue(numeric.getStringSeed().isEmpty());
        assertTrue(textual.isRestoredFromRecipe());
        assertEquals(Optional.of("every-kind-of-state"), textual.getStringSeed());
        for (GeneratorConfig parent : List.of(numeric, textual)) {
            GeneratorConfig child = parent.forChildStream("invoice");
            assertFalse(child.isRestoredFromRecipe());
            assertTrue(child.getStringSeed().isEmpty());
        }
    }

    /**
     * Every setting that a portable recipe records, away from its default, with distinct values for fields
     * of one type, so that a copy that mixes two of them up fails.
     */
    private static GeneratorConfig.Builder portableState(ObjectFieldStreamPolicy policy) {
        boolean legacy = policy == ObjectFieldStreamPolicy.LEGACY;
        GeneratorConfig.Builder builder = GeneratorConfig.builder();
        if (legacy) {
            builder.seed(42L);
        } else {
            builder.seed("every-kind-of-state");
        }
        return builder.charset(StandardCharsets.UTF_8)
                .stringLength(3, 9)
                .collectionSize(2, 4)
                .objectMaxDepth(6)
                .objectPoolSize(11)
                .objectOverrideDefaultInitialization(legacy)
                .objectIgnoreErrors(!legacy)
                .advancingConvenienceStreams(!legacy)
                .strictMathGaussian(!legacy)
                .objectReseedBuiltInRandomizers(!legacy)
                .objectConstructionPolicy(ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS)
                .objectDateRange(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31))
                .objectSemanticMode(
                        legacy ? ObjectGenerationSemanticMode.STRICT : ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                .objectFieldStreamPolicy(policy)
                .objectNullProbability(0.25)
                .objectOptionalEmptyProbability(0.5)
                .objectUniqueField("accountId")
                .objectUniquenessMaxAttempts(13)
                .locale(Locale.JAPAN)
                .clock(FIXED_CLOCK)
                .generationProfile("child-stream-profile")
                .safetyPolicy("child-stream-safety")
                .providerDatasetVersion("child-stream-dataset")
                .paymentCardSafetyPolicy(
                        legacy ? PaymentCardSafetyPolicy.STRIPE_SANDBOX : PaymentCardSafetyPolicy.CHECKSUM_VALID)
                .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .businessTaxIdentifierSafetyPolicy(BusinessTaxIdentifierSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .cryptoAddressSafetyPolicy(CryptoAddressSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .securitiesIdentifierSafetyPolicy(SecuritiesIdentifierSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .phoneNumberSafetyPolicy(PhoneNumberSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .nationalIdSafetyPolicy(NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .identityDocumentSafetyPolicy(IdentityDocumentSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .emailDomainPolicy(EmailDomainPolicy.REALISTIC_UNCLASSIFIED)
                .networkSafetyPolicy(NetworkSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .childStreamPolicy(ChildStreamPolicy.INDEPENDENT)
                .germanStreetLayout(true);
    }

    /** The portable settings, and every kind of state that a recipe cannot record, with two entries apiece. */
    private static GeneratorConfig everyKindOfState(ObjectFieldStreamPolicy policy) {
        Generator<String> typeValue = () -> "type";
        Generator<Long> otherTypeValue = () -> 7L;
        Generator<String> fieldValue = () -> "field";
        Generator<String> otherFieldValue = () -> "other field";
        ContextualGenerator<Integer> contextualTypeValue = context -> 3;
        ContextualGenerator<Short> otherContextualTypeValue = context -> (short) 4;
        ContextualGenerator<Integer> contextualFieldValue = context -> 5;
        ContextualGenerator<Integer> otherContextualFieldValue = context -> 6;
        Predicate<Field> startsWithA = field -> field.getName().startsWith("a");
        Predicate<Field> startsWithB = field -> field.getName().startsWith("b");
        Predicate<Field> startsWithC = field -> field.getName().startsWith("c");
        Predicate<Field> startsWithD = field -> field.getName().startsWith("d");
        Predicate<Field> startsWithX = field -> field.getName().startsWith("x");
        Predicate<Class<?>> arrays = Class::isArray;
        return portableState(policy)
                .generationFailureListener(diagnostic -> {})
                .generationReportListener(entry -> {})
                .generationReportParentsFirst(true)
                .objectSemanticRegistry(SemanticFieldRegistry.defaults().toBuilder()
                        .alias("email", "mailbox")
                        .build())
                .objectOverride(String.class, typeValue)
                .objectOverride(Long.class, otherTypeValue)
                .objectOverride(Owner.class, "name", fieldValue)
                .objectOverride(Owner.class, "code", otherFieldValue)
                .objectOverride(Integer.class, contextualTypeValue)
                .objectOverride(Short.class, otherContextualTypeValue)
                .objectOverride(Owner.class, "score", contextualFieldValue)
                .objectOverride(Owner.class, "rank", otherContextualFieldValue)
                .objectOverride(startsWithA, typeValue)
                .objectOverride(startsWithB, fieldValue)
                .objectOverride(startsWithC, contextualTypeValue)
                .objectOverride(startsWithD, contextualFieldValue)
                .objectExclude(startsWithX)
                .objectExcludeField("secret")
                .objectExcludeField("internalNote")
                .objectExcludeType(Long.class)
                .objectExcludeType(arrays)
                .objectSubtype(List.class, ArrayList.class)
                .objectSubtype(Map.class, LinkedHashMap.class)
                .objectSubtype(Set.class, TreeSet.class)
                .registryContext(DataRegistryContext.builder().isolated().build())
                .build();
    }

    private static GeneratorConfig restored(GeneratorConfig.Builder builder) {
        GenerationRecipe recipe = builder.build().getGenerationRecipe().orElseThrow();
        return GenerationRecipe.parse(recipe.serialize()).toGeneratorConfig();
    }

    /** What every child was before: the parent rebuilt with the child's seed. */
    private static GeneratorConfig rebuilt(GeneratorConfig parent, String streamName) {
        return parent.toBuilder()
                .seed(GenerationRecipe.deriveChildSeed(parent.getSeed().getAsLong(), streamName))
                .build();
    }

    private static String recipe(GeneratorConfig config) {
        return config.getGenerationRecipe().orElseThrow().serialize();
    }

    /**
     * Compares every instance field: collections and maps by their elements in iteration order, and any
     * other value with {@code equals}, which for a callback or a registry means the same instance.
     */
    private static void assertSameFields(GeneratorConfig expected, GeneratorConfig actual)
            throws IllegalAccessException {
        List<String> compared = new ArrayList<>();
        for (Field field : instanceFields()) {
            String name = field.getName();
            Object want = field.get(expected);
            Object got = field.get(actual);
            if (want == null || got == null) {
                assertSame(want, got, name);
            } else if (want instanceof Map<?, ?> wantMap && got instanceof Map<?, ?> gotMap) {
                assertEquals(want.getClass(), got.getClass(), name);
                assertEquals(new ArrayList<>(wantMap.entrySet()), new ArrayList<>(gotMap.entrySet()), name);
            } else if (want instanceof Collection<?> wantElements && got instanceof Collection<?> gotElements) {
                assertEquals(want.getClass(), got.getClass(), name);
                assertEquals(new ArrayList<>(wantElements), new ArrayList<>(gotElements), name);
            } else {
                assertEquals(want, got, name);
            }
            compared.add(name);
        }
        assertTrue(
                compared.containsAll(List.of(
                        "seed",
                        "stringSeed",
                        "restoredFromRecipe",
                        "modules",
                        "extensionRegistry",
                        "objectSemanticRegistry")),
                compared::toString);
    }

    /** The instance fields of a configuration; static fields, and any a coverage agent adds, are left out. */
    private static List<Field> instanceFields() {
        List<Field> fields = new ArrayList<>();
        for (Field field : GeneratorConfig.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !field.isSynthetic()) {
                field.setAccessible(true);
                fields.add(field);
            }
        }
        return fields;
    }

    private record CountingModule(AtomicInteger configured) implements KRandomModule {

        @Override
        public String id() {
            return "counting";
        }

        @Override
        public void configure(KRandomModuleContext context) {
            configured.incrementAndGet();
        }
    }

    /** An owner type for field overrides, which name a field by its owner's class name. */
    private static final class Owner {}
}
