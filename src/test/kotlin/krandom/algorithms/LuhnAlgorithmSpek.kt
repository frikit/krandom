package krandom.algorithms

import krandom.utils.LuhnUtils
import krandom.utils.TestLifecycle
import mu.KLogger
import mu.KLogging
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object LuhnAlgorithmSpek : Spek({

    val logger: KLogger = KLogging().logger(LuhnAlgorithmSpek::class.java.simpleName)

    TestLifecycle.onTestStart("generate random luhn number from scratch")
    describe("generate random double in range(start, end)") {
        var number: String
        (1..1_000).onEach {
            number = LuhnAlgorithm.randomNumber()
            TestLifecycle.onTestStep(logger, "generated : [$number]")
            it("$number valid luhn algorithm checked") {
                //basic check
                assert(number.length == 10)
                assert(!number.contains("0"))
                assert(number.matches("0-9".toRegex()))

                //advanced check
                assert(LuhnUtils.checkValidLuhnNumber(number))
            }
        }
    }
    TestLifecycle.onTestFinish("generate random luhn number from scratch")
})
