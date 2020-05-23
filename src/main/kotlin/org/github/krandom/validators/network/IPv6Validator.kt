package org.github.krandom.validators.network

import org.apache.commons.validator.routines.InetAddressValidator
import org.github.krandom.validators.Validator

object IPv6Validator : Validator {

    private val validator: InetAddressValidator = InetAddressValidator.getInstance()

    override fun validate(value: String): Boolean {
        return validator.isValid(value)
    }
}
