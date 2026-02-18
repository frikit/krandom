package org.github.krandom.common

import org.github.krandom.testhelper.Constants.generateValues
import io.kotest.core.spec.style.DescribeSpec

class RandomizerBooleanSpek : DescribeSpec({

    val randomType = "boolean"
    var boolean: Boolean

    describe("a randomizer") {
        val kRandomCommon: KRandomCommon = Randomizer()

        describe("a random tests for $randomType") {
            describe("generate random $randomType") {
                (1..generateValues).forEach {
                    boolean = kRandomCommon.randomBoolean()
                    it("[$it idx] $boolean should be true or false") {
                        assert(boolean || !boolean)
                    }
                }
            }
            }
    }
})
