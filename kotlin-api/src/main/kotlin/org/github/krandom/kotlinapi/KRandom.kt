/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.kotlinapi

import org.github.krandom.generator.Generators
import org.github.krandom.generator.schema.Field
import org.github.krandom.generator.schema.SchemaValueProvider
import org.github.krandom.generator.user.FullNameGenerator
import java.util.Locale

/**
 * Kotlin entrypoint for Java-backed random generators.
 */
object KRandom {

    fun defaults(): KConfig = KConfig()

    fun int(): KGenerator<Int> = KGenerator(Generators.ofInt().map { it.toInt() })

    fun int(min: Int, max: Int): KGenerator<Int> = KGenerator(Generators.ofInt(min, max).map { it.toInt() })

    fun int(min: Int, max: Int, seed: Long): KGenerator<Int> =
        KGenerator(Generators.ofInt(min, max, seed).map { it.toInt() })

    fun word(): KGenerator<String> = KGenerator(Generators.ofWord())

    fun sentence(): KGenerator<String> = KGenerator(Generators.ofSentence())

    fun url(): KGenerator<String> = KGenerator(Generators.ofUrl())

    fun email(): KGenerator<String> = KGenerator(Generators.ofEmail())

    fun fullName(): KGenerator<String> = KGenerator(Generators.ofFullName())

    fun fullName(config: KConfig): KGenerator<String> = KGenerator(FullNameGenerator(config.toJava()))

    fun <T> fromType(type: Class<T>): KGenerator<T> = KGenerator(Generators.forType(type))

    fun field(): Field = Generators.ofField()

    fun field(locale: Locale): Field = Generators.ofField(locale)

    fun schema(fields: Map<String, SchemaValueProvider>): KSchema = KSchema(Generators.ofSchema(fields))

    fun schema(locale: Locale, fields: Map<String, SchemaValueProvider>): KSchema =
        KSchema(Generators.ofSchema(locale, fields))

    fun schema(config: KConfig, fields: Map<String, SchemaValueProvider>): KSchema =
        KSchema(Generators.ofSchema(config.toJava(), fields))

    fun providerHub(): KProviderHub = KProviderHub(Generators.ofProviderHub())

    fun providerHub(locale: Locale): KProviderHub = KProviderHub(Generators.ofProviderHub(locale))

    fun providerHub(config: KConfig): KProviderHub = KProviderHub(Generators.ofProviderHub(config.toJava()))
}
