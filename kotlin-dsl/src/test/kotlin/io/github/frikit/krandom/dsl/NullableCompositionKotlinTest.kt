/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.dsl

import io.github.frikit.krandom.generator.ContextualGenerator
import io.github.frikit.krandom.generator.Generator
import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.generator.Generators
import io.github.frikit.krandom.generator.Krandom
import io.github.frikit.krandom.generator.base.ConstantGenerator
import io.github.frikit.krandom.generator.`object`.ObjectFaker
import io.github.frikit.krandom.generator.`object`.PropertySelector
import io.github.frikit.krandom.generator.schema.SchemaValueProvider
import io.github.frikit.krandom.generator.selection.PickGenerator
import io.github.frikit.krandom.generator.selection.RepeatGenerator
import io.github.frikit.krandom.generator.selection.SequenceGenerator
import io.github.frikit.krandom.generator.selection.UniqueGenerator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * What Kotlin sees, through their JSpecify annotations, of the core APIs that pass null through: each takes
 * a nullable type argument, so every declaration below compiles only while core allows it, and the null
 * reaches the caller.
 */
class NullableCompositionKotlinTest {
    class Profile {
        var nickname: String? = null
    }

    private val seeded: GeneratorConfig = GeneratorConfig.builder().seed(42L).build()

    @Test
    fun aPropertySelectorTakesANullableProperty() {
        val nickname: PropertySelector<Profile, String?> = PropertySelector { profile -> profile.nickname }

        assertNull(nickname.apply(Profile()))
    }

    @Test
    fun repeatKeepsTheNullsOfANullableSource() {
        val flags: RepeatGenerator<Boolean?> = Generators.repeat(Generators.ofNullableBoolean(seeded), 30)
        val values: List<Boolean?> = flags.generate()

        assertEquals(30, values.size)
        assertTrue(null in values, values.toString())
    }

    @Test
    fun constantUniqueSequenceAndThreadLocalTakeANullableType() {
        val none: ConstantGenerator<String?> = Generators.ofConstant(null)
        val unique: UniqueGenerator<Boolean?> = Generators.unique(Generators.ofNullableBoolean(seeded))
        val sequence: SequenceGenerator<String?> = Generators.sequence(listOf(none), 2, 2)
        val local: Generator<String?> = Generators.threadLocal { Generators.ofConstant<String?>(null) }

        assertNull(none.generate())
        assertEquals(setOf(true, false, null), List(3) { unique.generate() }.toSet())
        assertEquals(listOf(null, null), sequence.generate())
        assertNull(local.generate())
    }

    @Test
    fun anOverrideOrRuleThatReturnsNullLeavesTheFieldNull() {
        val nothing: ContextualGenerator<String?> = ContextualGenerator { null }
        val overridden =
            GeneratorConfig
                .builder()
                .seed(42L)
                .objectOverride(Profile::class.java, "nickname", nothing)
                .build()

        assertNotNull(Generators.ofObject(Profile::class.java, seeded).generate().nickname)
        assertNull(Generators.ofObject(Profile::class.java, overridden).generate().nickname)
        assertNull(
            ObjectFaker(Profile::class.java, seeded)
                .ruleFor("nickname", Generators.ofConstant<String?>(null))
                .generate()
                .nickname,
        )
        assertNull(ObjectFaker(Profile::class.java, seeded).ruleForContext("nickname", nothing).generate().nickname)
    }

    @Test
    fun aSchemaIsAGeneratorOfNullableRecords() {
        val profiles =
            Generators.ofSchema(
                seeded,
                linkedMapOf("name" to SchemaValueProvider { "Ada" }, "nickname" to SchemaValueProvider { null }),
            )
        val records: Generator<Map<String, Any?>> = profiles
        val generated: Map<String, Any?> = profiles.generate()
        val repeated: RepeatGenerator<Map<String, Any?>> = Generators.repeat(profiles, 2)

        assertEquals("Ada", generated["name"])
        assertTrue("nickname" in generated)
        assertNull(generated["nickname"])
        assertNull(records.generate()["nickname"])
        assertEquals(listOf(null, null), repeated.generate().map { it["nickname"] })
    }

    @Test
    fun theVarargsPickReturnsANullAmongItsValues() {
        val fromArray: String? = Krandom.seeded("nullable pick").pick<String?>(null, null)

        assertNull(fromArray)
    }

    @Test
    fun pickOfAListReturnsAnElement() {
        val krandom = Krandom.seeded("list pick")
        // A list that holds null binds to pick(List), which returns one of its elements.
        val withNull: String? = krandom.pick(listOf("a", null))
        // The varargs pick still takes the list as one value when its type argument is given.
        val whole: List<String?> = krandom.pick<List<String?>>(listOf("a", null))
        val element: String = krandom.pick(listOf("a", "b"))
        val picks: PickGenerator<String?> = Generators.pick(listOf("a", null))
        val onlyNull: PickGenerator<String?> = Generators.pick(listOf(null), seeded)

        assertTrue(withNull == "a" || withNull == null, withNull.toString())
        assertEquals(listOf("a", null), whole)
        assertTrue(element == "a" || element == "b", element)
        assertTrue(picks.generate() in listOf("a", null))
        assertNull(onlyNull.generate())
    }
}
