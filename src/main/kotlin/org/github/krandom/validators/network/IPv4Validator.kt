package org.github.krandom.validators.network

import org.apache.commons.validator.routines.InetAddressValidator

object IPv4Validator {

    private val validator: InetAddressValidator = InetAddressValidator.getInstance()

    fun validate(value: String): Boolean {
        return validator.isValidInet4Address(value)
    }
}