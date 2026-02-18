package org.github.krandom.user

import org.github.krandom.testhelper.Constants.generateValues
import org.github.krandom.testhelper.Constants.overflowUserSizeMinus
import org.github.krandom.testhelper.Constants.overflowUserSizePlus
import org.github.krandom.testhelper.Constants.userSize
import org.github.krandom.testhelper.UserUtils.validateEmail
import org.github.krandom.testhelper.UserUtils.validateEmails
import org.github.krandom.user.BaseUserGenerator.propName
import org.github.krandom.user.BaseUserGenerator.propNames
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.assertions.throwables.shouldThrow

class EmailSpek : DescribeSpec({

    propName = "email"
    propNames = "emails"

    describe("a user randomizer domains=default") {
        val kRandomUser = Email()
        val actualDomains = kRandomUser.domains
        val expectedDomains = Email.DEFAULT_DOMAINS

        it(" no param provided should return default domains") {
            assert(actualDomains == expectedDomains) {
                "When init Email without domains should be default one default=$expectedDomains != actual=$actualDomains"
            }
        }

        describe("generate user $propName") {
            (1..generateValues).forEach { _ ->
                val value = kRandomUser.randomData()
                it(" $value should be valid $propName") {
                    validateEmail(value, actualDomains)
                }
            }
        }

        describe("generate user $propNames") {
            (1..generateValues).forEach { _ ->
                val value = kRandomUser.randomDatas()
                it(" ${value.size} all should be valid $propName") {
                    validateEmails(value, actualDomains)
                }
            }
        }

        describe("generate user $propNames($userSize)") {
            (1..generateValues).forEach { _ ->
                val value = kRandomUser.randomDatas(userSize)
                it("should be right size ${value.size} == $userSize") {
                    assert(value.size == userSize) { "${value.size} != $userSize" }
                }
                it(" ${value[0]} should be valid $propName") {
                    validateEmails(value, actualDomains)
                }
            }
        }
    }

    describe("a user randomizer domains=default") {
        val kRandomUser = Email()
        val domains = kRandomUser.domains

        describe("generate user $propName") {
            (1..generateValues).forEach { _ ->
                val value = kRandomUser.randomData()
                it(" $value should be valid $propName") {
                    validateEmail(value, domains)
                }
            }
        }

        describe("generate user $propNames") {
            (1..generateValues).forEach { _ ->
                val value = kRandomUser.randomDatas()
                it(" ${value.size} all should be valid $propName") {
                    validateEmails(value, domains)
                }
            }
        }

        describe("generate user $propNames($userSize)") {
            (1..generateValues).forEach { _ ->
                val value = kRandomUser.randomDatas(userSize)
                it("should be right size ${value.size} == $userSize") {
                    assert(value.size == userSize) { "${value.size} != $userSize" }
                }
                it(" ${value[0]} should be valid $propName") {
                    validateEmails(value, domains)
                }
            }
        }
    }

    describe("a user randomizer domains=custom") {
        val kRandomUser = Email(listOf(
                //ip v4
                "163.171.16.2",
                "164.171.16.2",
                "165.171.16.2",
                "166.171.16.2",
                //ip v6
                "2001:0db8:85a3:0000:0000:8a2e:0370:7334",
                "2001:db8:85a3::8a2e:370:7334",
                "2001:db8:1234:ffff:ffff:ffff:ffff:ffff"
        ))
        val domains = kRandomUser.domains

        describe("generate user $propName") {
            (1..generateValues).forEach { _ ->
                val value = kRandomUser.randomData()
                it(" $value should be valid $propName") {
                    validateEmail(value, domains)
                }
            }
        }

        describe("generate user $propNames") {
            (1..generateValues).forEach { _ ->
                val value = kRandomUser.randomDatas()
                it(" ${value.size} all should be valid $propName") {
                    validateEmails(value, domains)
                }
            }
        }

        describe("generate user $propNames($userSize)") {
            (1..generateValues).forEach { _ ->
                val value = kRandomUser.randomDatas(userSize)
                it("should be right size ${value.size} == $userSize") {
                    assert(value.size == userSize) { "${value.size} != $userSize" }
                }
                it(" ${value[0]} should be valid $propName") {
                    validateEmails(value, domains)
                }
            }
        }
    }

    describe("a user randomizer(fail test cases)") {
        val kRandomUser = Email(listOf("victor.dev"))

        describe("generate user $propNames($overflowUserSizePlus)") {
            it("should throw exception") {
                shouldThrow<IllegalArgumentException> {
                    kRandomUser.randomDatas(overflowUserSizePlus)
                }
            }
        }

        describe("generate user $propNames($overflowUserSizeMinus)") {
            it("should throw exception") {
                shouldThrow<IllegalArgumentException> {
                    kRandomUser.randomDatas(overflowUserSizeMinus)
                }
            }
        }

        describe("generate user with empty domain list") {
            it("should throw exception") {
                shouldThrow<IllegalArgumentException> {
                    Email(emptyList())
                }
            }
        }

        describe("generate user with one empty element in domain list") {
            it("should throw exception") {
                shouldThrow<IllegalArgumentException> {
                    Email(listOf("test", "", "test2"))
                }
            }
        }
    }

})
