/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples.android.contacts

import android.net.Uri

/** A contact as an app keeps it, with the `content://` address of its photo. */
data class Contact(
    val id: Long,
    val name: String,
    val email: String,
    val photo: Uri,
)
