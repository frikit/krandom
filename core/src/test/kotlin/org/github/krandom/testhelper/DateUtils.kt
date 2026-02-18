package org.github.krandom.testhelper

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

object DateUtils {

    fun newDate(year: Int, month: Int, date: Int): Date {
        val dateLocal = LocalDateTime.of(year, month, date, 0, 0)
        return Date.from(dateLocal.atZone(ZoneId.systemDefault()).toInstant())
    }

    fun newDate(): Date {
        val dateLocal = LocalDateTime.now()
        return Date.from(dateLocal.atZone(ZoneId.systemDefault()).toInstant())
    }

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
