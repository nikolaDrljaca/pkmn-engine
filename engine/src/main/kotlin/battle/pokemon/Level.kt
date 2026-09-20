package com.drbrosdev.battle.pokemon

@JvmInline
value class Level(val value: Int = CURRENT) {
    init {
        require(value in 1..MAX)
    }

    override fun toString(): String = value.toString()

    companion object {
        const val CURRENT = 50
        const val MAX = 100
    }
}