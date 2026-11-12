/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.nullness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.ContextualGenerator;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.Krandom;
import io.github.frikit.krandom.generator.base.ConstantGenerator;
import io.github.frikit.krandom.generator.object.ObjectFaker;
import io.github.frikit.krandom.generator.provider.ProviderHub;
import io.github.frikit.krandom.generator.selection.RepeatGenerator;
import io.github.frikit.krandom.generator.selection.SequenceGenerator;
import io.github.frikit.krandom.generator.selection.UniqueGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Null-marked consumer code over the public API that passes {@code null} through. NullAway checks this
 * source as it checks the main sources, so each test compiles only while the API accepts the nullable
 * type arguments it uses, and each asserts that the {@code null} reaches the caller.
 */
@NullMarked
@DisplayName("APIs that pass null through accept nullable type arguments")
class NullableCompositionTest {

    /** A record whose nickname object generation fills unless an override or rule leaves it null. */
    record Profile(@Nullable String nickname) {}

    private static GeneratorConfig seeded() {
        return GeneratorConfig.builder().seed(42L).build();
    }

    @Test
    @DisplayName("ofConstant(null) is a ConstantGenerator<@Nullable String> that returns null")
    void constantOfNull() {
        ConstantGenerator<@Nullable String> none = Generators.ofConstant(null);

        assertNull(none.generate());
        assertNull(none.value());
    }

    @Test
    @DisplayName("repeat(ofNullableBoolean(), n) keeps every null its source generates")
    void repeatKeepsNulls() {
        RepeatGenerator<@Nullable Boolean> flags = Generators.repeat(Generators.ofNullableBoolean(seeded()), 30);
        List<@Nullable Boolean> values = flags.generate();
        List<@Nullable Boolean> unseeded =
                Generators.repeat(Generators.ofNullableBoolean(), 3).generate();

        assertEquals(30, values.size());
        assertTrue(values.contains(null), values::toString);
        assertEquals(3, unseeded.size());
        assertEquals(Arrays.asList(null, null), Generators.repeat(none(), 2).generate());
    }

    @Test
    @DisplayName("unique over nullable booleans returns true, false and null once each, then fails")
    void uniqueReturnsNullOnce() {
        ProviderHub hub = Generators.ofProviderHub();
        BiPredicate<@Nullable Boolean, @Nullable Boolean> same = Objects::equals;
        List<UniqueGenerator<@Nullable Boolean>> generators = new ArrayList<>();
        generators.add(Generators.unique(Generators.ofNullableBoolean(seeded())));
        generators.add(Generators.unique(Generators.ofNullableBoolean(seeded()), 50));
        generators.add(Generators.unique(Generators.ofNullableBoolean(seeded()), same));
        generators.add(hub.unique(Generators.ofNullableBoolean(seeded())));
        generators.add(hub.unique(Generators.ofNullableBoolean(seeded()), 50));
        generators.add(hub.unique(Generators.ofNullableBoolean(seeded()), same));

        for (UniqueGenerator<@Nullable Boolean> flags : generators) {
            Set<@Nullable Boolean> drawn = new HashSet<>();
            for (int i = 0; i < 3; i++) {
                assertTrue(drawn.add(flags.generate()), drawn::toString);
            }
            assertTrue(drawn.contains(null), drawn::toString);
            assertThrows(IllegalStateException.class, flags::generate);
        }
    }

    @Test
    @DisplayName("sequence keeps the nulls its sources generate")
    void sequenceKeepsSourceNulls() {
        List<Generator<@Nullable String>> sources = new ArrayList<>();
        sources.add(none());
        SequenceGenerator<@Nullable String> plain = Generators.sequence(sources, 2, 2);
        SequenceGenerator<@Nullable String> configured = Generators.sequence(seeded(), sources, 2, 2, 0.0);

        assertEquals(Arrays.asList(null, null), plain.generate());
        assertEquals(Arrays.asList(null, null), configured.generate());
    }

    @Test
    @DisplayName("threadLocal returns the null of each thread's generator")
    void threadLocalReturnsNull() {
        Generator<@Nullable String> local = Generators.threadLocal(NullableCompositionTest::none);

        assertNull(local.generate());
    }

    @Test
    @DisplayName("a contextual field or predicate override that returns null leaves the field null")
    void contextualOverridesLeaveFieldNull() {
        ContextualGenerator<@Nullable String> nothing = context -> null;
        GeneratorConfig byName = GeneratorConfig.builder()
                .seed(42L)
                .objectOverride(Profile.class, "nickname", nothing)
                .build();
        GeneratorConfig byPredicate = GeneratorConfig.builder()
                .seed(42L)
                .objectOverride(field -> field.getName().equals("nickname"), nothing)
                .build();

        assertNotNull(Generators.ofObject(Profile.class, seeded()).generate().nickname());
        assertNull(Generators.ofObject(Profile.class, byName).generate().nickname());
        assertNull(Generators.ofObject(Profile.class, byPredicate).generate().nickname());
    }

    @Test
    @DisplayName("a faker rule, context rule or dependent rule that returns null leaves the field null")
    void fakerRulesLeaveFieldNull() {
        ContextualGenerator<@Nullable String> nothing = context -> null;

        assertNotNull(new ObjectFaker<>(Profile.class, seeded()).generate().nickname());
        assertNull(new ObjectFaker<>(Profile.class, seeded())
                .ruleFor("nickname", none())
                .generate()
                .nickname());
        assertNull(new ObjectFaker<>(Profile.class, seeded())
                .ruleForContext("nickname", nothing)
                .generate()
                .nickname());
        assertNull(new ObjectFaker<>(Profile.class, seeded())
                .<@Nullable String>ruleFor("nickname", profile -> null)
                .generate()
                .nickname());
    }

    @Test
    @DisplayName("Krandom.pick returns a null among its values")
    void pickReturnsNull() {
        Krandom krandom = Krandom.seeded("nullable pick");
        List<@Nullable String> onlyNull = Arrays.asList((String) null);

        assertNull(krandom.<@Nullable String>pick(null, null));
        assertNull(krandom.pick(onlyNull));
    }

    private static ConstantGenerator<@Nullable String> none() {
        return Generators.ofConstant(null);
    }
}
