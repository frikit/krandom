package org.github.krandom.common

import org.github.krandom.properties.Properties

interface KRandomByte {

    fun randomByte(): Byte

    fun randomByte(rangeTo: ClosedRange<Byte>): Byte

    fun randomByte(start: Byte = Properties.MIN_BYTE, end: Byte = Properties.MAX_BYTE): Byte
}
