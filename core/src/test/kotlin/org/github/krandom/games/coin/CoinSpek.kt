/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.games.coin

import org.github.krandom.games.coin.enum.CoinResult
import org.github.krandom.testhelper.Constants
import org.github.krandom.testhelper.isValidCoin
import io.kotest.core.spec.style.DescribeSpec

class CoinSpek : DescribeSpec({

    val testName = "head OR tail"
    val moreThanMax = Short.MAX_VALUE.toInt() + 1

    describe("a default coin") {
        val coin = Coin()

        describe("generate coin flip(1)") {
            (1..Constants.generateValues).forEach { _ ->
                val value = coin.flip()
                it(" $value should be valid $testName") {
                    isValidCoin(value)
                }
            }
        }

        describe("generate coin flip($moreThanMax)") {
            val value = coin.flip(moreThanMax)
            it(" ${value.take(10)} should be valid $testName") {
                isValidCoin(value, Coin.MAX_ALLOWED_SIZE)
            }
        }

        describe("generate coin flip(${Constants.generateValues})") {
            (1..Constants.generateValues).forEach { _ ->
                val value = coin.flip(Constants.generateValues)
                it(" ${value.take(10)} should be valid $testName") {
                    isValidCoin(value, Constants.generateValues)
                }
            }
        }

    }

    describe("a provided coin") {
        val head = CoinResult("h")
        val tail = CoinResult("t")
        val coin = Coin(head, tail)

        describe("generate coin flip(1)") {
            (1..Constants.generateValues).forEach { _ ->
                val value = coin.flip()
                it(" $value should be valid $testName") {
                    isValidCoin(value, head, tail)
                }
            }
        }

        describe("generate coin flip($moreThanMax)") {
            val value = coin.flip(moreThanMax)
            it(" ${value.take(10)} should be valid $testName") {
                isValidCoin(value, Coin.MAX_ALLOWED_SIZE, head, tail)
            }
        }

        describe("generate coin flip(${Constants.generateValues})") {
            (1..Constants.generateValues).forEach { _ ->
                val value = coin.flip(Constants.generateValues)
                it(" $value should be valid $testName") {
                    isValidCoin(value, Constants.generateValues, head, tail)
                }
            }
        }

    }

})

