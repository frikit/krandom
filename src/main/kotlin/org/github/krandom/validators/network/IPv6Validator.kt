package org.github.krandom.validators.network

import org.apache.commons.validator.routines.InetAddressValidator

object IPv6Validator {

    private val validator: InetAddressValidator = InetAddressValidator.getInstance()

    fun validate(value: String): Boolean {
        return validator.isValid(value)
    }
}