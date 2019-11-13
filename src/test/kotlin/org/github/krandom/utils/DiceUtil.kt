package org.github.krandom.utils

import java.lang.StringBuilder

object DiceUtil {

    fun generateRegEx(start: Int, end: Int) : Regex {
        //^(1|2|3|4|){1}$
        val res: StringBuilder = StringBuilder("^(")
        for (i in start..end) {
            res.append(i.toString())
            res.append("|")
        }

        res.append("){1}\$")

        return res.toString().toRegex()
    }

    fun generateExpectedValues(start: Int, end: Int) : List<String> {
        val res = arrayListOf<String>()
        for (i in start..end) {
            res.add(i.toString())
        }

        return res
    }
}
