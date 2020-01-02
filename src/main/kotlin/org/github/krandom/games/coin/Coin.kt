package org.github.krandom.games.coin

import org.github.krandom.common.Randomizer
import org.github.krandom.games.coin.enum.CoinResult

class Coin(val head: CoinResult = CoinResult.HEAD, val tail: CoinResult = CoinResult.TAIL) {

    private val randomizer by lazy { Randomizer() }

    companion object {
        var MAX_ALLOWED_SIZE = Short.MAX_VALUE.toInt()
    }

    fun flip(nrTimes: Int): List<CoinResult> {
        val times = if (nrTimes >= MAX_ALLOWED_SIZE) MAX_ALLOWED_SIZE else nrTimes
        val res = (1..times).map { flip() }.toList()
        val expectedSize = res.size
        require(expectedSize == times) {
            "Generated result $expectedSize != $times , but they should be the same"
        }

        return res
    }

    fun flip(): CoinResult {
        return if (randomizer.randomBoolean()) head else tail
    }

}
