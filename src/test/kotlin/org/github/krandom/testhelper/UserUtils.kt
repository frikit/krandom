package org.github.krandom.testhelper

import org.github.krandom.user.DummyEnum
import org.github.krandom.user.enum.TitleResult
import org.github.krandom.validators.network.IPv4Validator
import org.github.krandom.validators.network.IPv6Validator
import org.github.krandom.validators.user.EmailValidator
import org.github.krandom.validators.user.SocialSecurityNumberValidator

object UserUtils {

    fun validateAge(age: Int) {
        assert(age in 0..100) { "Age should be between 0-100! [$age]" }
    }

    fun validateAges(ages: List<Int>) {
        for (it in ages) {
            validateAge(it)
        }
    }

    fun validateName(name: String, numbers: Boolean = false) {
        if (numbers) {
            assert(name.contains(Regex("[0-9]+"))) { "Name should contain numbers! [$name]" }
        } else {
            assert(!name.contains(Regex("[0-9]+"))) { "Name should not contain numbers! [$name]" }
        }
        assert(!name.contains(Regex("[!@#$%^&*()_+]"))) { "Name should not contain special chars expect \'! [$name]" }
        assert(name.contains(Regex("[^\\s+$]+"))) { "Name should not contain white spaces! [$name]" }
    }

    fun validateNames(name: List<String>, numbers: Boolean = false) {
        for (it in name) {
            validateName(it, numbers)
        }
    }

    fun validateGender(gender: String, validValues: List<String>) {
        assert(validValues.contains(gender)) { "Gender $gender should be one of these $validValues" }
    }

    fun validateGenders(gender: List<String>, validValues: List<String>) {
        for (it in gender) {
            validateGender(it, validValues)
        }
    }

    fun validateTitle(title: TitleResult, validValues: List<TitleResult>) {
        assert(title.title.isNotEmpty()) { "Title cant be empty!" }
        assert(title.fullTitle.isNotEmpty()) { "FullTitle cant be empty!" }
        assert(title.description.isNotEmpty()) { "Description cant be empty!" }

        if (title.title.length <= 3) {
            assert(title.getTitle(true).endsWith(".")) { "Short titles should end with ." }
        } else {
            assert(!title.getTitle(true).endsWith(".")) { "Long titles should not end with ." }
        }

        assert(!title.getTitle().endsWith(".")) { "UK style titles should not end with ." }

        assert(validValues.contains(title)) { "Title $title should be one of these $validValues" }
    }

    fun validateTitles(title: List<TitleResult>, validValues: List<TitleResult>) {
        for (it in title) {
            validateTitle(it, validValues)
        }
    }

    fun validateCustomTitle(title: DummyEnum, validValues: List<DummyEnum>) {
        assert(title.title.isNotEmpty()) { "Title cant be empty!" }
        assert(validValues.contains(title)) { "Title $title should be one of these $validValues" }
    }

    fun validateCustomTitles(title: List<DummyEnum>, validValues: List<DummyEnum>) {
        for (it in title) {
            validateCustomTitle(it, validValues)
        }
    }

    fun validateEmail(email: String, domains: List<String>) {
        assert(EmailValidator.validate(email)) { "Not valid email '$email'!" }

        val domainEmail = email.split("@")[1].toLowerCase()
        val normalized = domains.map {
            if (IPv4Validator.validate(it) || IPv6Validator.validate(it)) {
                "[" + it.toLowerCase() + "]"
            } else {
                it.toLowerCase()
            }
        }
        assert(normalized.contains(domainEmail)) { "Generated email '$domainEmail' is not from domains=$normalized" }
    }

    fun validateEmails(emails: List<String>, domains: List<String>) {
        for (it in emails) {
            validateEmail(it, domains)
        }
    }

    fun validateSSN(ssn: String) {
        assert(SocialSecurityNumberValidator.validate(ssn)) { "Not valid ssn '$ssn'!" }
    }

    fun validateSSNs(ssns: List<String>) {
        for (it in ssns) {
            validateSSN(it)
        }
    }
}
