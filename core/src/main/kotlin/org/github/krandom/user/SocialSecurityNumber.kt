/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.user

import org.github.krandom.user.common.GenericUserGenerator

class SocialSecurityNumber(
        val withDashes: Boolean = true
) : KRandomUser<String>, GenericUserGenerator<String>() {

    private fun fourNumberInt(): String = String.format("%04d", kRandomCommon.randomInt(100, 9999))

    private fun firstGroupNumbersSSN(): String = String.format("%03d", kRandomCommon.randomInt(1, 665))

    private fun secondGroupNumbersSSN(): String = String.format("%02d", kRandomCommon.randomInt(1, 99))

    private fun dashBetween(): String = if (withDashes) "-" else ""

    private fun fullSSN(): String = firstGroupNumbersSSN() + dashBetween() + secondGroupNumbersSSN() + dashBetween() + fourNumberInt()

    override fun randomData(): String {
        return randomDatas(1).first()
    }

    override fun randomDatas(size: Int): List<String> {
        isValidSize(size)

        return (0 until size).map { fullSSN() }.toList()
    }
}
