package krandom.spek

import krandom.common.KRandom
import krandom.common.Randomizer
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

        describe("a random tests for float") {

            TestLifecycle().onTestStart("generate random float")
            on("generate random float") {
                (1..generateValues).forEach {
                    floatNumber = kRandom.randomFloat()
                    TestLifecycle().onTestStep(logger, "generated : [$floatNumber]")
                    it("$floatNumber should be in range >=0 and >1") {
                        assert(floatNumber >= 0)
                        assert(floatNumber < 1)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random double")

            TestLifecycle().onTestStart("generate random double in range")
            on("generate random double in range") {
                (1..generateValues).forEach {
                    floatNumber = kRandom.randomFloat(1.0f..5.0f)
                    TestLifecycle().onTestStep(logger, "generated : [$floatNumber]")
                    it("$floatNumber should be in range >=1 and >5") {
                        assert(floatNumber >= 1)
                        assert(floatNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random double in range")

            TestLifecycle().onTestStart("generate random double in range(start, end)")
            on("generate random double in range(start, end)") {
                (1..generateValues).forEach {
                    floatNumber = kRandom.randomFloat(1.0f, 5.0f)
                    TestLifecycle().onTestStep(logger, "generated : [$floatNumber]")
                    it("$floatNumber should be in range >=1 and >5") {
                        assert(floatNumber >= 1)
                        assert(floatNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random double in range(start, end)")

            TestLifecycle().onTestStart("generate random double in range(start)")
            on("generate random double in range(start)") {
                (1..generateValues).forEach {
                    floatNumber = kRandom.randomFloat(start = 1.0f)
                    TestLifecycle().onTestStep(logger, "generated : [$floatNumber]")
                    it("$floatNumber should be >=1") {
                        assert(floatNumber >= 1)
                        assert(floatNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random double in range(start)")

            TestLifecycle().onTestStart("generate random double in range(end)")
            on("generate random double in range(end)") {
                (1..generateValues).forEach {
                    floatNumber = kRandom.randomFloat(end = 5.0f)
                    TestLifecycle().onTestStep(logger, "generated : [$floatNumber]")
                    it("$floatNumber should be >5") {
                        assert(floatNumber >= 1)
                        assert(floatNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random double in range(end)")
        }


    }
})