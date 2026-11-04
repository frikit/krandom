/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.kotest

import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.generator.base.RegexGenerator
import io.kotest.assertions.withClue
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import java.util.regex.Pattern
import kotlin.random.Random

/**
 * RegexGenerator's contract, checked over many seeds and patterns: every value matches the pattern under
 * java.util.regex, or construction fails and names why.
 */
@OptIn(ExperimentalKotest::class)
class RegexGeneratorPropertyTest :
    DescribeSpec({

        describe("RegexGenerator") {

            it("generates only values java.util.regex matches, for every listed pattern and any seed") {
                checkAll(
                    3_000,
                    PropTestConfig(seed = 20261104L),
                    Arb.element(LISTED_PATTERNS),
                    Arb.long(),
                ) { regex, seed ->
                    assertEveryValueMatches(regex, RegexGenerator(regex, GeneratorConfig.builder().seed(seed).build()))
                }
            }

            it("rejects a random pattern by name, or generates only values java.util.regex matches") {
                var accepted = 0
                var rejected = 0
                checkAll(
                    6_000,
                    PropTestConfig(seed = 20261105L),
                    arbitrary { source -> randomPattern(source.random, 3) },
                    Arb.long(),
                    Arb.int(1, 60),
                ) { regex, seed, minLength ->
                    // A minimum length above 5 raises how far *, + and {n,} repeat.
                    val config =
                        GeneratorConfig
                            .builder()
                            .seed(seed)
                            .stringLength(minLength, minLength)
                            .build()
                    val generator =
                        try {
                            RegexGenerator(regex, config)
                        } catch (exception: IllegalArgumentException) {
                            val message = exception.message.orEmpty()
                            withClue(message) { REJECTIONS.any { message.startsWith(it) } shouldBe true }
                            rejected++
                            null
                        }
                    if (generator != null) {
                        accepted++
                        assertEveryValueMatches(regex, generator)
                    }
                }
                // Neither branch can have gone quiet.
                accepted shouldBeGreaterThan 2_000
                rejected shouldBeGreaterThan 500
            }
        }
    })

private val LISTED_PATTERNS =
    listOf(
        "\\d{3}-\\d{4}",
        "[A-Z]{2,5}",
        "(yes|no)",
        "^[a-z]+$",
        "\\bfoo\\b",
        "[\\w.-]+@example\\.com",
        "(?i)abc",
        "(?i)(yes|no)-[a-c]{2}",
        "a(?i)b|c",
        "(a(?i)b)c",
        "(?i:x)y",
        "(?i)[^a-z]{3}",
        "(?i)\\P{Lower}{2}",
        "(?i)[^\\p{Lu}]\\p{Ll}",
        "\\p{L}+",
        "\\p{Lu}\\p{Ll}{2,8}",
        "[\\p{L}\\d_-]+",
        "\\P{L}{3}",
        "\\p{Nd}{4}",
        "\\p{N}\\p{IsDigit}\\p{IsAlphabetic}",
        "\\p{IsLatin}{2}\\p{Punct}",
        "\\p{Alnum}\\p{Graph}\\p{Print}\\p{Space}\\p{Blank}\\p{XDigit}\\p{ASCII}",
        "\\Qa.b\\E\\d",
        "\\Qab\\E+",
        "[\\Qa-c\\E]{3}",
        "(?i)\\Q(x)\\E[a-c]",
    )

/** How a rejection starts: a construct this generator cannot honour, or a pattern too large to generate. */
private val REJECTIONS = listOf("Unsupported ", "Generated values could be longer", "Generating a value could take")

private val ATOMS =
    listOf(
        "a",
        "B",
        "é",
        "-",
        " ",
        "_",
        "1",
        ".",
        "\\d",
        "\\w",
        "\\s",
        "\\D",
        "\\W",
        "\\S",
        "[ab]",
        "[a -]",
        "[^a]",
        "[\\d-]",
        "[\\W]",
        "[a-z]",
        "[^\\w.]",
        "\\p{L}",
        "\\p{Lu}",
        "\\P{Alpha}",
        "\\pN",
        "\\p{Punct}",
        "[\\p{Ll}\\d]",
        "[^\\p{Lower}]",
        "\\P{XDigit}",
        "\\p{IsLatin}",
        "\\Qa.\\E",
        "\\Q*\\E",
        "^",
        "$",
        "\\b",
        "\\A",
        "\\z",
        "\\Z",
        // Constructs it rejects
        "\\h",
        "\\p{InGreek}",
        "[a&&b]",
    )
private val ATOM_QUANTIFIERS =
    listOf("", "", "", "", "?", "*", "+", "{2}", "{0,2}", "{1,}", "{0}", "??", "*?", "+?", "{1,2}?")

// No open-ended group: java.util.regex can take years to check a value against ((\w+)*)*.
private val GROUP_QUANTIFIERS = listOf("", "", "?", "{2}", "{0,2}", "{0}", "??", "{1,2}?")
private val GROUP_OPENINGS = listOf("(", "(?:", "(?i:", "(?-i:")

// Flags hold to the end of their group. (?s) is rejected.
private val FLAGS = listOf("(?i)", "(?-i)", "(?s)")

private fun randomPattern(
    random: Random,
    depth: Int,
): String {
    val pattern = StringBuilder()
    val alternatives = if (random.nextInt(4) == 0) 2 else 1
    for (alternative in 0 until alternatives) {
        if (alternative > 0) {
            pattern.append('|')
        }
        repeat(random.nextInt(4)) {
            when {
                depth > 0 && random.nextInt(4) == 0 -> {
                    pattern
                        .append(GROUP_OPENINGS.random(random))
                        .append(randomPattern(random, depth - 1))
                        .append(')')
                        .append(GROUP_QUANTIFIERS.random(random))
                }

                random.nextInt(8) == 0 -> {
                    pattern.append(FLAGS.random(random))
                }

                else -> {
                    pattern.append(ATOMS.random(random)).append(ATOM_QUANTIFIERS.random(random))
                }
            }
        }
    }
    return pattern.toString()
}

private fun assertEveryValueMatches(
    regex: String,
    generator: RegexGenerator,
) {
    val compiled = Pattern.compile(regex)
    repeat(20) {
        val value = generator.generate()
        withClue("\"$value\" from the pattern $regex") { compiled.matcher(value).matches() shouldBe true }
    }
}
