package org.github.krandom.user

import org.github.krandom.exceptions.NegativeSizeException
import org.github.krandom.exceptions.SizeLimitExceedException
import org.github.krandom.testhelper.Constants
import org.github.krandom.testhelper.Constants.generateValues
import org.github.krandom.testhelper.Constants.userSize
import org.github.krandom.testhelper.UserUtils
import org.github.krandom.testhelper.UserUtils.validateName
import org.github.krandom.user.BaseUserGenerator.propName
import org.github.krandom.user.BaseUserGenerator.propNames
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object SurNameTest : Spek({

    run {
        propName = "surname"
        propNames = "surnames"
    }

    describe("a user randomizer") {
        val kRandomUser: KRandomUser<String> = SurName()

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
                    UserUtils.validateNames(name)
                }
            }
        }
       describe("generate user $propNames($userSize)") {
            (1..generateValues).forEach { _ ->
                val name: List<String> = kRandomUser.randomDatas(userSize)
                assert(name.size == userSize) { "${name.size} != $userSize" }
                it(" ${name[0]} should be valid name") {
                    UserUtils.validateNames(name)
                }
            }
        }
       describe("generate user $propNames(${Constants.overflowUserSizePlus})") {
            try {
                kRandomUser.randomDatas(Constants.overflowUserSizePlus)
                assert(false) { "Should be runtime exception on line above" }
            } catch (see: SizeLimitExceedException) {
                assert(true) { "Exception should throw" }
            }
        }
       describe("generate user $propNames(${Constants.overflowUserSizeMinus})") {
            try {
                kRandomUser.randomDatas(Constants.overflowUserSizeMinus)
                assert(false) { "Should be runtime exception on line above" }
            } catch (see: NegativeSizeException) {
                assert(true) { "Exception should throw" }
            }
        }
        }

})
