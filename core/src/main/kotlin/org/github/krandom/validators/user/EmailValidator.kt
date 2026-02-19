/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.validators.user

import org.apache.commons.validator.routines.EmailValidator
import org.github.krandom.validators.Validator

object EmailValidator : Validator {

    private val validator: EmailValidator = EmailValidator.getInstance(true, true)

    override fun validate(value: String): Boolean {
        return validator.isValid(value)
    }
}
