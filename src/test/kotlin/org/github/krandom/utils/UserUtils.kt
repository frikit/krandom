package org.github.krandom.utils

import mu.KLogger
import mu.KLogging

object UserUtils {
    private val logger: KLogger = KLogging().logger(UserUtils::class.java.simpleName)

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
        for ((index, it) in name.withIndex()) {
            try {
                validateName(it, numbers)
            } catch (e: Throwable) {
                logger.error { "Empty value found with index [$index] and value [$it]" }
            }
        }
    }
}
