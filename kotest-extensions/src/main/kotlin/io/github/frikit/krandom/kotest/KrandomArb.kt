/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.kotest

import io.github.frikit.krandom.generator.Generator
import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.generator.GenerationRecipe
import io.github.frikit.krandom.generator.`object`.ObjectGenerator
import io.kotest.property.Arb
import io.kotest.property.RandomSource
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
@Deprecated(
    message = "This bridge reuses mutable generator state. Use krandomArb { config -> ... } instead.",
    replaceWith = ReplaceWith("krandomArb { config -> createGenerator(config) }")
)
fun <T> Generator<T>.toArb(): Arb<T> = arbitrary { generate() }

/**
 * Creates a Kotest [Arb] from a factory that receives a fresh, host-seeded kRandom configuration
 * for every sample.
 *
 * The [config] supplies locale, clock, safety, and object-generation settings. Kotest's
 * [RandomSource] supplies the per-case random draw, so rerunning a Kotest seed reproduces the same
 * sequence without sharing mutable generator state between cases.
 *
 * @throws IllegalArgumentException when [config] uses a caller-owned, secure, callback-backed, or
 * custom-registry random source that cannot be converted into a portable seed-owned configuration
 */
fun <T> krandomArb(
    config: GeneratorConfig,
    factory: (GeneratorConfig) -> Generator<T>
): Arb<T> = arbitrary { randomSource ->
    factory(config.forKotestSample(randomSource)).generate()
}

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
@Deprecated(
    message = "This bridge reuses mutable generator state. Use krandomArb(config) { sampleConfig -> ... } instead.",
    replaceWith = ReplaceWith("krandomArb(GeneratorConfig.defaults()) { config -> createGenerator(config) }")
)
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
@Deprecated(
    message = "This bridge reuses mutable generator state. Use krandomReplayObjectArb instead.",
    replaceWith = ReplaceWith("krandomReplayObjectArb<T>(config)")
)
inline fun <reified T : Any> krandomObjectArb(
    config: GeneratorConfig = GeneratorConfig.defaults()
): Arb<T> {
    val generator = ObjectGenerator(T::class.java, config)
    return arbitrary { generator.generate() }
}

/**
 * Creates a replay-safe Kotest [Arb] for objects by deriving a fresh kRandom configuration from
 * every host [RandomSource] draw.
 */
inline fun <reified T : Any> krandomReplayObjectArb(
    config: GeneratorConfig = GeneratorConfig.defaults()
): Arb<T> = arbitrary { randomSource ->
    ObjectGenerator(T::class.java, config.forKotestSample(randomSource)).generate()
}

@PublishedApi
internal fun GeneratorConfig.forKotestSample(randomSource: RandomSource): GeneratorConfig {
    val portable = try {
        toBuilder().seed(0L).build()
    } catch (exception: IllegalStateException) {
        throw IllegalArgumentException(
            "Kotest integration requires a seed-owned GeneratorConfig; caller-owned, secure, " +
                "factory-backed, and custom-registry random sources are not replayable",
            exception
        )
    }
    val recipe = portable.generationRecipe.orElseThrow {
        IllegalArgumentException("Kotest integration requires a portable seed-owned GeneratorConfig")
    }
    val hostDraw = randomSource.random.nextLong()
    val childSeed = GenerationRecipe.deriveChildSeed(
        recipe.seed,
        "kotest|source=${randomSource.seed}|draw=$hostDraw"
    )
    return portable.toBuilder().seed(childSeed).build()
}
