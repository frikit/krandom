package krandom.user

import krandom.KRandomUser
import krandom.utils.Constants.generateValues
import krandom.utils.TestLifecycle
import krandom.utils.UserUtils.validateName
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.on

class FirstNameTest : Spek({

    describe("a user randomizer") {
        val kRandomUser: KRandomUser<String> = FirstName()

        TestLifecycle.onTestStart("generate user name")
        on("generate user name") {
            (1..generateValues).forEach {
                val name: String = kRandomUser.randomData()
                validateName(name)
            }
        }
        TestLifecycle.onTestFinish("generate user name")

        TestLifecycle.onTestStart("generate user names")
        on("generate user names") {
            (1..generateValues).forEach {
                val name: List<String> = kRandomUser.randomDatas()
                name.forEach { validateName(it) }
            }
        }
        TestLifecycle.onTestFinish("generate user names")

        TestLifecycle.onTestStart("generate user names")
        on("generate user names") {
            (1..generateValues).forEach {
                val name: List<String> = kRandomUser.randomDatas(10)
                assert(name.size == 10)
                name.forEach { validateName(it) }
            }
        }
        TestLifecycle.onTestFinish("generate user names")
    }

})