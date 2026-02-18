package org.github.krandom.network

import org.github.krandom.common.KRandomCommon
import org.github.krandom.common.Randomizer

object IPv4Random {

    val kRandomCommon: KRandomCommon by lazy { Randomizer() }

    fun random(times: Short): List<String> {
        return (1..times).map { random() }.toList()
    }

    fun random(): String {
        val part1 = kRandomCommon.randomInt(0, 223)
        val part2 = kRandomCommon.randomInt(0, 255)
        val part3 = kRandomCommon.randomInt(0, 255)
        val part4 = kRandomCommon.randomInt(0, 255)
        return "$part1.$part2.$part3.$part4"
    }
}
