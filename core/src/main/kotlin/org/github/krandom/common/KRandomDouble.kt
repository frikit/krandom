package org.github.krandom.common

import org.github.krandom.properties.Properties

interface KRandomDouble {

    fun randomDouble(): Double

    fun randomDouble(rangeTo: ClosedRange<Double>): Double

    fun randomDouble(start: Double = Properties.MIN_DOUBLE, end: Double = Properties.MAX_DOUBLE): Double

}
