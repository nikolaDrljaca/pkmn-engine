package com.drbrosdev.battle.move

import com.drbrosdev.battle.pokemon.Element
import com.drbrosdev.battle.pokemon.MajorStatus
import com.drbrosdev.battle.pokemon.Pokemon
import com.drbrosdev.battle.pokemon.PokemonDsl
import com.drbrosdev.battle.pokemon.stats.StatKey
import com.drbrosdev.battle.pokemon.stats.StatModifier
import com.drbrosdev.battle.pokemon.stats.StatModifiers
import java.util.*

data class Move(
    val id: String,
    val name: String,

    val element: Element,
    val power: Int, // NOTE: Status moves have no power
    val powerPoints: Int,
    val accuracy: MoveAccuracy,
    val type: MoveType, // physical, status, special

    val priority: MovePriority = MovePriority(),

    // this needs to become an Impl of MoveEffect!
    // describes what the move does
    val effect: MoveEffect,

    // not used in MoveEffect, but as part of turn validation
    val status: MoveStatus = MoveStatus.NORMAL
)

sealed interface MovePower {
    // Standard power
    @JvmInline
    value class Power(val value: Int) : MovePower

    // Moves like Dragon Rage / Sonic Boom deal Direct HP damage
    @JvmInline
    value class Direct(val value: Int): MovePower

    // Status Moves have no power
    data object NoPower: MovePower
}

fun Move.isStatusMove() = type == MoveType.STATUS
fun Move.isPhysicalMove() = type == MoveType.PHYSICAL
fun Move.isSpecialMove() = type == MoveType.SPECIAL

enum class MoveStatus {
    NORMAL,
    DISABLED
}

@JvmInline
value class MovePriority(val value: Int = DEFAULT) {
    init {
        require(value in MIN_PRIORITY..MAX_PRIORITY) {
            "MovePriority $value is outside defined range ($MIN_PRIORITY, $MAX_PRIORITY)!"
        }
    }

    companion object {
        const val DEFAULT = 0
        const val MAX_PRIORITY = 5
        const val MIN_PRIORITY = -7
    }
}

@PokemonDsl
class MoveBuilder {

    var id: String = UUID.randomUUID().toString()
    var name: String = "Test Move"
    var element: Element = Element.NORMAL
    var power: Int = 50
    var powerPoints: Int = 10
    var accuracy: MoveAccuracy = MoveAccuracy.AlwaysHit
    var type: MoveType = MoveType.PHYSICAL
    var effect: MoveEffect = NoEffect
    var status: MoveStatus = MoveStatus.NORMAL
    var priority: Int = 0

    fun percentAccuracy(value: Int) {
        this.accuracy = MoveAccuracy.Percent(Percentage(value))
    }

    fun alwaysHit() {
        this.accuracy = MoveAccuracy.AlwaysHit
    }

    fun sequentialEffect(vararg effects: MoveEffect) {
        this.effect = SequenceMoveEffect(effects.toList())
    }

    fun physical() {
        this.type = MoveType.PHYSICAL
    }

    fun special() {
        this.type = MoveType.SPECIAL
    }

    fun status() {
        this.type = MoveType.STATUS
    }

    fun build() = Move(
        id = id,
        name = name,
        element = element,
        type = type,
        powerPoints = powerPoints,
        power = power,
        accuracy = accuracy,
        effect = effect,
        status = status,
        priority = MovePriority(priority)
    )
}

fun buildMove(block: MoveBuilder.() -> Unit): Move =
    MoveBuilder().apply(block).build()

data class MoveContext(
    val user: Pokemon,
    val target: Pokemon,
    val move: Move
)


// Eg example move
private val flamethrower = Move(
    id = "flamethrower",
    name = "Flamethrower",
    element = Element.FIRE,
    power = 90,
    powerPoints = 18,
    accuracy = MoveAccuracy.Percent(Percentage(100)),
    type = MoveType.SPECIAL,
    effect = SequenceMoveEffect(
        listOf(
            ApplyFormulaDamage,
            ApplyStatusCondition(Percentage(10), MajorStatus.Burned)
        )
    )
)

val Tackle = buildMove {
    id = "tackle"
    name = "Tackle"
    element = Element.NORMAL
    power = 40
    powerPoints = 35
    percentAccuracy(95)
    type = MoveType.PHYSICAL
    sequentialEffect(ApplyFormulaDamage)
}

val Scratch = buildMove {
    id = "scratch"
    name = "Scratch"
    element = Element.NORMAL
    power = 40
    powerPoints = 35
    percentAccuracy(100)
    type = MoveType.PHYSICAL
    sequentialEffect(ApplyFormulaDamage)
}

val Leer = buildMove {
    id = "leer"
    name = "Leer"
    element = Element.NORMAL
    power = 0
    powerPoints = 30
    percentAccuracy(100)
    type = MoveType.STATUS
    sequentialEffect(
        ApplyStatModification { StatModifiers(mapOf(StatKey.DEFENCE to StatModifier.Stage(-1))) }
    )
}

val Growl = buildMove {
    id = "growl"
    name = "Growl"
    element = Element.NORMAL
    power = 0
    powerPoints = 30
    percentAccuracy(100)
    type = MoveType.STATUS
    sequentialEffect(
        ApplyStatModification { StatModifiers(mapOf(StatKey.ATTACK to StatModifier.Stage(-1))) }
    )
}

val Pursuit = buildMove {
    id = "pursuit"
    name = "Pursuit"
    element = Element.DARK
    power = 40
    powerPoints = 20
    percentAccuracy(100)
    type = MoveType.PHYSICAL
    sequentialEffect(ApplyFormulaDamage)
}
