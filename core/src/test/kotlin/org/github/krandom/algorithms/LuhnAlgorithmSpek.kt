/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.algorithms

import org.github.krandom.testhelper.Constants.generateValues
import org.github.krandom.testhelper.LuhnUtils
import io.kotest.core.spec.style.DescribeSpec

class LuhnAlgorithmSpek : DescribeSpec({

    describe("generate random luhn number from scratch") {
        var number: String
        (1..generateValues).onEach {
            number = LuhnAlgorithm.randomNumber()
            it("$number valid luhn algorithm checked") {
                //basic check
                assert(number.length == 10) { "Length should be 10! but it is [${number.length}]" }
                assert(number.matches("[0-9]+".toRegex())) { "Should be number 0-9 but is not [$number]" }

                //advanced check
                assert(LuhnUtils.checkValidLuhnNumber(number))
            }
        }
    }
    })
