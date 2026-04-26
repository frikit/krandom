/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.dsl

import io.github.frikit.krandom.generator.Generator
import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.generator.`object`.ObjectGenerator

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
     * Registers a field-level override by field name.
     *
     * ```kotlin
     * rule("firstName") { "Ada" }
     * ```
     */
    fun <V> rule(fieldName: String, generator: () -> V) {
        @Suppress("UNCHECKED_CAST")
        fieldOverrides[fieldName] = Generator { generator() } as Generator<*>
    }

    /**
     * Registers a type-level override.
     *
     * ```kotlin
     * ruleForType(String::class.java) { "fixed" }
     * ```
     */
    fun <V> ruleForType(clazz: Class<V>, generator: () -> V) {
        @Suppress("UNCHECKED_CAST")
        typeOverrides[clazz] = Generator { generator() } as Generator<*>
    }

    /**
     * Registers a type-level override using reified type.
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
     * Excludes a field by name from generation.
     */
    fun exclude(fieldName: String) {
        configBuilder.objectExcludeField(fieldName)
    }

    @PublishedApi
    internal fun build(): ObjectGenerator<T> {
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
