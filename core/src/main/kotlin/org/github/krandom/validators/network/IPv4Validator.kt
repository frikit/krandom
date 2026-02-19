/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.validators.network

import org.apache.commons.validator.routines.InetAddressValidator
import org.github.krandom.validators.Validator

object IPv4Validator : Validator {

    private val validator: InetAddressValidator = InetAddressValidator.getInstance()

    override fun validate(value: String): Boolean {
        return validator.isValidInet4Address(value)
    }
}
