/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.common

import org.github.krandom.properties.Properties

interface KRandomFloat {

    fun randomFloat(): Float

    fun randomFloat(rangeTo: ClosedRange<Float>): Float

    fun randomFloat(start: Float = Properties.MIN_FLOAT, end: Float = Properties.MAX_FLOAT): Float

}
