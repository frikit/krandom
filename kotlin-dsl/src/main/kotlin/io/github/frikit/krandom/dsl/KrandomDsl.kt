/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.dsl

import io.github.frikit.krandom.generator.Generator
import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.generator.`object`.ObjectGenerator
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Entry point for the krandom Kotlin DSL.
 *
 * ```kotlin
 * val person = krandom<Person> {
 *     config { seed(42L) }
 *     rule("firstName") { "Ada" }
 *     rule("age") { 30 }
 * }
 * ```
 */
inline fun <reified T : Any> krandom(block: KrandomBuilder<T>.() -> Unit = {}): T {
    val builder = KrandomBuilder(T::class.java)
    builder.block()
    return builder.build().generate()
}

/**
 * Generates a list of random instances using the krandom DSL.
 *
 * ```kotlin
 * val people = krandomList<Person>(10) {
 *     rule("firstName") { "Ada" }
 * }
 * ```
 */
inline fun <reified T : Any> krandomList(count: Int, block: KrandomBuilder<T>.() -> Unit = {}): List<T> {
    val builder = KrandomBuilder(T::class.java)
    builder.block()
    return builder.build().generateList(count)
}

/**
 * Creates a reusable generator using the krandom DSL.
 *
 * ```kotlin
 * val gen = krandomGenerator<Person> {
 *     config { seed(42L) }
 * }
 * val person = gen.generate()
 * ```
 */
inline fun <reified T : Any> krandomGenerator(block: KrandomBuilder<T>.() -> Unit = {}): Generator<T> {
    val builder = KrandomBuilder(T::class.java)
    builder.block()
    return builder.build()
}

/**
 * Builds a standalone [GeneratorConfig] with the DSL's [ConfigScope].
 *
 * The returned configuration exposes its portable replay recipe through
 * [GeneratorConfig.getGenerationRecipe] when it is seed-owned:
 *
 * ```kotlin
 * val config = krandomConfig { seed(42L) }
 * val recipe = config.generationRecipe.orElseThrow().serialize()
 * ```
 */
fun krandomConfig(block: ConfigScope.() -> Unit): GeneratorConfig {
    val builder = GeneratorConfig.builder()
    ConfigScope(builder).block()
    return builder.build()
}

/**
 * Builder for configuring krandom object generation via DSL.
 *
 * Defaults match the Java `ObjectGenerator` with one intentional, documented difference:
 * the DSL enables `objectOverrideDefaultInitialization` so field rules and generated values
 * replace Kotlin/Java field initializers; without it, `rule(...)` on an initialized property
 * would be ignored, which surprises DSL users.
 */
@KrandomDslMarker
class KrandomBuilder<T : Any>(private val type: Class<T>) {

    private var configBuilder = GeneratorConfig.builder()
        .objectOverrideDefaultInitialization(true)
    private val fieldOverrides = mutableMapOf<String, Generator<*>>()
    private val typeOverrides = mutableMapOf<Class<*>, Generator<*>>()
    private val exclusions = linkedSetOf<String>()

    /**
     * Configures the underlying [GeneratorConfig].
     */
    fun config(block: ConfigScope.() -> Unit) {
        ConfigScope(configBuilder).block()
    }

    /**
     * Registers a type-safe field-level override through a property reference.
     *
     * The property reference survives renames and is checked by the Kotlin compiler, so the rule
     * value type always matches the property type.
     *
     * ```kotlin
     * rule(Person::firstName) { "Ada" }
     * ```
     *
     * @throws IllegalArgumentException when a rule for the same property is already registered
     */
    fun <V> rule(property: KProperty1<T, V>, generator: () -> V) {
        registerFieldRule(property.name, Generator { generator() })
    }

    /**
     * Registers a field-level override by field name.
     *
     * Use this string form only for fields that cannot be referenced as a Kotlin property;
     * prefer the type-safe [rule] overload with a property reference. Unknown field names fail
     * when the generator is built.
     *
     * ```kotlin
     * rule("firstName") { "Ada" }
     * ```
     *
     * @throws IllegalArgumentException when a rule for the same field is already registered
     */
    fun <V> rule(fieldName: String, generator: () -> V) {
        registerFieldRule(fieldName, Generator { generator() })
    }

