package org.github.krandom.utils

fun generateRandomString(function: () -> String, numbers: Boolean): String {
    var res: String = function.invoke()
    if (numbers) {
        for (i in 0..99) {
            if (!res.contains("[0-9]+".toRegex())) {
                res = function.invoke()
            } else {
                return res
            }
        }
        res = ""
    }
    return res
}
