package org.github.krandom.validators.user

import org.apache.commons.validator.routines.EmailValidator

object EmailValidator {

    private val validator: EmailValidator = EmailValidator.getInstance(true, true)

    fun validate(value: String): Boolean {
        return validator.isValid(value)
    }
}