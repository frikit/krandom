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
import io.kotest.property.PropTestConfig
import io.kotest.property.RandomSource
import io.kotest.property.Shrinker
import io.kotest.property.arbitrary.DoubleShrinker
import io.kotest.property.arbitrary.IntShrinker
import io.kotest.property.arbitrary.LongShrinker
import io.kotest.property.arbitrary.arbitrary
import kotlin.math.nextDown

/**
 * Creates a Kotest [Arb] from a factory that receives a fresh, host-seeded kRandom configuration
 * for every sample.
 *
 * The [config] supplies locale, clock, safety, and object-generation settings, and its seed (`0`
 * when unseeded) parents every sample seed. Kotest's [RandomSource] supplies the per-case random
 * draw, so rerunning a Kotest seed with the same configuration reproduces the same sequence without
 * sharing mutable generator state between cases.
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
 * Creates a replay-safe Kotest [Arb] for objects by deriving a fresh kRandom configuration from
 * every host [RandomSource] draw, parented by the seed of [config] (`0` when unseeded).
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
 * @throws IllegalArgumentException when [min] is not less than [max]
 */
fun krandomIntArb(min: Int, max: Int): Arb<Int> {
    require(min < max) { "min must be less than max, got: min=$min, max=$max" }
    val lo = min
    val hi = max - 1
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
 * @throws IllegalArgumentException when [min] is not less than [max]
 */
fun krandomLongArb(min: Long, max: Long): Arb<Long> {
    require(min < max) { "min must be less than max, got: min=$min, max=$max" }
    val lo = min
    val hi = max - 1
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
 * @throws IllegalArgumentException when [min] is not less than [max]
 */
fun krandomDoubleArb(min: Double, max: Double): Arb<Double> {
    require(min < max) { "min must be less than max, got: min=$min, max=$max" }
    val lo = min
    val hiExclusive = max
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

/**
 * Returns the portable, value-free kRandom recipe for [config] as used by the Kotest adapters.
 *
 * Together with the Kotest seed printed on failure, this recipe determines the generated values:
 * it carries the parent seed of every sample (the configured seed, or `0` when [config] is
 * unseeded) and the configuration (locale, clock, profile, safety and construction policies)
 * needed to reconstruct the same [GeneratorConfig] on another machine. A live clock is read when
 * this function is called; snapshot the configuration first for temporal replay.
 *
 * @throws IllegalArgumentException when [config] cannot be converted into a portable
 * seed-owned configuration
 */
fun krandomKotestRecipe(config: GeneratorConfig = GeneratorConfig.defaults()): String =
    config.kotestPortableRecipe().serializeForDiagnostics()

/**
 * Runs [io.kotest.property.checkAll] over [arb] and, when the property fails, rethrows the
 * assertion error with the kRandom recipe of [config] appended alongside Kotest's own seed
 * report, so a CI failure carries both replay halves.
 *
 * The recipe's clock is read from [config] when the failure is reported, which the appended
 * header states. For temporal replay prefer the `checkAllWithRecipe(config, arbFactory)` overload,
 * which snapshots the clock once for sampling and the recipe, or call
 * [GeneratorConfig.snapshotClock] before creating the Arb and pass that same configuration here.
 * This helper cannot recover the clock used earlier by an independently constructed Arb.
 */
suspend fun <A> checkAllWithRecipe(
    config: GeneratorConfig,
    arb: Arb<A>,
    property: suspend (A) -> Unit
) {
    try {
        io.kotest.property.checkAll(arb) { value -> property(value) }
    } catch (failure: AssertionError) {
        throw AssertionError(
            (failure.message ?: "Property failed") +
                "\n\nkRandom recipe (configuration portion; combine with the Kotest seed above; clock read " +
                "when the failure was reported unless the configuration was snapshotted before sampling):\n" +
                krandomKotestRecipe(config),
            failure
        )
    }
}

/**
 * Runs [io.kotest.property.checkAll] with one clock snapshot shared by sampling and the failure
 * recipe.
 *
 * The clock of [config] is captured once with [GeneratorConfig.snapshotClock]; [arbFactory]
 * receives that snapshot, so every sample sees the same instant, and a failing property is rethrown
 * with the recipe of the same snapshot appended below Kotest's seed report. Replaying the Kotest seed
 * (for example through [propTestConfig]) with a configuration rebuilt from that recipe reproduces the
 * failing samples, including time-relative values.
 *
 * ```kotlin
 * checkAllWithRecipe(config, { session ->
 *     krandomArb(session) { sample -> Generator { DateGenerator(sample).future(7) } }
 * }) { date ->
 *     // assert the application contract
 * }
 * ```
 *
 * @throws IllegalArgumentException before sampling when [config] cannot be converted into a
 * portable seed-owned configuration
 */
suspend fun <A> checkAllWithRecipe(
    config: GeneratorConfig,
    arbFactory: (GeneratorConfig) -> Arb<A>,
    propTestConfig: PropTestConfig = PropTestConfig(),
    property: suspend (A) -> Unit
) {
    val session = config.snapshotClock()
    val recipe = krandomKotestRecipe(session)
    try {
        io.kotest.property.checkAll(propTestConfig, arbFactory(session)) { value -> property(value) }
    } catch (failure: AssertionError) {
        throw AssertionError(
            (failure.message ?: "Property failed") +
                "\n\nkRandom recipe (configuration portion with the clock snapshotted before sampling; " +
                "combine with the Kotest seed above):\n" + recipe,
            failure
        )
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
    val recipe = kotestPortableRecipe()
    val hostDraw = randomSource.random.nextLong()
    val childSeed = GenerationRecipe.deriveChildSeed(
        recipe.seed,
        "kotest|source=${randomSource.seed}|draw=$hostDraw"
    )
    return toBuilder().seed(childSeed).build()
}

/**
 * Returns the portable recipe whose seed parents every Kotest sample: the configured seed (a text
 * seed contributes its derived numeric seed), or `0` for an unseeded configuration.
 */
private fun GeneratorConfig.kotestPortableRecipe(): GenerationRecipe {
    val parentSeed = if (seed.isPresent) seed.asLong else 0L
    val portable = try {
        toBuilder().seed(parentSeed).build()
    } catch (exception: IllegalStateException) {
        throw IllegalArgumentException(
            "Kotest integration requires a seed-owned GeneratorConfig; caller-owned, secure, " +
                "factory-backed, and custom-registry random sources are not replayable",
            exception
        )
    }
    return portable.generationRecipe.orElseThrow {
        IllegalArgumentException("Kotest integration requires a portable seed-owned GeneratorConfig")
    }
}

private fun kotestChildSeed(randomSource: RandomSource): Long {
    val hostDraw = randomSource.random.nextLong()
    return GenerationRecipe.deriveChildSeed(
        0L,
        "kotest|source=${randomSource.seed}|draw=$hostDraw"
    )
}
