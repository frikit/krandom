package org.github.krandom.utils

import org.github.krandom.utils.ResourcePathHolder.relativeFirstNamePath
import org.github.krandom.utils.ResourcePathHolder.relativeSurNamePath
import io.kotest.core.spec.style.DescribeSpec

class ResourcePathHolderSpek : DescribeSpec({

    val testCases = listOf(relativeFirstNamePath, relativeSurNamePath)

    describe("check object") {
        it("object string should not be blank/empty") {
            assert(ResourcePathHolder.toString().isNotBlank()) { " should not be blank!" }
            assert(ResourcePathHolder.toString().isNotEmpty()) { " should not be empty!" }
        }
    }

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
