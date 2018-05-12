package krandom.common

import krandom.utils.TestLifecycle
import mu.KLogger
import mu.KLogging
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

class RandomizerSpek : Spek({

    val logger: KLogger = KLogging().logger(RandomizerSpek::class.java.simpleName)

    val generateValues = 1000
    var doubleNumber: Double
    var floatNumber: Float
    var longNumber: Long
    var intNumber: Int

    describe("a randomizer") {
        val kRandom: KRandom = Randomizer()

        describe("a random tests for double") {

            TestLifecycle().onTestStart("generate random double")
            on("generate random double") {
                (1..generateValues).forEach {
                    doubleNumber = kRandom.randomDouble()
                    TestLifecycle().onTestStep(logger, "generated : [$doubleNumber]")
                    it("$doubleNumber should be in range >=0 and >1") {
                        assert(doubleNumber >= 0)
                        assert(doubleNumber < 1)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random double")

            TestLifecycle().onTestStart("generate random double in range")
            on("generate random double in range") {
                (1..generateValues).forEach {
                    doubleNumber = kRandom.randomDouble(1.0..5.0)
                    TestLifecycle().onTestStep(logger, "generated : [$doubleNumber]")
                    it("$doubleNumber should be in range >=1 and >5") {
                        assert(doubleNumber >= 1)
                        assert(doubleNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random double in range")

            TestLifecycle().onTestStart("generate random double in range(start, end)")
            on("generate random double in range(start, end)") {
                (1..generateValues).forEach {
                    doubleNumber = kRandom.randomDouble(1.0, 5.0)
                    TestLifecycle().onTestStep(logger, "generated : [$doubleNumber]")
                    it("$doubleNumber should be in range >=1 and >5") {
                        assert(doubleNumber >= 1)
                        assert(doubleNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random double in range(start, end)")

            TestLifecycle().onTestStart("generate random double in range(0, 0)")
            on("generate random double in range(0, 0)") {
                try {
                    kRandom.randomDouble(0.0,0.0)
                } catch (exception: IllegalArgumentException) {
                    assert(exception.message!!.startsWith(prefix = "Illegal argument passed start = 0.0 and end = 0.0, they should be different!", ignoreCase = false))
                }
            }
            TestLifecycle().onTestFinish("generate random double in range(0, 0)")

            TestLifecycle().onTestStart("generate random double in range(-start, end)")
            on("generate random double in range(-start, end)") {
                (1..generateValues).forEach {
                    doubleNumber = kRandom.randomDouble(-1.0, 5.0)
                    TestLifecycle().onTestStep(logger, "generated : [$doubleNumber]")
                    it("$doubleNumber should be in range >=-1 and >5") {
                        assert(doubleNumber >= -1)
                        assert(doubleNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random double in range(-start, end)")

            TestLifecycle().onTestStart("generate random double in range(-start, -end)")
            on("generate random double in range(-start, -end)") {
                (1..generateValues).forEach {
                    doubleNumber = kRandom.randomDouble(-1.0, -5.0)
                    TestLifecycle().onTestStep(logger, "generated : [$doubleNumber]")
                    it("$doubleNumber should be in range >=-1 and >-5") {
                        assert(doubleNumber >= -1)
                        assert(doubleNumber < -5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random double in range(-start, -end)")

            TestLifecycle().onTestStart("generate random double in range(start, -end)")
            on("generate random double in range(start, -end)") {
                (1..generateValues).forEach {
                    doubleNumber = kRandom.randomDouble(1.0, -5.0)
                    TestLifecycle().onTestStep(logger, "generated : [$doubleNumber]")
                    it("$doubleNumber should be in range >=1 and >-5") {
                        assert(doubleNumber >= 1)
                        assert(doubleNumber < -5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random double in range(start, -end)")

            TestLifecycle().onTestStart("generate random double in range(start)")
            on("generate random double in range(start)") {
                (1..generateValues).forEach {
                    doubleNumber = kRandom.randomDouble(start = 1.0)
                    TestLifecycle().onTestStep(logger, "generated : [$doubleNumber]")
                    it("$doubleNumber should be >=1") {
                        assert(doubleNumber >= 1)
                        assert(doubleNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random double in range(start)")

            TestLifecycle().onTestStart("generate random double in range(end)")
            on("generate random double in range(end)") {
                (1..generateValues).forEach {
                    doubleNumber = kRandom.randomDouble(end = 5.0)
                    TestLifecycle().onTestStep(logger, "generated : [$doubleNumber]")
                    it("$doubleNumber should be >5") {
                        assert(doubleNumber >= 1)
                        assert(doubleNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random double in range(end)")
        }

    }
})