package org.github.krandom.common

import org.github.krandom.utils.Constants.generateValues
import org.github.krandom.utils.TestLifecycle
import mu.KLogger
import mu.KLogging
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object RandomizerCharSpek : Spek({

    val logger: KLogger = KLogging().logger(RandomizerCharSpek::class.java.simpleName)

    var charz: Char

    describe("a randomizer") {
        val kRandomCommon: KRandomCommon = Randomizer()

        describe("a random tests for char") {
            TestLifecycle.onTestStart("generate random char with all defaults params")
            describe("generate random char with all defaults params") {
                (1..generateValues).forEach {
                    charz = kRandomCommon.randomChar()
                    charz.let { char ->
                        TestLifecycle.onTestStep(logger, "generated : [$char]")
                        it("$char.toInt() should be in range >66 and <122") {
                            assert(char.toInt() >= 66) { "${char.toInt()} >= 66" }
                            assert(char.toInt() < 122) { "${char.toInt()} < 122" }
                            assert(char.isLetter())
                            assert(!char.isDigit())
                        }
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random char with all defaults params")

            TestLifecycle.onTestStart("generate random char with uppercase")
            describe("generate random char with uppercase") {
                (1..generateValues).forEach {
                    charz = kRandomCommon.randomChar(true, false, false, false)
                    charz.let { char ->
                        TestLifecycle.onTestStep(logger, "generated : [$char]")
                        it("$char.toInt() should be in range >65 and <90") {
                            assert(char.toInt() >= 65) { "${char.toInt()} >= 65" }
                            assert(char.toInt() < 90) { "${char.toInt()} < 90" }
                            assert(char.isLetter())
                            assert(!char.isDigit())
                            assert(char.isUpperCase())
                            assert(!char.isLowerCase())
                        }
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random char with uppercase")

            TestLifecycle.onTestStart("generate random char with lowercase")
            describe("generate random char with lowercase") {
                (1..generateValues).forEach {
                    charz = kRandomCommon.randomChar(false, true, false, false)
                    charz.let { char ->
                        TestLifecycle.onTestStep(logger, "generated : [$char]")
                        it("$char.toInt() should be in range >97 and <122") {
                            assert(char.toInt() >= 97) { "${char.toInt()} >= 97" }
                            assert(char.toInt() < 122) { "${char.toInt()} < 122" }
                            assert(char.isLetter())
                            assert(!char.isDigit())
                            assert(!char.isUpperCase())
                            assert(char.isLowerCase())
                        }
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random char with lowercase")

            TestLifecycle.onTestStart("generate random char with numbers")
            describe("generate random char with numbers") {
                (1..generateValues).forEach {
                    charz = kRandomCommon.randomChar(false, false, true, false)
                    charz.let { char ->
                        TestLifecycle.onTestStep(logger, "generated : [$char]")
                        it("$char.toInt() should be in range >48 and <57") {
                            assert(char.toInt() >= 48) { "${char.toInt()} >= 48" }
                            assert(char.toInt() < 57) { "${char.toInt()} < 57" }
                            assert(!char.isLetter())
                            assert(char.isDigit())
                            assert(!char.isUpperCase())
                            assert(!char.isLowerCase())
                        }
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random char with numbers")

            TestLifecycle.onTestStart("generate random char with symbols")
            describe("generate random char with numbers") {
                (1..generateValues).forEach {
                    charz = kRandomCommon.randomChar(false, false, false, true)
                    charz.let { char ->
                        TestLifecycle.onTestStep(logger, "generated : [$char]")
                        it("$char.toInt() should be in range >33 and <126") {
                            assert(char.toInt() >= 33) { "${char.toInt()} >= 33" }
                            assert(char.toInt() < 126) { "${char.toInt()} < 126" }
                            assert(!char.isLetter())
                            assert(!char.isDigit())
                            assert(!char.isUpperCase())
                            assert(!char.isLowerCase())
                        }
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random char with symbols")

            TestLifecycle.onTestStart("generate random char with all available")
            describe("generate random char with all available") {
                (1..generateValues).forEach {
                    charz = kRandomCommon.randomChar(true, true, true, true)
                    charz.let { char ->
                        TestLifecycle.onTestStep(logger, "generated : [$char]")
                        it("$char.toInt() should be in range >33 and <126") {
                            assert(char.toInt() >= 33) { "${char.toInt()} >= 33" }
                            assert(char.toInt() < 126) { "${char.toInt()} < 126" }
                        }
                    }
                }
            }
            TestLifecycle.onTestFinish("generate random char with all available")
        }
    }
})
