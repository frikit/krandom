package org.github.krandom.testhelper

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
        assert(validValues.contains(gender)) {"Gender $gender should be one of these $validValues"}
    }

    fun validateGenders(gender: List<String>, validValues: List<String>) {
        for (it in gender) {
            validateGender(it, validValues)
        }
    }
}
