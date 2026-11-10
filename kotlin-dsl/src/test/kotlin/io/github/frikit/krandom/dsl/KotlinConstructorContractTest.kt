/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.dsl

import io.github.frikit.krandom.generator.Generator
import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.generator.`object`.ObjectGenerator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

/**
 * The contract of a Kotlin class built through its primary constructor: its other properties are filled
 * as fields, and its constructor parameters take the constraints and overrides of their properties.
 */
class KotlinConstructorContractTest {
    @Test
    fun aRuleForABodyPropertyReplacesItsInitializer() {
        val user = krandom<BodyPropertyUser> { rule(BodyPropertyUser::name) { "new" } }

        assertEquals("new", user.name)
    }

    @Test
    fun aBodyPropertyIsGeneratedAsAField() {
        val users = krandomList<BodyPropertyUser>(200) { config { seed(42L) } }

        // The DSL replaces initializers with generated values, as it does for a class built field by field.
        assertFalse(users.any { user -> user.name == "old" }, users.map { user -> user.name }.toString())
    }

    @Test
    fun theJavaApiKeepsABodyInitializerUnlessAFieldOverrideTargetsIt() {
        val kept = ObjectGenerator(BodyPropertyUser::class.java).generate()
        val replaced =
            ObjectGenerator(
                BodyPropertyUser::class.java,
                GeneratorConfig
                    .builder()
                    .objectOverride(BodyPropertyUser::class.java, "name", Generator { "new" })
                    .build(),
            ).generate()

        assertEquals("old", kept.name)
        assertEquals("new", replaced.name)
    }

    @Test
    fun aLateinitBodyPropertyIsSet() {
        val user = ObjectGenerator(LateinitUser::class.java).generate()

        assertFalse(user.name.isEmpty())
    }

    @Test
    fun anObjectSingletonKeepsItsState() {
        assertSame(MutableSingleton, krandom<MutableSingleton>())
        assertEquals("kept", MutableSingleton.label)
    }
}

data class BodyPropertyUser(
    val id: Long,
) {
    var name: String = "old"
}

data class LateinitUser(
    val id: Long,
) {
    lateinit var name: String
}

object MutableSingleton {
    var label: String = "kept"
}
