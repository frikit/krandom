package org.github.krandom.common

import org.github.krandom.testhelper.Constants.generateValues
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
            describe("generate random char with all defaults params") {
                (1..generateValues).forEach { _ ->
                    charz = kRandomCommon.randomChar()
                    charz.let { char ->
                        it("$char.toInt() should be in range >66 and <122") {
                            assert(char.toInt() >= 66) { "${char.toInt()} >= 66" }
                            assert(char.toInt() < 122) { "${char.toInt()} < 122" }
                            assert(char.isLetter())
                            assert(!char.isDigit())
                        }
                    }
                }
            }
            describe("generate random char with uppercase") {
                (1..generateValues).forEach { _ ->
                    charz = kRandomCommon.randomChar(upperLetters = true, lowerLetters = false, numbers = false, specialCharacters = false)
                    charz.let { char ->
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
            describe("generate random char with lowercase") {
                (1..generateValues).forEach { _ ->
                    charz = kRandomCommon.randomChar(upperLetters = false, lowerLetters = true, numbers = false, specialCharacters = false)
                    charz.let { char ->
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
            describe("generate random char with numbers") {
                (1..generateValues).forEach { _ ->
                    charz = kRandomCommon.randomChar(upperLetters = false, lowerLetters = false, numbers = true, specialCharacters = false)
                    charz.let { char ->
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
            describe("generate random char with numbers") {
                (1..generateValues).forEach { _ ->
                    charz = kRandomCommon.randomChar(upperLetters = false, lowerLetters = false, numbers = false, specialCharacters = true)
                    charz.let { char ->
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
            describe("generate random char with all available") {
                (1..generateValues).forEach { _ ->
                    charz = kRandomCommon.randomChar(upperLetters = true, lowerLetters = true, numbers = true, specialCharacters = true)
                    charz.let { char ->
                        it("$char.toInt() should be in range >33 and <126") {
                            assert(char.toInt() >= 33) { "${char.toInt()} >= 33" }
                            assert(char.toInt() < 126) { "${char.toInt()} < 126" }
                        }
                    }
                }
            }
            }
    }
})
