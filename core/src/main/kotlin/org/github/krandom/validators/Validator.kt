package org.github.krandom.validators

interface Validator {

    fun validate(value: String): Boolean
}
