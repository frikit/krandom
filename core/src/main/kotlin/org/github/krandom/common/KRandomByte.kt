/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.common

import org.github.krandom.properties.Properties

interface KRandomByte {

    fun randomByte(): Byte

    fun randomByte(rangeTo: ClosedRange<Byte>): Byte

    fun randomByte(start: Byte = Properties.MIN_BYTE, end: Byte = Properties.MAX_BYTE): Byte
}
