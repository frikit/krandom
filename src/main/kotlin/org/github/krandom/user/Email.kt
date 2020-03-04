package org.github.krandom.user

import org.github.krandom.common.Randomizer
import org.github.krandom.user.common.GenericUserGenerator
import org.github.krandom.validators.network.IPv4Validator
import org.github.krandom.validators.network.IPv6Validator

class Email(val domains: List<String> = DEFAULT_DOMAINS) : KRandomUser<String>, GenericUserGenerator<String>() {

    private val kRandomInt = Randomizer()

    private val validInBetweenNameAndSurnameChars = listOf(".", "-", "", "_", "+")
    private val firstName = FirstName()
    private val surname = SurName()

    companion object {
        val DEFAULT_DOMAINS = listOf(
                "test.com",
                "example.com",
                "example.org",
                "example.net",
                "test.invalid"
        )
    }

    init {
        require(domains.isNotEmpty()) { "Domains list should not be empty!" }
        domains.forEach {
            require(it.trim().isNotBlank()) { "Domain name can't be empty or null or contains only white spaces!" }
        }
    }

    private fun randomIndexInList(list: List<String>): Int {
        return kRandomInt.randomInt(0, list.size)
    }

    private fun randomDomainAddresses(size: Int, domains: List<String>): List<String> {
        return (1..size).map {
            val idx = randomIndexInList(domains)
            val value = domains[idx]

            if (IPv4Validator.validate(value)
                    || IPv6Validator.validate(value)) {
                "[$value]"
            } else {
                value
            }
        }.toList()
    }

    override fun randomData(): String {
        return randomDatas(1).first()
    }

    override fun randomDatas(size: Int): List<String> {
        isValidSize(size)
        val name = firstName.randomDatas(size)
        val between = validInBetweenNameAndSurnameChars[randomIndexInList(validInBetweenNameAndSurnameChars)]
        val surname = surname.randomDatas(size)
        val number = (1..size).map { kRandomInt.randomInt(1, 9999) }.toList()
        val at = "@"
        val domain = randomDomainAddresses(size, domains)
        return (0 until size).map {
            name[it].toLowerCase() + between + surname[it].toLowerCase() + number[it] + at + domain[it]
        }
                .map { it.trim() }
                .toList()
    }

}