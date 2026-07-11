/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.dsl

import io.github.frikit.krandom.generator.Generator
import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.generator.`object`.ObjectGenerator
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
 * Builder for configuring krandom object generation via DSL.
 */
@KrandomDslMarker
class KrandomBuilder<T : Any>(private val type: Class<T>) {

    private var configBuilder = GeneratorConfig.builder()
        .objectOverrideDefaultInitialization(true)
    private val fieldOverrides = mutableMapOf<String, Generator<*>>()
    private val typeOverrides = mutableMapOf<Class<*>, Generator<*>>()

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
     * This string form is the compatibility bridge for fields that cannot be referenced as a
     * Kotlin property; prefer the type-safe [rule] overload with a property reference. Unknown
     * field names fail when the generator is built.
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
     * Excludes a property from generation through a type-safe reference.
     */
    fun exclude(property: KProperty1<T, *>) {
        exclude(property.name)
    }

    /**
     * Excludes a field by name from generation.
     */
    fun exclude(fieldName: String) {
        configBuilder.objectExcludeField(fieldName)
    }

    @PublishedApi
    internal fun build(): ObjectGenerator<T> {
        val known = knownFieldNames()
        val unknown = fieldOverrides.keys.filterNot { it in known }
        require(unknown.isEmpty()) {
            "Unknown field rule(s) ${unknown.sorted()} for ${type.name}; " +
                "known fields: ${known.sorted()}"
        }
        for ((fieldName, generator) in fieldOverrides) {
            configBuilder.objectOverride(type, fieldName, generator)
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

    private fun knownFieldNames(): Set<String> {
        val names = mutableSetOf<String>()
        var current: Class<*>? = type
        while (current != null && current != Any::class.java) {
            current.declaredFields.forEach { field -> names += field.name }
            current = current.superclass
        }
        // Kotlin metadata adds primary-constructor parameters and member properties that have no
        // backing Java field visible above; reflection over synthetic/local classes can fail, and
        // that supplementary lookup must not block validation of the Java field names.
        runCatching {
            type.kotlin.primaryConstructor?.parameters?.forEach { parameter ->
                parameter.name?.let { names += it }
            }
            type.kotlin.memberProperties.forEach { property -> names += property.name }
        }
        return names
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
}

/**
 * DSL marker to prevent scope leaking.
 */
@DslMarker
annotation class KrandomDslMarker
