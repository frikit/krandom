/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class ProfileRollbackTest {
    public static class Fixture {
        public String name;
        public String other;
        public int age;
        public String derived;
    }

    private GeneratorConfig config() {
        return GeneratorConfig.builder().seed(42).clock(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC))
            .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY).build();
    }

    @Test
    void failedNestedProfileCanBeRetriedWithoutLeakingConfiguration() {
        AtomicBoolean fail = new AtomicBoolean(true);
        ObjectFaker<Fixture> faker = new ObjectFaker<>(Fixture.class, config())
            .profile("nested", f -> f.ruleForContext("other", context -> "nested"))
            .profile("retry", f -> {
                f.useProfile("nested").ruleFor("name", () -> "fixed")
                    .ruleFor("derived", value -> value.name + "!")
                    .ignore("age").include("name", "other", "derived")
                    .afterGenerate(value -> value.age = 7).strict();
                f.profile("created", value -> value.ignore("age"));
                if (fail.getAndSet(false)) throw new IllegalStateException("failed");
            });
        assertThrows(IllegalStateException.class, () -> faker.useProfile("retry"));
        assertThrows(IllegalArgumentException.class, () -> faker.useProfile("created"));
        Fixture untouched = faker.generate();
        Fixture expected = new ObjectFaker<>(Fixture.class, config()).generate();
        assertEquals(expected.name, untouched.name);
        assertEquals(expected.age, untouched.age);
        assertEquals(expected.other, untouched.other);
        assertEquals(expected.derived, untouched.derived);
        Fixture fixed = faker.useProfile("retry").generate();
        assertEquals("fixed", fixed.name);
        assertEquals("nested", fixed.other);
        assertEquals("fixed!", fixed.derived);
        assertEquals(7, fixed.age);
    }

    @Test
    void failedProfilePreservesPreviouslyGeneratedSequenceAndRules() {
        ObjectFaker<Fixture> faker = new ObjectFaker<>(Fixture.class, config()).ruleFor("name", () -> "kept");
        ObjectFaker<Fixture> control = new ObjectFaker<>(Fixture.class, config()).ruleFor("name", () -> "kept");
        assertEquals(control.generate().age, faker.generate().age);
        faker.profile("bad", f -> { f.ruleFor("other", () -> "discarded"); throw new AssertionError("failed"); });
        assertThrows(AssertionError.class, () -> faker.useProfile("bad"));
        Fixture next = faker.generate();
        Fixture expected = control.generate();
        assertEquals(expected.age, next.age);
        assertEquals(expected.other, next.other);
        assertEquals("kept", next.name);
    }
}
