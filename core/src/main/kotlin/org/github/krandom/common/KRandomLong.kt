/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.common

import org.github.krandom.properties.Properties

interface KRandomLong {

    fun randomLong(): Long

    fun randomLong(rangeTo: ClosedRange<Long>): Long

    fun randomLong(start: Long = Properties.MIN_LONG, end: Long = Properties.MAX_LONG): Long

}
