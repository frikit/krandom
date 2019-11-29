package org.github.krandom.testhelper

object UserUtils {

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
}
