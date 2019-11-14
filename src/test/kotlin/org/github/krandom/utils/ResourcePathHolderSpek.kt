package org.github.krandom.utils

import org.github.krandom.utils.ResourcePathHolder.relativeFirstNamePath
import org.github.krandom.utils.ResourcePathHolder.relativeSurNamePath
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object ResourcePathHolderSpek : Spek({

    val testCases = listOf(relativeFirstNamePath, relativeSurNamePath)

    for (test in testCases) {
        describe("check [$test]") {
            it("[$test] should not be blank") {
                assert(test.isNotBlank()) { "[$test] should not be blank!" }
            }
            it("[$test] should not be empty") {
                assert(test.isNotEmpty()) { "[$test] should not be empty!" }
            }
        }
    }
})
