package com.drbrosdev.battle.pokemon

@JvmInline
value class Happiness(val value: Int = BASE_VALUE) {
    init {
        require(value in RANGE)
    }

    companion object {
        const val BASE_VALUE = 50
        val RANGE = 0..255
    }
}