package org.github.krandom.user

import org.github.krandom.exceptions.NegativeSizeException
import org.github.krandom.exceptions.SizeLimitExceedException
import org.github.krandom.testhelper.Constants.generateValues
import org.github.krandom.testhelper.Constants.overflowUserSizeMinus
import org.github.krandom.testhelper.Constants.overflowUserSizePlus
import org.github.krandom.testhelper.Constants.userSize
import org.github.krandom.testhelper.UserUtils.validateName
import org.github.krandom.testhelper.UserUtils.validateNames
import org.github.krandom.user.BaseUserGenerator.propName
import org.github.krandom.user.BaseUserGenerator.propNames
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


object FirstNameTest : Spek({

    run {
        propName = "name"
        propNames = "names"
    }

    describe("a user randomizer") {
        val kRandomUser: KRandomUser<String> = FirstName()

       describe("generate user $propName") {
            (1..generateValues).forEach { _ ->
                val name: String = kRandomUser.randomData()
                it(" $name should be valid $propName") {
                    validateName(name)
                }
            }
        }
       describe("generate user $propNames") {
            (1..generateValues).forEach { _ ->
                val name: List<String> = kRandomUser.randomDatas()
                it(" ${name.size} all should be valid name") {
                    validateNames(name)
                }
            }
        }
       describe("generate user $propNames($userSize)") {
            (1..generateValues).forEach { _ ->
                val name: List<String> = kRandomUser.randomDatas(userSize)
                assert(name.size == userSize) { "${name.size} != $userSize" }
                it(" ${name[0]} should be valid name") {
                    validateNames(name)
                }
            }
        }
       describe("generate user $propNames($overflowUserSizePlus)") {
            try {
                kRandomUser.randomDatas(overflowUserSizePlus)
                assert(false) { "Should be runtime exception on line above" }
            } catch (see: SizeLimitExceedException) {
                assert(true) { "Exception should throw" }
            }
        }
       describe("generate user $propNames($overflowUserSizeMinus)") {
            try {
                kRandomUser.randomDatas(overflowUserSizeMinus)
                assert(false) { "Should be runtime exception on line above" }
            } catch (see: NegativeSizeException) {
                assert(true) { "Exception should throw" }
            }
        }
        }

})
