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
            TestLifecycle().onTestFinish("generate random float")

            TestLifecycle().onTestStart("generate random float in range")
            on("generate random float in range") {
                (1..generateValues).forEach {
                    floatNumber = kRandom.randomFloat(1.0f..5.0f)
                    TestLifecycle().onTestStep(logger, "generated : [$floatNumber]")
                    it("$floatNumber should be in range >=1 and >5") {
                        assert(floatNumber >= 1)
                        assert(floatNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random float in range")

            TestLifecycle().onTestStart("generate random float in range(start, end)")
            on("generate random float in range(start, end)") {
                (1..generateValues).forEach {
                    floatNumber = kRandom.randomFloat(1.0f, 5.0f)
                    TestLifecycle().onTestStep(logger, "generated : [$floatNumber]")
                    it("$floatNumber should be in range >=1 and >5") {
                        assert(floatNumber >= 1)
                        assert(floatNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random float in range(start, end)")

            TestLifecycle().onTestStart("generate random float in range(start)")
            on("generate random float in range(start)") {
                (1..generateValues).forEach {
                    floatNumber = kRandom.randomFloat(start = 1.0f)
                    TestLifecycle().onTestStep(logger, "generated : [$floatNumber]")
                    it("$floatNumber should be >=1") {
                        assert(floatNumber >= 1)
                        assert(floatNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random float in range(start)")

            TestLifecycle().onTestStart("generate random float in range(end)")
            on("generate random float in range(end)") {
                (1..generateValues).forEach {
                    floatNumber = kRandom.randomFloat(end = 5.0f)
                    TestLifecycle().onTestStep(logger, "generated : [$floatNumber]")
                    it("$floatNumber should be >5") {
                        assert(floatNumber >= 1)
                        assert(floatNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random float in range(end)")
        }

        describe("a random tests for long") {

            TestLifecycle().onTestStart("generate random long")
            on("generate random long") {
                (1..generateValues).forEach {
                    longNumber = kRandom.randomLong()
                    TestLifecycle().onTestStep(logger, "generated : [$longNumber]")
                    it("$longNumber should be in range >=0 and >1") {
                        assert(longNumber >= 0)
                        assert(longNumber < 1)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random long")

            TestLifecycle().onTestStart("generate random long in range")
            on("generate random long in range") {
                (1..generateValues).forEach {
                    longNumber = kRandom.randomLong(1L..5L)
                    TestLifecycle().onTestStep(logger, "generated : [$longNumber]")
                    it("$longNumber should be in range >=1 and >5") {
                        assert(longNumber >= 1)
                        assert(longNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random long in range")

            TestLifecycle().onTestStart("generate random long in range(start, end)")
            on("generate random long in range(start, end)") {
                (1..generateValues).forEach {
                    longNumber = kRandom.randomLong(1L, 5L)
                    TestLifecycle().onTestStep(logger, "generated : [$longNumber]")
                    it("$longNumber should be in range >=1 and >5") {
                        assert(longNumber >= 1)
                        assert(longNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random long in range(start, end)")

            TestLifecycle().onTestStart("generate random long in range(start)")
            on("generate random long in range(start)") {
                (1..generateValues).forEach {
                    longNumber = kRandom.randomLong(start = 1L)
                    TestLifecycle().onTestStep(logger, "generated : [$longNumber]")
                    it("$longNumber should be >=1") {
                        assert(longNumber >= 1)
                        assert(longNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random long in range(start)")

            TestLifecycle().onTestStart("generate random long in range(end)")
            on("generate random long in range(end)") {
                (1..generateValues).forEach {
                    longNumber = kRandom.randomLong(end = 5L)
                    TestLifecycle().onTestStep(logger, "generated : [$longNumber]")
                    it("$longNumber should be >5") {
                        assert(longNumber >= 1)
                        assert(longNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random long in range(end)")
        }

        describe("a random tests for int") {

            TestLifecycle().onTestStart("generate random int")
            on("generate random int") {
                (1..generateValues).forEach {
                    intNumber = kRandom.randomInt()
                    TestLifecycle().onTestStep(logger, "generated : [$intNumber]")
                    it("$intNumber should be in range >=0 and >1") {
                        assert(intNumber >= 0)
                        assert(intNumber < 1)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random int")

            TestLifecycle().onTestStart("generate random int in range")
            on("generate random int in range") {
                (1..generateValues).forEach {
                    intNumber = kRandom.randomInt(1..5)
                    TestLifecycle().onTestStep(logger, "generated : [$intNumber]")
                    it("$intNumber should be in range >=1 and >5") {
                        assert(intNumber >= 1)
                        assert(intNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random int in range")

            TestLifecycle().onTestStart("generate random int in range(start, end)")
            on("generate random int in range(start, end)") {
                (1..generateValues).forEach {
                    intNumber = kRandom.randomInt(1, 5)
                    TestLifecycle().onTestStep(logger, "generated : [$intNumber]")
                    it("$intNumber should be in range >=1 and >5") {
                        assert(intNumber >= 1)
                        assert(intNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random int in range(start, end)")

            TestLifecycle().onTestStart("generate random int in range(start)")
            on("generate random int in range(start)") {
                (1..generateValues).forEach {
                    intNumber = kRandom.randomInt(start = 1)
                    TestLifecycle().onTestStep(logger, "generated : [$intNumber]")
                    it("$intNumber should be >=1") {
                        assert(intNumber >= 1)
                        assert(intNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random int in range(start)")

            TestLifecycle().onTestStart("generate random int in range(end)")
            on("generate random int in range(end)") {
                (1..generateValues).forEach {
                    intNumber = kRandom.randomInt(end = 5)
                    TestLifecycle().onTestStep(logger, "generated : [$intNumber]")
                    it("$intNumber should be >5") {
                        assert(intNumber >= 1)
                        assert(intNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random long in range(end)")
        }

    }
})