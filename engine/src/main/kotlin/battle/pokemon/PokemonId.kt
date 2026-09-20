package com.drbrosdev.battle.pokemon

import java.util.UUID

@JvmInline
value class PokemonId private constructor(val id: String) {
    init {
        require(id.isNotBlank()) {
            "Pokemon $id has no ownership!"
        }
        require(id.contains("-")) {
            "Pokemon $id does not contain a discriminator!"
        }
    }

    override fun toString(): String = id

    fun baseId(): String = id.split("-").dropLast(1).joinToString(separator = "-") { it }

    companion object {
        operator fun invoke(id: String): PokemonId {
            val slug = UUID.randomUUID()
                .toString()
                .take(4)
            return PokemonId("$id-$slug")
        }

        // NOTE: only used in tests
        fun of(value: String) = PokemonId(value)
    }
}