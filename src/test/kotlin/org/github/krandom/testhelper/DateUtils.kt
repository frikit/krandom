package org.github.krandom.testhelper

import java.util.*

object DateUtils {

    fun validateBirthDay(actual: Date, from: Date, to: Date) {
        assert(actual.after(from)) { "Birthday should be after [$from], but it is [$actual]" }
        assert(actual.before(to)) { "Birthday should be before [$to], but it is [$actual]" }
    }

    fun validateBirthDays(bds: List<Date>, from: Date, to: Date) {
        for (it in bds) {
            validateBirthDay(it, from, to)
        }
    }
}
