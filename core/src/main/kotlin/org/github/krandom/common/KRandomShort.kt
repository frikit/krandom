package org.github.krandom.common

import org.github.krandom.properties.Properties

interface KRandomShort {

    fun randomShort(): Short

    fun randomShort(rangeTo: ClosedRange<Short>): Short

    fun randomShort(start: Short = Properties.MIN_SHORT, end: Short = Properties.MAX_SHORT): Short
}
