package org.github.krandom.testhelper

object LuhnUtils {

    fun checkValidLuhnNumber(str: String): Boolean {

        val length = IntArray(str.length)
        for (i in str.indices) {
            length[i] = Integer.parseInt(str.substring(i, i + 1))
        }
        run {
            var i = length.size - 2
            while (i >= 0) {
                var j = length[i]
                j *= 2
                if (j > 9) {
                    j = j % 10 + 1
                }
                length[i] = j
                i -= 2
            }
        }
        var sum = 0
        for (i in length.indices) {
            sum += length[i]
        }
        return sum % 10 == 0
    }
}
