/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.games.coin.enum

data class CoinResult(val value: String) {

    companion object {
        val HEAD = CoinResult("head")
        val TAIL = CoinResult("tail")
    }

    override fun toString(): String {
        return value
    }

}
