package org.github.krandom.games.dice.enum

import org.github.krandom.games.dice.utils.DiceTypeUtils

enum class DiceType(val nrFaces: Int, val defaultValues: List<String>) {
    D4(4, DiceTypeUtils.generateExpected(4)),
    D6(6, DiceTypeUtils.generateExpected(6)),
    D8(8, DiceTypeUtils.generateExpected(8)),
    D10(10, DiceTypeUtils.generateExpected(10)),
    D12(12, DiceTypeUtils.generateExpected(12)),
    D20(20, DiceTypeUtils.generateExpected(20));
}
