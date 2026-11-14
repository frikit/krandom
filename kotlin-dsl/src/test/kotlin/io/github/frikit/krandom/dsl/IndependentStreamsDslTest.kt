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
        it("Kotlin configurations opt in to the German street layout, which recipes keep") {
            val config =
                krandomConfig {
                    seed(42L)
                    locale(java.util.Locale.GERMANY)
                    germanStreetLayout(true)
                }
            config.isGermanStreetLayout shouldBe true
            config.generationRecipe
                .orElseThrow()
                .toGeneratorConfig()
                .isGermanStreetLayout shouldBe true
            krandomConfig { seed(42L) }.isGermanStreetLayout shouldBe false
        }
    })

data class StreamFixture(
    val name: String,
    val age: Int,
    val values: List<Int>,
)
