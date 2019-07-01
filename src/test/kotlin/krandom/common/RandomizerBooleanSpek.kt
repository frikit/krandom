package krandom.common

import krandom.utils.Constants.generateValues
import krandom.utils.TestLifecycle
import mu.KLogger
import mu.KLogging
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object RandomizerBooleanSpek : Spek({

    val logger: KLogger = KLogging().logger(RandomizerBooleanSpek::class.java.simpleName)
    val randomType = "boolean"
    var boolean: Boolean

    describe("a randomizer") {
        val kRandomCommon: KRandomCommon = Randomizer()

        describe("a random tests for $randomType") {
            TestLifecycle.onTestStart("generate random $randomType")
            describe("generate random $randomType") {
                (1..generateValues).forEach {
                    boolean = kRandomCommon.randomBoolean()
                    TestLifecycle.onTestStep(logger, "generated : [$boolean]")
                    it("[$it idx] $boolean should be true or false") {
                        assert(boolean || !boolean)
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random $randomType")
        }
    }
})
