package org.github.krandom.common

import org.github.krandom.properties.Properties

interface KRandomInt {

    fun randomInt(): Int

    fun randomInt(rangeTo: ClosedRange<Int>): Int

    fun randomInt(start: Int = Properties.MIN_INT, end: Int = Properties.MAX_INT): Int

}
