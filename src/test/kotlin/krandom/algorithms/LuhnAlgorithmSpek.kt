package krandom.algorithms

import krandom.utils.TestLifecycle
import mu.KLogger
import mu.KLogging
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

fun checkValidLuhnNumber(str: String): Boolean {

    val length = IntArray(str.length)
    for (i in 0 until str.length) {
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


class LuhnAlgorithmSpek : Spek({

    val logger: KLogger = KLogging().logger(LuhnAlgorithmSpek::class.java.simpleName)

    TestLifecycle().onTestStart("generate random luhn number from scratch")
    on("generate random double in range(start, end)") {
        var number: String
        (1..1_000).onEach {
            number = LuhnAlgorithm.randomNumber()
            TestLifecycle().onTestStep(logger, "generated : [$number]")
            it("$number valid luhn algorithm checked") {
                //basic check
                assert(number.length == 10)
                assert(!number.contains("0"))
                assert(number.matches("0-9".toRegex()))

                //advanced check
                assert(checkValidLuhnNumber(number))
            }
        }
    }
    TestLifecycle().onTestFinish("generate random luhn number from scratch")
})