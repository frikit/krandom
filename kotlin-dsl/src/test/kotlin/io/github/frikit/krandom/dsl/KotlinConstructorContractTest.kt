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
import org.junit.jupiter.api.Assertions.assertTrue
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
    fun aClassWithAJdkSuperclassFillsOnlyTheFieldsItCanOpen() {
        // Throwable's fields are in java.lang, which is not open to krandom, so they stay as built.
        val errors = krandomList<DomainError>(20) { config { seed(42L) } }

        assertTrue(errors.all { error -> error.message == "boom" })
        assertFalse(errors.any { error -> error.detail == "old" }, errors.map { error -> error.detail }.toString())
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
    fun aGetterConstraintReachesAParameterThatKotlinWritesWithAWildcard() {
        // The field is List<T>, and the parameter, by declaration-site variance, List<? extends T>.
        val parameterType =
            VarianceHost::class.java.declaredConstructors
                .single()
                .parameters[0]
                .parameterizedType
        val outer = krandom<VarianceOuter> { config { collectionSize(3, 3) } }

        assertEquals("java.util.List<? extends T>", parameterType.typeName)
        assertEquals(2, outer.host.items.size, outer.toString())
    }

    @Test
    fun generatingOneTypeLeavesTheValuesOfAnotherAsTheyWere() {
        val before = krandom<SizedCodeHolder> { config { seed(42L) } }
        val code = krandom<SizedCode> { config { seed(42L) } }
        val after = krandom<SizedCodeHolder> { config { seed(42L) } }

        // As the generated type, the value class reads the constraints of its property's field too.
        assertEquals(1000, code.value.length)
        assertEquals(before, after)
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
    fun aNestedClassLeavesItsHostUninitialized() {
        // FailingHost's companion property fails as soon as FailingHost starts.
        val child = krandom<FailingHost.Child> { config { seed(42L) } }

        assertFalse(child.label.isEmpty())
    }

    @Test
    fun aCompanionObjectKeepsItsInheritedState() {
        assertSame(CompanionHost.Companion, krandom<CompanionHost.Companion>())
        assertEquals("kept", CompanionHost.Companion.label)
        // A named companion object is kept in the field of its own name.
        assertSame(NamedCompanionHost.Named, krandom<NamedCompanionHost.Named>())
        assertEquals("kept", NamedCompanionHost.Named.label)
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

class DomainError(
    val code: Long,
) : RuntimeException("boom") {
    var detail: String = "old"
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

data class VarianceHost<T>(
    @get:Size(min = 2, max = 2)
    val items: List<T>,
)

data class VarianceOuter(
    val host: VarianceHost<String>,
)

@JvmInline
value class SizedCode(
    @param:NotBlank
    @field:Size(min = 1000, max = 1000)
    val value: String,
)

data class SizedCodeHolder(
    val code: SizedCode,
)

data class DefaultedName(
    val name: String = "default",
)

data class RequiredName(
    val name: String,
)

class FailingHost {
    data class Child(
        val id: Long,
    ) {
        var label: String = ""
    }

    companion object {
        val unrelated: Any = error("FailingHost must not be initialized")

        // Of Child's own type, but not where Kotlin keeps a companion object.
        val sample: Child = error("FailingHost must not be initialized")
    }
}

open class CompanionState {
    var label: String = "kept"
}

class CompanionHost {
    companion object : CompanionState()
}

class NamedCompanionHost {
    companion object Named : CompanionState()
}

object MutableSingleton {
    var label: String = "kept"
}
