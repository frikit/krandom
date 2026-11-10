/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples

import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.generator.Generators
import io.github.frikit.krandom.generator.`object`.FixtureRule
import io.github.frikit.krandom.generator.`object`.FixtureRuleKind
import io.github.frikit.krandom.generator.`object`.FixtureRuleProblem
import io.github.frikit.krandom.generator.`object`.ObjectFaker
import io.github.frikit.krandom.generator.`object`.exception.FixtureRuleException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/**
 * `ObjectFaker.strict()` checks the fixture rules before anything is generated and reports every
 * problem in one [FixtureRuleException], which names paths and rules, never a value.
 */
class FixtureDiagnosticsExampleTest {
    @Test
    fun `strict faker rejects an unused rule before generating`() {
        // The configuration excludes every field named email, so rule #2 can never supply a value.
        val config = GeneratorConfig.builder().objectExcludeField("email").build()
        val faker =
            ObjectFaker(AccountFixture::class.java, config)
                .ruleFor("owner", Generators.ofFullName())
                .ruleFor("email", Generators.ofEmail())
                .strict()

        val failure = assertThrows(FixtureRuleException::class.java) { faker.generate() }

        val problem = failure.problems.single()
        assertEquals(FixtureRuleProblem.Category.UNUSED, problem.category)
        assertEquals(FixtureRule.registered(FixtureRuleKind.RULE, "email", 2), problem.rule)
        assertEquals(
            """
            ObjectFaker for io.github.frikit.krandom.examples.AccountFixture has 1 fixture rule problem:
            - UNUSED rule #2 'email': the GeneratorConfig excludes AccountFixture.email, so generation never asks the rule for a value
            """.trimIndent(),
            failure.message,
        )
    }
}

/** A mutable Kotlin class, which `krandom-core` fills without the Kotlin DSL. */
class AccountFixture {
    var owner: String = ""
    var email: String = ""
}
