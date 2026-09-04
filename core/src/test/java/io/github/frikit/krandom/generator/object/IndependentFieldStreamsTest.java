/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.GenerationRecipe;
import io.github.frikit.krandom.generator.extension.KRandomModule;
import io.github.frikit.krandom.generator.extension.KRandomModuleContext;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class IndependentFieldStreamsTest {
    public static class Fixture { public String name; public int age; public Child child; public List<Integer> values; }
    public static class Child { public String name; public int age; }
    public record RecordFixture(String name, int age, List<Integer> values) {}

    private GeneratorConfig legacy() {
        return GeneratorConfig.builder().seed(42).clock(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC))
            .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY).build();
    }
    private GeneratorConfig independent() {
        return legacy().toBuilder().objectFieldStreamPolicy(ObjectFieldStreamPolicy.INDEPENDENT).build();
    }

    @Test
    void unrelatedFieldsRetainTheirStreamsAcrossOverridesExclusionsAndModules() {
        GeneratorConfig config = independent();
        KRandomModule emptyModule = new KRandomModule() {
            public String id() { return "unrelated"; }
            public void configure(KRandomModuleContext context) { }
        };
        for (GeneratorConfig changed : List.of(
                config.toBuilder().objectOverride(Fixture.class, "name", () -> "fixed").build(),
                config.toBuilder().objectExcludeField("name").build(),
                config.toBuilder().install(emptyModule).build(),
                config.toBuilder().objectOverride(Child.class, "name", () -> "nested").build())) {
            ObjectGenerator<Fixture> plain = new ObjectGenerator<>(Fixture.class, config);
            ObjectGenerator<Fixture> custom = new ObjectGenerator<>(Fixture.class, changed);
            for (int i = 0; i < 5; i++) {
                Fixture expected = plain.generate();
                Fixture actual = custom.generate();
                assertEquals(expected.age, actual.age);
                assertEquals(expected.child.age, actual.child.age);
                assertEquals(expected.values, actual.values);
            }
            assertTrue(changed.getGenerationRecipe().isEmpty(), "custom configuration must not claim portable replay");
        }
    }

    @Test
    void recordsKeepIndependentStreamsAndDifferentRecordIndices() {
        GeneratorConfig config = independent();
        List<RecordFixture> original = new ObjectGenerator<>(RecordFixture.class, config).generateList(5);
        List<RecordFixture> custom = new ObjectGenerator<>(RecordFixture.class,
            config.toBuilder().objectOverride(RecordFixture.class, "name", () -> "fixed").build()).generateList(5);
        assertEquals(original.stream().map(RecordFixture::age).toList(), custom.stream().map(RecordFixture::age).toList());
        assertEquals(original.stream().map(RecordFixture::values).toList(), custom.stream().map(RecordFixture::values).toList());
        assertTrue(original.stream().map(RecordFixture::age).distinct().count() > 1);
    }

    @Test
    void defaultsAndExistingRecipesRetainLegacyBehavior() {
        GeneratorConfig config = legacy();
        assertEquals(ObjectFieldStreamPolicy.LEGACY, config.getObjectFieldStreamPolicy());
        GenerationRecipe old = config.getGenerationRecipe().orElseThrow();
        assertFalse(old.getSettings().containsKey("object.field-stream-policy"));
        assertEquals(ObjectFieldStreamPolicy.LEGACY, old.toGeneratorConfig().getObjectFieldStreamPolicy());
        List<RecordFixture> baseline = new ObjectGenerator<>(RecordFixture.class, config).generateList(5);
        assertEquals(baseline, new ObjectGenerator<>(RecordFixture.class, old.toGeneratorConfig()).generateList(5));
        assertEquals(baseline, new ObjectGenerator<>(RecordFixture.class, independent()).generateList(5));
        int original = new ObjectGenerator<>(Fixture.class, config).generate().age;
        int custom = new ObjectGenerator<>(Fixture.class,
            config.toBuilder().objectOverride(Fixture.class, "name", () -> "fixed").build()).generate().age;
        assertNotEquals(original, custom, "legacy non-portable configurations retain their existing sequence");
    }

    @Test
    void recipeRoundTripPreservesExplicitPolicyWithoutChangingAlgorithm() {
        GenerationRecipe recipe = independent().getGenerationRecipe().orElseThrow();
        assertEquals("INDEPENDENT", recipe.getSettings().get("object.field-stream-policy"));
        assertEquals(GenerationRecipe.ALGORITHM, recipe.getAlgorithm());
        GeneratorConfig replay = GenerationRecipe.parse(recipe.serialize()).toGeneratorConfig();
        assertEquals(ObjectFieldStreamPolicy.INDEPENDENT, replay.toBuilder().build().getObjectFieldStreamPolicy());
        assertEquals(new ObjectGenerator<>(RecordFixture.class, independent()).generateList(5),
            new ObjectGenerator<>(RecordFixture.class, replay).generateList(5));
        assertThrows(IllegalArgumentException.class, () -> GenerationRecipe.builder()
            .setting("object.field-stream-policy", "UNKNOWN").build().toGeneratorConfig());
    }

    @Test
    void independentStreamsRequireAnOwnedSeedAndRejectNullPolicy() {
        assertThrows(NullPointerException.class, () -> GeneratorConfig.builder().objectFieldStreamPolicy(null));
        assertThrows(IllegalStateException.class, () -> GeneratorConfig.builder()
            .objectFieldStreamPolicy(ObjectFieldStreamPolicy.INDEPENDENT).build());
        assertThrows(IllegalStateException.class, () -> GeneratorConfig.builder().random(new Random(42))
            .objectFieldStreamPolicy(ObjectFieldStreamPolicy.INDEPENDENT).build());
        assertEquals(ObjectFieldStreamPolicy.INDEPENDENT, GeneratorConfig.builder().seed("text")
            .objectFieldStreamPolicy(ObjectFieldStreamPolicy.INDEPENDENT).build().getObjectFieldStreamPolicy());
    }
}
