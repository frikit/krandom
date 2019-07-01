package krandom.common

import krandom.utils.Constants.generateValues
import krandom.utils.TestLifecycle
import mu.KLogger
import mu.KLogging
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object RandomizerStringSpek : Spek({

    val logger: KLogger = KLogging().logger(RandomizerStringSpek::class.java.simpleName)

    var stringz: String

    describe("a randomizer") {
        val kRandomCommon: KRandomCommon = Randomizer()

        describe("a random tests for string") {
            TestLifecycle.onTestStart("generate random string with all defaults params")
            describe("generate random string with all defaults params") {
                (1..generateValues).forEach {
                    stringz = kRandomCommon.randomString()
                    stringz.let { string ->
                        TestLifecycle.onTestStep(logger, "generated : [$string]")
                        it("$string should be with length 5 and not empty and not blank") {
                            assert(string.length == 5)
                            assert(string.isNotBlank())
                            assert(string.isNotEmpty())
                        }
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random string with all defaults params")

            TestLifecycle.onTestStart("generate random string")
            describe("generate random string") {
                (1..generateValues).forEach {
                    stringz = kRandomCommon.randomString(25, false, false)
                    stringz.let { string ->
                        TestLifecycle.onTestStep(logger, "generated : [$string]")
                        it("$string should be with length 25 and not empty and not blank") {
                            assert(string.length == 25)
                            assert(string.isNotBlank())
                            assert(string.isNotEmpty())
                            assert(!string.contains("[0-9]+".toRegex()))
                        }
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random string")

            TestLifecycle.onTestStart("generate random string with numbers")
            describe("generate random string with numbers") {
                (1..generateValues).forEach {
                    stringz = kRandomCommon.randomString(33, false, true)
                    stringz.let { string ->
                        TestLifecycle.onTestStep(logger, "generated : [$string]")
                        it("$string should be with length 33 and not empty and not blank") {
                            assert(string.length == 33)
                            assert(string.isNotBlank())
                            assert(string.isNotEmpty())
                            assert(string.contains("[0-9]+".toRegex()))
                        }
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random string with numbers")
        }
    }
})
