/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.user.enum

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.collections.HashMap

enum class AgeGroup(val groupName: String, val range: ClosedRange<Int>) {
    CHILD("child", 0..14),
    TEEN("teen", 14..18),
    ADULT("adult", 18..60),
    SENIOR("senior", 60..99);

    private val birthDays = HashMap<String, Pair<Date, Date>>()

    fun getBirthdayRange(): Pair<Date, Date> {
        if (!birthDays.containsKey(this.groupName)) {
            val start = this.range.start.toLong()
            val end = this.range.endInclusive.toLong()

            val from: Date = Date.from(LocalDateTime.now().minusYears(end).atZone(ZoneId.systemDefault()).toInstant())
            val to: Date = Date.from(LocalDateTime.now().minusYears(start).atZone(ZoneId.systemDefault()).toInstant())

            birthDays[this.groupName] = from to to
        }

        return birthDays[this.groupName]!!
    }
}
