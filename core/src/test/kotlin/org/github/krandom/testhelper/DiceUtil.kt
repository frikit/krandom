/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.testhelper

object DiceUtil {

    fun generateRegEx(start: Int, end: Int): Regex {
        //^(1|2|3|4|){1}$
        val res = StringBuilder("^(")
        for (i in start..end) {
            res.append(i.toString())
            res.append("|")
        }

        res.append("){1}\$")

        return res.toString().toRegex()
    }

    fun generateExpectedValues(start: Int, end: Int): ArrayList<Int> {
        val res = arrayListOf<Int>()
        for (i in start..end) {
            res.add(i)
        }

        return res
    }

    fun generateInvalidValues(curr: Int): ArrayList<MutableCollection<Int>> {
        val res = arrayListOf<MutableCollection<Int>>()
        val unaDin = arrayListOf<Int>()
        for (i in 0..9) {
            for (j in 0..i) {
                unaDin.add(j)
            }
            if (curr != unaDin.size) {
                res.add(unaDin.toMutableList())
            }
            unaDin.clear()
        }

        return res
    }

    fun getRollTimes(diceSize: Int): Int = diceSize * 5

}
