/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.common

import org.github.krandom.testhelper.Constants.generateValues
import io.kotest.core.spec.style.DescribeSpec

class RandomizerCharSpek : DescribeSpec({

    var charz: Char

    describe("a randomizer") {
        val kRandomCommon: KRandomCommon = Randomizer()

        describe("a random tests for char") {
            describe("generate random char with all defaults params") {
                (1..generateValues).forEach { _ ->
                    charz = kRandomCommon.randomChar()
                    charz.let { char ->
                        it("$char.code should be in range >66 and <122") {
                            assert(char.code >= 66) { "${char.code} >= 66" }
                            assert(char.code < 122) { "${char.code} < 122" }
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
                        it("$char.code should be in range >65 and <90") {
                            assert(char.code >= 65) { "${char.code} >= 65" }
                            assert(char.code < 90) { "${char.code} < 90" }
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
                        it("$char.code should be in range >97 and <122") {
                            assert(char.code >= 97) { "${char.code} >= 97" }
                            assert(char.code < 122) { "${char.code} < 122" }
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
                        it("$char.code should be in range >48 and <57") {
                            assert(char.code >= 48) { "${char.code} >= 48" }
                            assert(char.code < 57) { "${char.code} < 57" }
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
                        it("$char.code should be in range >33 and <126") {
                            assert(char.code >= 33) { "${char.code} >= 33" }
                            assert(char.code < 126) { "${char.code} < 126" }
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
                        it("$char.code should be in range >33 and <126") {
                            assert(char.code >= 33) { "${char.code} >= 33" }
                            assert(char.code < 126) { "${char.code} < 126" }
                        }
                    }
                }
            }
            }
    }
})
