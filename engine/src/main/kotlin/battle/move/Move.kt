package com.drbrosdev.battle.move

import com.drbrosdev.battle.pokemon.Element
import com.drbrosdev.battle.pokemon.MajorStatus
import com.drbrosdev.battle.pokemon.PokemonDsl
import com.drbrosdev.battle.pokemon.VolatileStatus
import com.drbrosdev.battle.pokemon.stats.StatKey
import com.drbrosdev.battle.pokemon.stats.StatModifier
import com.drbrosdev.battle.pokemon.stats.StatModifiers
import java.util.*

data class Move(
    val id: MoveId,
    val name: String,

    val element: Element,
    val power: Int, // NOTE: Status moves have no power
    val powerPoints: Int,
    val accuracy: MoveAccuracy,
    val type: MoveType, // physical, status, special

    val priority: MovePriority = MovePriority(),
    val critApplication: CritApplication = CritApplication.Normal(MoveCritStage()),

    // not used in MoveEffect, but as part of turn validation
    val status: MoveStatus = MoveStatus.NORMAL,

    // this needs to become an Impl of MoveEffect!
    // describes what the move does
    val effect: MoveEffect,
) {

    // Mutation functions
    fun enable() = copy(status = MoveStatus.NORMAL)
    fun disable() = copy(status = MoveStatus.DISABLED)
}

fun Move.isStatusMove() = type == MoveType.STATUS
fun Move.isPhysical() = type == MoveType.PHYSICAL
fun Move.isSpecialMove() = type == MoveType.SPECIAL

enum class MoveStatus {
    NORMAL,
    DISABLED
}

@JvmInline
value class MoveId(val id: String) {
    init {
        require(id.isNotBlank()) {
            "Move cannot have a blank id!"
        }
    }

    override fun toString(): String = id
}

sealed interface CritApplication {
    data class Normal(val stage: MoveCritStage) : CritApplication

    data object Always : CritApplication
}

@JvmInline
value class MoveCritStage(val value: Int = MIN) {
    init {
        require(value in RANGE) {
            "Move Crit stage must be within $RANGE"
        }
    }

    companion object {
        const val MIN = 0
        const val MAX = 4
        val RANGE = MIN..MAX
    }
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
    var effect: MoveEffect = MoveEffect.NoEffect
    var status: MoveStatus = MoveStatus.NORMAL
    var priority: Int = 0
    private var critApplication: CritApplication = CritApplication.Normal(MoveCritStage())

    fun percentAccuracy(value: Int) {
        this.accuracy = MoveAccuracy.Percent(Percentage(value))
    }

    fun alwaysHit() {
        this.accuracy = MoveAccuracy.AlwaysHit
    }

    fun effects(vararg effects: MoveEffect) {
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

    fun alwaysCrits() {
        this.critApplication = CritApplication.Always
    }

    fun critStage(stage: Int) {
        this.critApplication = CritApplication.Normal(MoveCritStage(stage))
    }

    fun build() = Move(
        id = MoveId(id),
        name = name,
        element = element,
        type = type,
        powerPoints = powerPoints,
        power = power,
        accuracy = accuracy,
        effect = effect,
        status = status,
        priority = MovePriority(priority),
        critApplication = critApplication
    )
}

fun buildMove(block: MoveBuilder.() -> Unit): Move =
    MoveBuilder().apply(block).build()

// Eg example move
val Flamethrower = buildMove {
    id = "flamethrower"
    name = "Flamethrower"
    element = Element.FIRE
    power = 90
    powerPoints = 18
    percentAccuracy(100)
    special()
    effects(
        ApplyDamage,
        ApplyStatusCondition(Percentage(100), MajorStatus.Burned)
    )
}

val Tackle = buildMove {
    id = "tackle"
    name = "Tackle"
    element = Element.NORMAL
    power = 40
    powerPoints = 35
    percentAccuracy(95)
    type = MoveType.PHYSICAL
    effects(ApplyDamage)
}

val Scratch = buildMove {
    id = "scratch"
    name = "Scratch"
    element = Element.NORMAL
    power = 40
    powerPoints = 35
    percentAccuracy(100)
    type = MoveType.PHYSICAL
    effects(ApplyDamage)
}

val Leer = buildMove {
    id = "leer"
    name = "Leer"
    element = Element.NORMAL
    power = 0
    powerPoints = 30
    percentAccuracy(100)
    type = MoveType.STATUS
    effects(
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
    effects(
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
    effects(ApplyDamage)
}

val SonicBoom = buildMove {
    id = "sonic-boom"
    name = "Sonic Boom"
    element = Element.NORMAL
    power = 20
    powerPoints = 20
    percentAccuracy(90)
    special()
    effects(ApplyDirectDamage)
}

val SandAttack = buildMove {
    id = "sand-attack"
    name = "Sand Attack"
    element = Element.GROUND
    power = 0
    powerPoints = 24
    percentAccuracy(100)
    status()
    effects(ApplyAccuracyChange(StatModifier.negativeStage(1)))
}

val ConfuseRay = buildMove {
    id = "confuse-ray"
    name = "Confuse Ray"
    element = Element.GHOST
    power = 0
    powerPoints = 16
    percentAccuracy(100)
    status()
    effects(
        ApplyVolatileStatusCondition(
            percentage = Percentage(100),
            volatileStatusFactory = { VolatileStatus.confusion(it) }
        )
    )
}
