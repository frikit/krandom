/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.dsl

import io.github.frikit.krandom.generator.ContextualGenerator
import io.github.frikit.krandom.generator.Generator
import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.generator.`object`.ObjectGenerator
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import java.lang.reflect.Field
import java.util.function.Predicate

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
    fun aParameterMeetsTheConstraintsOfItsPropertyFieldAndGetterToo() {
        val combined = krandomList<ParameterAndFieldConstraints>(100) { config { seed(42L) } }
        val getterOnly = krandomList<GetterConstraint>(100) { config { seed(42L) } }
        val parameterFirst = krandomList<ParameterOverFieldConstraint>(100) { config { seed(42L) } }

        // Kotlin compiles no parameter names here, so only the property names the field and its getter.
        assertFalse(
            ParameterAndFieldConstraints::class.java.declaredConstructors
                .single()
                .parameters[0]
                .isNamePresent,
        )
        assertEquals(setOf(2), combined.map { value -> value.code.length }.toSet(), combined.toString())
        assertFalse(combined.any { value -> value.code.isBlank() }, combined.toString())
        assertEquals(setOf(2), getterOnly.map { value -> value.code.length }.toSet(), getterOnly.toString())
        // Where both declare a constraint of a type, the parameter's applies, as for a Java constructor.
        assertEquals(setOf(3), parameterFirst.map { value -> value.code.length }.toSet(), parameterFirst.toString())
    }

    @Test
    fun aFieldPredicateOverrideReplacesADefaultArgumentAndFillsARequiredOne() {
        val names = Predicate<Field> { field -> field.name == "name" }
        val plain = GeneratorConfig.builder().objectOverride(names, Generator { "predicate" }).build()
        val contextual =
            GeneratorConfig.builder().objectOverride(names, ContextualGenerator { _ -> "contextual" }).build()

        assertEquals("predicate", ObjectGenerator(DefaultedName::class.java, plain).generate().name)
        assertEquals("contextual", ObjectGenerator(DefaultedName::class.java, contextual).generate().name)
        assertEquals("predicate", ObjectGenerator(RequiredName::class.java, plain).generate().name)
        assertEquals("contextual", ObjectGenerator(RequiredName::class.java, contextual).generate().name)
        // Without an override, the default stays.
        assertEquals("default", ObjectGenerator(DefaultedName::class.java).generate().name)
    }

    @Test
    fun anObjectSingletonKeepsItsState() {
        assertSame(MutableSingleton, krandom<MutableSingleton>())
        assertEquals("kept", MutableSingleton.label)
    }

    @Test
    fun aCompanionObjectKeepsItsInheritedState() {
        assertSame(CompanionHost.Companion, krandom<CompanionHost.Companion>())
        assertEquals("kept", CompanionHost.Companion.label)
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

data class ParameterAndFieldConstraints(
    @param:NotBlank
    @field:Size(min = 2, max = 2)
    val code: String,
)

data class GetterConstraint(
    @get:Size(min = 2, max = 2)
    val code: String,
)

data class ParameterOverFieldConstraint(
    @param:Size(min = 3, max = 3)
    @field:Size(min = 2, max = 2)
    val code: String,
)

data class DefaultedName(
    val name: String = "default",
)

data class RequiredName(
    val name: String,
)

open class CompanionState {
    var label: String = "kept"
}

class CompanionHost {
    companion object : CompanionState()
}

object MutableSingleton {
    var label: String = "kept"
}
