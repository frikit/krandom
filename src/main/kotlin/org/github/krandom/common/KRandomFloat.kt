package org.github.krandom.common

import org.github.krandom.properties.Properties

interface KRandomFloat {

    fun randomFloat(): Float

    fun randomFloat(rangeTo: ClosedRange<Float>): Float

    fun randomFloat(start: Float = Properties.minFloat, end: Float = Properties.maxFloat): Float

}
