package org.github.krandom.validators.user

import org.apache.commons.validator.routines.EmailValidator
import org.github.krandom.validators.Validator

object EmailValidator : Validator {

    private val validator: EmailValidator = EmailValidator.getInstance(true, true)

    override fun validate(value: String): Boolean {
        return validator.isValid(value)
    }
}
