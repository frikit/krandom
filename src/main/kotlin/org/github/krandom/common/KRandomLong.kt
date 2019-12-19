package org.github.krandom.common

import org.github.krandom.properties.Properties

interface KRandomLong {

    fun randomLong(): Long

    fun randomLong(rangeTo: ClosedRange<Long>): Long

    fun randomLong(start: Long = Properties.minLong, end: Long = Properties.maxLong): Long

}
