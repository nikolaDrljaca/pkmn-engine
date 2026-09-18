package com.drbrosdev.battle.environment.hazard


@JvmInline
value class Layers private constructor(val count: Int) {
    fun stack(max: Int): Layers = Layers((count + 1).coerceAtMost(max))

    companion object {
        val One = Layers(1)
    }
}
