package org.github.krandom.user

import org.github.krandom.user.common.GenericUserGenerator
import org.github.krandom.user.enum.AgeGroup

//TODO add exclude age group or list?
//TODO add exclude list of ages?
class Age : KRandomUser<Int>, GenericUserGenerator<Int>() {

    private val list = AgeGroup.values().toList()

    override fun randomData(): Int {
        val index = kRandomCommon.randomInt(0, list.size)
        val group = list[index]
        return kRandomCommon.randomInt(group.range)
    }

    override fun randomDatas(size: Int): List<Int> {
        val start = AgeGroup.CHILD.range.start
        val end = AgeGroup.SENIOR.range.endInclusive
        val actList = (start..end).asIterable().toList()
        return randomDatas(actList, size)
    }
}
