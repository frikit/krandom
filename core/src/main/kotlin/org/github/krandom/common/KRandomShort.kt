/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.common

import org.github.krandom.properties.Properties

interface KRandomShort {

    fun randomShort(): Short

    fun randomShort(rangeTo: ClosedRange<Short>): Short

    fun randomShort(start: Short = Properties.MIN_SHORT, end: Short = Properties.MAX_SHORT): Short
}
