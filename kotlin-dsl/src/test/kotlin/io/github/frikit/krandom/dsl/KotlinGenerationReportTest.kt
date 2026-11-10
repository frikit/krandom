/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.dsl

import io.github.frikit.krandom.generator.report.GenerationReportEntry
import io.github.frikit.krandom.generator.report.GenerationStep
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe

@JvmInline
value class ReportedEmail(
    val value: String,
)

data class ReportedCustomer(
    val name: String,
    val email: ReportedEmail,
    val tags: List<String>,
    val visits: Int,
)

class KotlinGenerationReportTest :
    DescribeSpec({

        describe("the generation report from the Kotlin DSL") {

            it("reports each generated path and leaves the seeded values unchanged, value classes included") {
                val entries = mutableListOf<GenerationReportEntry>()
                val plain = krandomList<ReportedCustomer>(20) { config { seed(42L) } }
                val reported =
                    krandomList<ReportedCustomer>(20) {
                        config {
                            seed(42L)
                            generationReportListener { entry -> entries += entry }
                        }
                    }

                reported shouldBe plain
                entries.shouldNotBeEmpty()
                entries.map { entry -> entry.path() } shouldContain "ReportedCustomer.email"
                entries.map { entry -> entry.step() } shouldContain GenerationStep.COLLECTION
                val values = plain.flatMap { customer -> listOf(customer.name, customer.email.value) + customer.tags }
                entries.forEach { entry ->
                    values.filter { value -> value.length >= 4 }.forEach { value ->
                        entry.toString().contains(value) shouldBe false
                    }
                }
            }
        }
    })
