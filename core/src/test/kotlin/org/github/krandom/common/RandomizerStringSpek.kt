/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.common

import org.github.krandom.testhelper.Constants.generateValues
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.assertions.throwables.shouldThrow

class RandomizerStringSpek : DescribeSpec({

    var stringz: String

    describe("a randomizer") {
        val kRandomCommon: KRandomCommon = Randomizer()

        describe("a random tests for string") {
            describe("generate random string with all defaults params") {
                (1..generateValues).forEach { _ ->
                    stringz = kRandomCommon.randomString()
                    stringz.let { string ->
                        it("$string should be with length 5 and not empty and not blank") {
                            assert(string.length == 5)
                            assert(string.isNotBlank())
                            assert(string.isNotEmpty())
                        }
                    }
                }
            }
            describe("generate random string") {
                (1..generateValues).forEach { _ ->
                    stringz = kRandomCommon.randomString(length = 25, specialCharacters = false, numbers = false)
                    stringz.let { string ->
                        it("$string should be with length 25 and not empty and not blank") {
                            assert(string.length == 25)
                            assert(string.isNotBlank())
                            assert(string.isNotEmpty())
                            assert(!string.contains("[0-9]+".toRegex()))
                        }
                    }
                }
            }
            describe("generate random string with numbers") {
                (1..generateValues).forEach { _ ->
                    stringz = kRandomCommon.randomString(length = 33, specialCharacters = false, numbers = true)
                    stringz.let { string ->
                        it("$string should be with length 33 and not empty and not blank") {
                            assert(string.length == 33)
                            assert(string.isNotBlank())
                            assert(string.isNotEmpty())
                            assert(string.contains("[0-9]+".toRegex()))
                        }
                    }
                }
            }
            describe("generate random string and throw exception") {
                (1..generateValues).forEach { _ ->
                    shouldThrow<IllegalArgumentException> {
                        kRandomCommon.randomString(length = -33, specialCharacters = false, numbers = true)
                    }
                }
            }
            }
    }
})
