package org.github.krandom.validators.user

import org.github.krandom.validators.Validator

object SocialSecurityNumberValidator : Validator {

    private val regexWithDashes = "(?!\\b(\\d)1+-(\\d)1+-(\\d)1+\\b)(?!123-45-6789|219-09-9999|078-05-1120)(?!666|000|9\\d{2})\\d{3}-(?!00)\\d{2}-(?!0{4})\\d{4}".toRegex()
    private val regexWithoutDashes = "^(?!\\b(\\d)1+\\b)(?!123456789|219099999|078051120)(?!666|000|9\\d{2})\\d{3}(?!00)\\d{2}(?!0{4})\\d{4}\$".toRegex()

    override fun validate(value: String): Boolean {
        return if (value.contains('-'))
            value.matches(regexWithDashes)
        else
            value.matches(regexWithoutDashes)
    }
}
