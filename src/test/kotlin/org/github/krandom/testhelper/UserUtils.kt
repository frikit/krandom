package org.github.krandom.testhelper

object UserUtils {

    fun validateName(name: String, numbers: Boolean = false) {
        if (numbers) {
            assert(name.contains(Regex("[0-9]+"))) { "Name should not contains numbers! [$name]" }
        } else {
            assert(!name.contains(Regex("[0-9]+"))) { "Name should not contains numbers! [$name]" }
        }
        assert(!name.contains(Regex("[!@#$%^&*()_+]"))) { "Name should not contains special chars expect \'! [$name]" }
        assert(name.contains(Regex("[^\\s+$]+"))) { "Name should not contains white spaces! [$name]" }
    }

    fun validateNames(name: List<String>, numbers: Boolean = false) {
        for (it in name) {
            validateName(it, numbers)
        }
    }
}
