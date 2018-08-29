package krandom.common

import krandom.utils.TestLifecycle
import mu.KLogger
import mu.KLogging
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.api.dsl.xon

class RandomizerSpek : Spek({

    val logger: KLogger = KLogging().logger(RandomizerSpek::class.java.simpleName)

    val generateValues = 1000
    var doubleNumber: Double
    var floatNumber: Float
    var longNumber: Long
    var intNumber: Int
    var char: Char
    var boolean: Boolean
    var string: String

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
                    kRandom.randomDouble(0.0, 0.0)
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

            TestLifecycle().onTestStart("generate random float in range(0, 0)")
            on("generate random float in range(0, 0)") {
                try {
                    kRandom.randomFloat(0.0f, 0.0f)
                } catch (exception: IllegalArgumentException) {
                    assert(exception.message!!.startsWith(prefix = "Illegal argument passed start = 0.0 and end = 0.0, they should be different!", ignoreCase = false))
                }
            }
            TestLifecycle().onTestFinish("generate random float in range(0, 0)")

            TestLifecycle().onTestStart("generate random float in range(-start, end)")
            on("generate random float in range(-start, end)") {
                (1..generateValues).forEach {
                    floatNumber = kRandom.randomFloat(-1.0f, 5.0f)
                    TestLifecycle().onTestStep(logger, "generated : [$floatNumber]")
                    it("$floatNumber should be in range >=-1 and >5") {
                        assert(floatNumber >= -1)
                        assert(floatNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random float in range(-start, end)")

            TestLifecycle().onTestStart("generate random float in range(-start, -end)")
            on("generate random float in range(-start, -end)") {
                (1..generateValues).forEach {
                    floatNumber = kRandom.randomFloat(-1.0f, -5.0f)
                    TestLifecycle().onTestStep(logger, "generated : [$floatNumber]")
                    it("$floatNumber should be in range >=-1 and >-5") {
                        assert(floatNumber >= -1)
                        assert(floatNumber < -5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random float in range(-start, -end)")

            TestLifecycle().onTestStart("generate random float in range(start, -end)")
            on("generate random float in range(start, -end)") {
                (1..generateValues).forEach {
                    floatNumber = kRandom.randomFloat(1.0f, -5.0f)
                    TestLifecycle().onTestStep(logger, "generated : [$floatNumber]")
                    it("$floatNumber should be in range >=1 and >-5") {
                        assert(floatNumber >= 1)
                        assert(floatNumber < -5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random float in range(start, -end)")

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

            TestLifecycle().onTestStart("generate random long in range(0, 0)")
            on("generate random long in range(0, 0)") {
                try {
                    kRandom.randomLong(0L, 0L)
                } catch (exception: IllegalArgumentException) {
                    assert(exception.message!!.startsWith(prefix = "Illegal argument passed start = 0.0 and end = 0.0, they should be different!", ignoreCase = false))
                }
            }
            TestLifecycle().onTestFinish("generate random long in range(0, 0)")

            TestLifecycle().onTestStart("generate random long in range(-start, end)")
            on("generate random long in range(-start, end)") {
                (1..generateValues).forEach {
                    longNumber = kRandom.randomLong(-1L, 5L)
                    TestLifecycle().onTestStep(logger, "generated : [$longNumber]")
                    it("$longNumber should be in range >=-1 and >5") {
                        assert(longNumber >= -1)
                        assert(longNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random long in range(-start, end)")

            TestLifecycle().onTestStart("generate random long in range(-start, -end)")
            on("generate random long in range(-start, -end)") {
                (1..generateValues).forEach {
                    longNumber = kRandom.randomLong(-1L, -5L)
                    TestLifecycle().onTestStep(logger, "generated : [$longNumber]")
                    it("$longNumber should be in range >=-1 and >-5") {
                        assert(longNumber >= -1)
                        assert(longNumber < -5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random long in range(-start, -end)")

            TestLifecycle().onTestStart("generate random long in range(start, -end)")
            on("generate random long in range(start, -end)") {
                (1..generateValues).forEach {
                    longNumber = kRandom.randomLong(1L, -5L)
                    TestLifecycle().onTestStep(logger, "generated : [$longNumber]")
                    it("$longNumber should be in range >=1 and >-5") {
                        assert(longNumber >= 1)
                        assert(longNumber < -5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random long in range(start, -end)")

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

            TestLifecycle().onTestStart("generate random int in range(0, 0)")
            on("generate random int in range(0, 0)") {
                try {
                    kRandom.randomInt(0, 0)
                } catch (exception: IllegalArgumentException) {
                    assert(exception.message!!.startsWith(prefix = "Illegal argument passed start = 0.0 and end = 0.0, they should be different!", ignoreCase = false))
                }
            }
            TestLifecycle().onTestFinish("generate random int in range(0, 0)")

            TestLifecycle().onTestStart("generate random int in range(-start, end)")
            on("generate random int in range(-start, end)") {
                (1..generateValues).forEach {
                    intNumber = kRandom.randomInt(-1, 5)
                    TestLifecycle().onTestStep(logger, "generated : [$intNumber]")
                    it("$intNumber should be in range >=-1 and >5") {
                        assert(intNumber >= -1)
                        assert(intNumber < 5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random int in range(-start, end)")

            TestLifecycle().onTestStart("generate random int in range(-start, -end)")
            xon("generate random int in range(-start, -end)") {
                (1..generateValues).forEach {
                    intNumber = kRandom.randomInt(-5, -1)
                    TestLifecycle().onTestStep(logger, "generated : [$intNumber]")
                    it("$intNumber should be in range >=-1 and >-5") {
                        assert(intNumber >= -1)
                        assert(intNumber < -5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random int in range(-start, -end)")

            TestLifecycle().onTestStart("generate random int in range(start, -end)")
            xon("generate random int in range(start, -end)") {
                (1..generateValues).forEach {
                    intNumber = kRandom.randomInt(1, -5)
                    TestLifecycle().onTestStep(logger, "generated : [$intNumber]")
                    it("$intNumber should be in range >=1 and >-5") {
                        assert(intNumber >= 1)
                        assert(intNumber < -5)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random int in range(start, -end)")

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
            TestLifecycle().onTestFinish("generate random int in range(end)")
        }

        describe("a random tests for char") {
            TestLifecycle().onTestStart("generate random char with all defaults params")
            on("generate random char with all defaults params") {
                (1..generateValues).forEach {
                    char = kRandom.randomChar()
                    TestLifecycle().onTestStep(logger, "generated : [$char]")
                    it("$char.toInt() should be in range >66 and >122") {
                        assert(char.toInt() >= 66)
                        assert(char.toInt() < 122)
                        assert(char.isLetter())
                        assert(!char.isDigit())
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random char with all defaults params")

            TestLifecycle().onTestStart("generate random char with uppercase")
            on("generate random char with uppercase") {
                (1..generateValues).forEach {
                    char = kRandom.randomChar(true, false, false, false)
                    TestLifecycle().onTestStep(logger, "generated : [$char]")
                    it("$char.toInt() should be in range >68 and >90") {
                        assert(char.toInt() >= 68)
                        assert(char.toInt() < 90)
                        assert(char.isLetter())
                        assert(!char.isDigit())
                        assert(char.isUpperCase())
                        assert(!char.isLowerCase())
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random char with uppercase")

            TestLifecycle().onTestStart("generate random char with lowercase")
            on("generate random char with lowercase") {
                (1..generateValues).forEach {
                    char = kRandom.randomChar(false, true, false, false)
                    TestLifecycle().onTestStep(logger, "generated : [$char]")
                    it("$char.toInt() should be in range >97 and >122") {
                        assert(char.toInt() >= 97)
                        assert(char.toInt() < 122)
                        assert(char.isLetter())
                        assert(!char.isDigit())
                        assert(!char.isUpperCase())
                        assert(char.isLowerCase())
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random char with lowercase")

            TestLifecycle().onTestStart("generate random char with numbers")
            on("generate random char with numbers") {
                (1..generateValues).forEach {
                    char = kRandom.randomChar(false, false, true, false)
                    TestLifecycle().onTestStep(logger, "generated : [$char]")
                    it("$char.toInt() should be in range >48 and >57") {
                        assert(char.toInt() >= 48)
                        assert(char.toInt() < 57)
                        assert(!char.isLetter())
                        assert(char.isDigit())
                        assert(!char.isUpperCase())
                        assert(!char.isLowerCase())
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random char with numbers")

            TestLifecycle().onTestStart("generate random char with symbols")
            on("generate random char with numbers") {
                (1..generateValues).forEach {
                    char = kRandom.randomChar(false, false, false, true)
                    TestLifecycle().onTestStep(logger, "generated : [$char]")
                    it("$char.toInt() should be in range >33 and >126") {
                        assert(char.toInt() >= 33)
                        assert(char.toInt() < 126)
                        assert(!char.isLetter())
                        assert(!char.isDigit())
                        assert(!char.isUpperCase())
                        assert(!char.isLowerCase())
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random char with symbols")

            TestLifecycle().onTestStart("generate random char with all available")
            on("generate random char with all available") {
                (1..generateValues).forEach {
                    char = kRandom.randomChar(true, true, true, true)
                    TestLifecycle().onTestStep(logger, "generated : [$char]")
                    it("$char.toInt() should be in range >33 and >126") {
                        assert(char.toInt() >= 33)
                        assert(char.toInt() < 126)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random char with all available")
        }

        describe("a random tests for boolean") {
            TestLifecycle().onTestStart("generate random boolean")
            on("generate random boolean") {
                (1..10).forEach {
                    boolean = kRandom.randomBoolean()
                    TestLifecycle().onTestStep(logger, "generated : [$boolean]")
                    it("[$it idx] $boolean should be true or false") {
                        assert(boolean || !boolean)
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random boolean")
        }

        describe("a random tests for string") {
            TestLifecycle().onTestStart("generate random string with all defaults params")
            on("generate random string with all defaults params") {
                (1..generateValues).forEach {
                    string = kRandom.randomString()
                    TestLifecycle().onTestStep(logger, "generated : [$string]")
                    it("$string should be with length 5 and not empty and not blank") {
                        assert(string.length == 5)
                        assert(string.isNotBlank())
                        assert(string.isNotEmpty())
                        assert(string.contains("[0-9]+".toRegex()))
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random string with all defaults params")

            TestLifecycle().onTestStart("generate random string with uppercase")
            on("generate random string with uppercase") {
                (1..generateValues).forEach {
                    string = kRandom.randomString(25, false, false)
                    TestLifecycle().onTestStep(logger, "generated : [$string]")
                    it("$string should be with length 25 and not empty and not blank") {
                        assert(string.length == 25)
                        assert(string.isNotBlank())
                        assert(string.isNotEmpty())
                        assert(!string.contains("[0-9]+".toRegex()))
                    }
                }
            }
            TestLifecycle().onTestFinish("generate random string with uppercase")
        }

    }
})