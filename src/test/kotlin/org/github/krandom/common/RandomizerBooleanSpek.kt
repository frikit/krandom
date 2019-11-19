package org.github.krandom.common

import org.github.krandom.testhelper.Constants.generateValues
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object RandomizerBooleanSpek : Spek({

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
