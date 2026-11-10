/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples

import io.github.frikit.krandom.dsl.krandom
import io.github.frikit.krandom.generator.`object`.FixtureRule
import io.github.frikit.krandom.generator.`object`.FixtureRuleKind
import io.github.frikit.krandom.generator.report.GenerationReportEntry
import io.github.frikit.krandom.generator.report.GenerationStep
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Optional

/**
 * A `generationReportListener` in the DSL's `config` block receives one entry for each value that object
 * generation produces: its path, the step and generator that produced it, and the rule that matched it.
 * An entry never holds the value itself.
 */
class GenerationReportExampleTest {
    @Test
    fun `listener reports how each property was produced`() {
        val entries = mutableListOf<GenerationReportEntry>()

        krandom<Customer> {
            config {
                seed(42L)
                generationReportListener { entry -> entries += entry }
            }
            rule(Customer::payment) { Cash }
        }

        // The value class property gets the value it wraps from the email provider, chosen by its name.
        val email = entries.single { it.path == "Customer.email" }
        assertEquals(GenerationStep.SEMANTIC, email.step)
        assertEquals("email", email.generator)

        // A DSL rule is a GeneratorConfig field override, which has no registration number.
        val payment = entries.single { it.path == "Customer.payment" }
        assertEquals(GenerationStep.OVERRIDE, payment.step)
        assertEquals(
            Optional.of(FixtureRule.configured(FixtureRuleKind.FIELD_OVERRIDE, "payment")),
            payment.matchedRule,
        )
    }
}
