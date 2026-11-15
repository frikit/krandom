/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.dsl

import io.github.frikit.krandom.generator.`object`.ObjectFieldStreamPolicy
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class IndependentStreamsDslTest :
    DescribeSpec({
        it("an unrelated Kotlin rule does not perturb independent seeded fields") {
            val plain =
                krandomList<StreamFixture>(5) {
                    config {
                        seed(42L)
                        objectFieldStreamPolicy(ObjectFieldStreamPolicy.INDEPENDENT)
                    }
                }
            val customized =
                krandomList<StreamFixture>(5) {
                    config {
                        seed(42L)
                        objectFieldStreamPolicy(ObjectFieldStreamPolicy.INDEPENDENT)
                    }
                    rule(StreamFixture::name) { "fixed" }
                }
            plain.map { it.age } shouldBe customized.map { it.age }
            plain.map { it.values } shouldBe customized.map { it.values }
        }
        it("Kotlin configurations expose snapshots and preserve the configured policy") {
            val config =
                krandomConfig {
                    seed(42L)
                    objectFieldStreamPolicy(ObjectFieldStreamPolicy.INDEPENDENT)
                }.snapshotClock()
            config.generationRecipe
                .orElseThrow()
                .toGeneratorConfig()
                .objectFieldStreamPolicy shouldBe
                ObjectFieldStreamPolicy.INDEPENDENT
        }
        it("Kotlin configurations set the seeding settings, on by default from 4.0.0, which their recipes keep") {
            val config =
                krandomConfig {
                    seed(42L)
                    advancingConvenienceStreams(true)
                    strictMathGaussian(true)
                    objectReseedBuiltInRandomizers(true)
                }.snapshotClock()
            val replay = config.generationRecipe.orElseThrow().toGeneratorConfig()
            listOf(
                replay.isAdvancingConvenienceStreams,
                replay.isStrictMathGaussian,
                replay.isObjectReseedBuiltInRandomizers,
            ) shouldBe listOf(true, true, true)
            val off =
                krandomConfig {
                    seed(42L)
                    advancingConvenienceStreams(false)
                    strictMathGaussian(false)
                    objectReseedBuiltInRandomizers(false)
                }.snapshotClock()
            val offReplay = off.generationRecipe.orElseThrow().toGeneratorConfig()
            listOf(
                offReplay.isAdvancingConvenienceStreams,
                offReplay.isStrictMathGaussian,
                offReplay.isObjectReseedBuiltInRandomizers,
            ) shouldBe listOf(false, false, false)
            val defaults = krandomConfig { seed(42L) }
            listOf(
                defaults.isAdvancingConvenienceStreams,
                defaults.isStrictMathGaussian,
                defaults.isObjectReseedBuiltInRandomizers,
            ) shouldBe listOf(true, true, true)
            listOf(
                off.isAdvancingConvenienceStreams,
                off.isStrictMathGaussian,
                off.isObjectReseedBuiltInRandomizers,
            ) shouldBe listOf(false, false, false)
        }
        it("Kotlin configurations set the German street layout, on by default from 4.0.0, which recipes keep") {
            val config =
                krandomConfig {
                    seed(42L)
                    locale(java.util.Locale.GERMANY)
                    germanStreetLayout(false)
                }
            config.isGermanStreetLayout shouldBe false
            config.generationRecipe
                .orElseThrow()
                .toGeneratorConfig()
                .isGermanStreetLayout shouldBe false
            krandomConfig { seed(42L) }.isGermanStreetLayout shouldBe true
            krandomConfig {
                seed(42L)
                germanStreetLayout(true)
            }.isGermanStreetLayout shouldBe true
        }
    })

data class StreamFixture(
    val name: String,
    val age: Int,
    val values: List<Int>,
)