    private fun registerFieldRule(fieldName: String, generator: Generator<*>) {
        require(!fieldOverrides.containsKey(fieldName)) {
            "Duplicate rule for field '$fieldName' of ${type.name}"
        }
        fieldOverrides[fieldName] = generator
    }

    /**
     * Registers a type-level override.
     *
     * When [clazz] is a primitive or wrapper type, the override is registered for both
     * forms, so an override for `Int` matches both `int` and `Integer` fields.
     *
     * ```kotlin
     * ruleForType(String::class.java) { "fixed" }
     * ```
     */
    fun <V> ruleForType(clazz: Class<V>, generator: () -> V) {
        require(!typeOverrides.containsKey(clazz)) {
            "Duplicate type rule for ${clazz.name}"
        }
        @Suppress("UNCHECKED_CAST")
        typeOverrides[clazz] = Generator { generator() } as Generator<*>
    }

    /**
     * Registers a type-level override using reified type.
     *
     * Primitive/wrapper symmetry applies: `ruleForType<Int>` matches both `int` and
     * `Integer` fields.
     *
     * ```kotlin
     * ruleForType<String> { "fixed" }
     * ```
     */
    inline fun <reified V> ruleForType(noinline generator: () -> V) {
        ruleForType(V::class.java, generator)
    }

    /**
     * Sets the maximum object nesting depth.
     */
    fun maxDepth(depth: Int) {
        configBuilder.objectMaxDepth(depth)
    }

    /**
     * Excludes a property of the generated type from generation through a type-safe reference.
     *
     * The exclusion is scoped to the generated (root) type's field: a nested object's field with
     * the same name is still generated. A field inherited from a superclass is matched by that
     * superclass field wherever it appears in the object graph. Computed and delegated properties
     * have no backing field to exclude and are rejected when the generator is built.
     */
    fun exclude(property: KProperty1<T, *>) {
        exclude(property.name)
    }

    /**
     * Excludes a field of the generated type by name from generation.
     *
     * Like the property-reference form, the exclusion is scoped to the generated (root) type's
     * field; unknown names fail when the generator is built.
     */
    fun exclude(fieldName: String) {
        exclusions += fieldName
    }

    @PublishedApi
    internal fun build(): ObjectGenerator<T> {
        val targets = RuleTargets(type)
        for ((fieldName, generator) in fieldOverrides) {
            configBuilder.objectOverride(targets.ruleOwner(fieldName, "rule"), fieldName, generator)
        }
        for (fieldName in exclusions) {
            val excluded = targets.backingField(fieldName)
            configBuilder.objectExclude { candidate -> candidate == excluded }
        }
        for ((clazz, generator) in typeOverrides) {
            @Suppress("UNCHECKED_CAST")
            val gen = generator as Generator<Any>
            @Suppress("UNCHECKED_CAST")
            configBuilder.objectOverride(clazz as Class<Any>, gen)
            // Register for the primitive/wrapper counterpart so that
            // ruleForType<Int> matches both int and Integer fields.
            val counterpart = primitiveWrapperCounterpart(clazz)
            if (counterpart != null) {
                @Suppress("UNCHECKED_CAST")
                configBuilder.objectOverride(counterpart as Class<Any>, gen)
            }
        }

        return ObjectGenerator(type, configBuilder.build())
    }

    /**
     * Resolves rule and exclusion names against what object generation can actually target: Java
     * fields of the generated type and its superclasses, and Kotlin primary-constructor parameters.
     */
    private class RuleTargets(private val type: Class<*>) {

        private val fields = LinkedHashMap<String, Field>()
        private val constructorParameters = mutableSetOf<String>()
        private val memberPropertiesWithoutField = mutableSetOf<String>()

        init {
            var current: Class<*>? = type
            while (current != null && current != Any::class.java) {
                current.declaredFields
                    .filterNot { field -> Modifier.isStatic(field.modifiers) }
                    .forEach { field -> fields.putIfAbsent(field.name, field) }
                current = current.superclass
            }
            // Reflection over synthetic/local classes can fail; that supplementary Kotlin lookup
            // must not block validation of the Java field names collected above.
            runCatching {
                type.kotlin.primaryConstructor?.parameters?.forEach { parameter ->
                    parameter.name?.let { constructorParameters += it }
                }
                type.kotlin.memberProperties
                    .map { property -> property.name }
                    .filterNot { name -> name in fields || name in constructorParameters }
                    .forEach { name -> memberPropertiesWithoutField += name }
            }
        }

