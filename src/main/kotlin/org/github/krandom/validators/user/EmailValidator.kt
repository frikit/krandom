package org.github.krandom.validators.user

import org.github.krandom.validators.network.IPv6Validator
import java.util.regex.Pattern

object EmailValidator {

    val PATTERN: Pattern =
            Pattern.compile(
                    "(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'" +
                            "*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f" +
                            "\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c" +
                            "\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+" +
                            "[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]" +
                            "|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9]" +
                            "[0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f" +
                            "\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"
            )

    fun validate(values: List<String>): Boolean {
        return values.all {
            if (!PATTERN.matcher(it).matches()) {
                //if don't match regex, but it is valid with ipv6 format
                val domain = it.split("@")[1]
                        .replace("[", "")
                        .replace("]", "")
                //if it is valid ipv6 inside [] I consider as valid email address
                IPv6Validator.validate(domain)
            } else {
                true
            }
        }
    }

    fun validate(value: String): Boolean {
        return validate(listOf(value))
    }
}