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
import io.github.frikit.krandom.generator.base.DoubleGenerator
import io.github.frikit.krandom.generator.base.IntGenerator
import io.github.frikit.krandom.generator.base.LongGenerator
import io.github.frikit.krandom.generator.selection.PickGenerator
import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.Shrinker
import io.kotest.property.arbitrary.DoubleShrinker
import io.kotest.property.arbitrary.IntShrinker
import io.kotest.property.arbitrary.LongShrinker
import io.kotest.property.arbitrary.arbitrary
import kotlin.math.nextDown

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

/**
 * Creates a shrinking-aware Kotest [Arb] of ints in kRandom's half-open range [[min], [max]).
 *
 * Kotest's [RandomSource] owns determinism: replaying a failing Kotest seed reproduces the same
 * values. Edge cases cover the attainable bounds plus `-1`, `0`, and `1` when they fall inside the
 * range, and shrinking proposes only in-range values.
 *
 * @throws IllegalArgumentException when [min] equals [max]
 */
fun krandomIntArb(min: Int, max: Int): Arb<Int> {
    require(min != max) { "min and max must differ, both were: $min" }
    val lo = minOf(min, max)
    val hi = maxOf(min, max) - 1
    val edges = listOf(lo, hi, -1, 0, 1).filter { it in lo..hi }.distinct()
    return arbitrary(edges, IntShrinker(lo..hi)) { randomSource ->
        IntGenerator(min, max, kotestChildSeed(randomSource)).generate()
    }
}

/**
 * Creates a shrinking-aware Kotest [Arb] of longs in kRandom's half-open range [[min], [max]).
 *
 * Kotest's [RandomSource] owns determinism: replaying a failing Kotest seed reproduces the same
 * values. Edge cases cover the attainable bounds plus `-1`, `0`, and `1` when they fall inside the
 * range, and shrinking proposes only in-range values.
 *
 * @throws IllegalArgumentException when [min] equals [max]
 */
fun krandomLongArb(min: Long, max: Long): Arb<Long> {
    require(min != max) { "min and max must differ, both were: $min" }
    val lo = minOf(min, max)
    val hi = maxOf(min, max) - 1
    val edges = listOf(lo, hi, -1L, 0L, 1L).filter { it in lo..hi }.distinct()
    return arbitrary(edges, LongShrinker(lo..hi)) { randomSource ->
        LongGenerator(min, max, kotestChildSeed(randomSource)).generate()
    }
}

/**
 * Creates a shrinking-aware Kotest [Arb] of doubles in kRandom's half-open range [[min], [max]).
 *
 * Kotest's [RandomSource] owns determinism: replaying a failing Kotest seed reproduces the same
 * values. Edge cases cover the attainable bounds plus `-1.0`, `0.0`, and `1.0` when they fall
 * inside the range, and shrink candidates outside the range are discarded so a reported
 * counterexample is always a value this [Arb] can generate.
 *
 * @throws IllegalArgumentException when [min] equals [max]
 */
fun krandomDoubleArb(min: Double, max: Double): Arb<Double> {
    require(min != max) { "min and max must differ, both were: $min" }
    val lo = minOf(min, max)
    val hiExclusive = maxOf(min, max)
    val edges = listOf(lo, hiExclusive.nextDown(), -1.0, 0.0, 1.0)
        .filter { it >= lo && it < hiExclusive }
        .distinct()
    val shrinker = Shrinker<Double> { value ->
        DoubleShrinker.shrink(value).filter { it >= lo && it < hiExclusive }
    }
    return arbitrary(edges, shrinker) { randomSource ->
        DoubleGenerator(min, max, kotestChildSeed(randomSource)).generate()
    }
}

/**
 * Creates a shrinking-aware Kotest [Arb] that picks one element from [source].
 *
 * Kotest's [RandomSource] owns determinism: replaying a failing Kotest seed reproduces the same
 * selections. The first element is the edge case, and shrinking proposes only elements that appear
 * earlier in [source], so "smaller" means "closer to the front of the list".
 *
 * @throws IllegalArgumentException when [source] is empty
 */
fun <T : Any> krandomPickArb(source: List<T>): Arb<T> {
    require(source.isNotEmpty()) { "source must not be empty" }
    val elements = source.toList()
    return arbitrary(listOf(elements.first()), PickShrinker(elements)) { randomSource ->
        PickGenerator(elements, kotestChildSeed(randomSource)).generate()
    }
}

private class PickShrinker<T>(private val source: List<T>) : Shrinker<T> {
    override fun shrink(value: T): List<T> {
        val index = source.indexOf(value)
        if (index <= 0) {
            return emptyList()
        }
        return listOf(source[0], source[index / 2], source[index - 1])
            .distinct()
            .filter { source.indexOf(it) < index }
    }
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

private fun kotestChildSeed(randomSource: RandomSource): Long {
    val hostDraw = randomSource.random.nextLong()
    return GenerationRecipe.deriveChildSeed(
        0L,
        "kotest|source=${randomSource.seed}|draw=$hostDraw"
    )
}