        /**
         * Returns the owner type under which a field rule is looked up: the generated type for its
         * own fields and constructor parameters, or the declaring superclass for inherited fields.
         */
        fun ruleOwner(name: String, kind: String): Class<*> {
            val field = fields[name]
            return when {
                field != null && field.declaringClass == type -> type
                name in constructorParameters -> type
                field != null -> field.declaringClass
                else -> throw unresolved(name, kind)
            }
        }

        /** Returns the backing field an exclusion matches. */
        fun backingField(name: String): Field =
            fields[name] ?: if (name in constructorParameters) {
                throw IllegalArgumentException(
                    "Cannot exclude constructor parameter '$name' of ${type.name}: it has no backing " +
                        "field; declare it as a property or register a rule instead"
                )
            } else {
                throw unresolved(name, "exclusion")
            }

        private fun unresolved(name: String, kind: String): IllegalArgumentException =
            if (name in memberPropertiesWithoutField) {
                IllegalArgumentException(
                    "Property '$name' of ${type.name} has no backing field (computed or delegated) and " +
                        "is not a primary-constructor parameter, so a $kind cannot apply to it"
                )
            } else {
                IllegalArgumentException(
                    "Unknown field $kind [$name] for ${type.name}; known fields: " +
                        (fields.keys + constructorParameters).toSortedSet()
                )
            }
    }

    companion object {
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        private val WRAPPER_TO_PRIMITIVE = mapOf<Class<*>, Class<*>>(
            java.lang.Boolean::class.java to Boolean::class.javaPrimitiveType!!,
            java.lang.Byte::class.java to Byte::class.javaPrimitiveType!!,
            java.lang.Short::class.java to Short::class.javaPrimitiveType!!,
            java.lang.Integer::class.java to Int::class.javaPrimitiveType!!,
            java.lang.Long::class.java to Long::class.javaPrimitiveType!!,
            java.lang.Float::class.java to Float::class.javaPrimitiveType!!,
            java.lang.Double::class.java to Double::class.javaPrimitiveType!!,
            java.lang.Character::class.java to Char::class.javaPrimitiveType!!,
        )

        private val PRIMITIVE_TO_WRAPPER = WRAPPER_TO_PRIMITIVE.entries.associate { (k, v) -> v to k }

        private fun primitiveWrapperCounterpart(clazz: Class<*>): Class<*>? =
            WRAPPER_TO_PRIMITIVE[clazz] ?: PRIMITIVE_TO_WRAPPER[clazz]
    }
}

/**
 * Scope for configuring [GeneratorConfig] properties within the DSL.
 */
@KrandomDslMarker
class ConfigScope(private val builder: GeneratorConfig.Builder) {

    fun seed(seed: Long) {
        builder.seed(seed)
    }

    fun locale(locale: java.util.Locale) {
        builder.locale(locale)
    }

    fun stringLength(min: Int, max: Int) {
        builder.stringLength(min, max)
    }

    fun collectionSize(min: Int, max: Int) {
        builder.collectionSize(min, max)
    }

    fun objectMaxDepth(depth: Int) {
        builder.objectMaxDepth(depth)
    }

    /** Selects the explicit object construction policy (safe by default in v2). */
    fun constructionPolicy(policy: io.github.frikit.krandom.generator.`object`.ObjectConstructionPolicy) {
        builder.objectConstructionPolicy(policy)
    }

    /** Selects opt-in independent object-field streams; requires a numeric or textual seed. */
    fun objectFieldStreamPolicy(policy: io.github.frikit.krandom.generator.`object`.ObjectFieldStreamPolicy) {
        builder.objectFieldStreamPolicy(policy)
    }

    /** Sets a textual seed using the same derivation contract as `GeneratorConfig`. */
    fun seed(seedText: String) {
        builder.seed(seedText)
    }
}

/**
 * DSL marker to prevent scope leaking.
 */
@DslMarker
annotation class KrandomDslMarker
