/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.kotest

import io.github.frikit.krandom.generator.Generator
import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.generator.Generators
import io.github.frikit.krandom.generator.`object`.ObjectGenerator
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary

/**
 * Converts a krandom [Generator] into a Kotest [Arb] for property-based testing.
 *
 * Usage:
 * ```kotlin
 * val emailArb: Arb<String> = Generators.ofEmail().toArb()
 *
 * checkAll(emailArb) { email ->
 *     email shouldContain "@"
 * }
 * ```
 */
fun <T> Generator<T>.toArb(): Arb<T> = arbitrary { generate() }

/**
 * Creates a Kotest [Arb] from a krandom [Generator] supplier.
 *
 * The supplier is called once when the [Arb] is created, and the returned
 * generator is reused for subsequent samples. This preserves the generator's
 * normal sequence progression for seeded and stateful generators.
 *
 * Usage:
 * ```kotlin
 * val nameArb = krandomArb { Generators.ofFirstName() }
 * ```
 */
fun <T> krandomArb(factory: () -> Generator<T>): Arb<T> {
    val gen = factory()
    return arbitrary { gen.generate() }
}

/**
 * Creates a Kotest [Arb] that generates random instances of the given class
 * using krandom's [ObjectGenerator].
 *
 * Usage:
 * ```kotlin
 * val personArb = krandomObjectArb<Person>()
 *
 * checkAll(personArb) { person ->
 *     person.firstName shouldNotBe null
 * }
 * ```
 */
inline fun <reified T : Any> krandomObjectArb(
    config: GeneratorConfig = GeneratorConfig.defaults()
): Arb<T> {
    val gen = ObjectGenerator(T::class.java, config)
    return arbitrary { gen.generate() }
}
