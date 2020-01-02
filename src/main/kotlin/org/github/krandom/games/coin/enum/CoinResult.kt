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
