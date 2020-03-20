package org.github.krandom.user

import org.github.krandom.common.Randomizer
import org.github.krandom.user.common.GenericUserGenerator
import java.util.*
import kotlin.math.abs

class BirthDay(between: Pair<Date, Date>) : KRandomUser<Date>, GenericUserGenerator<Date>() {

    private val kRandom = Randomizer()
    private val oneDay = 86400000 - 1
    val start = if (between.first.before(between.second)) between.first.time else between.second.time
    val end = if (between.first.before(between.second)) between.second.time else between.first.time

    init {
        require(abs(start - end) > oneDay) {
            "Start date and end date should be at least 1 day difference between them!"
        }
    }

    override fun randomData(): Date {
        val time = kRandom.randomLong(start, end)
        return Date(time)
    }

    override fun randomDatas(size: Int): List<Date> {
        isValidSize(size)

        val list = (1..size).map { randomData() }
        return randomDatas(list, size)
    }
}
