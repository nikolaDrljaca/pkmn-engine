package com.drbrosdev

/*
data class Battle(
    val attacker: Pokemon,
    val defender: Pokemon
) {
    // random num generator can live here

    fun updateAttacker(pokemon: Pokemon) : Battle {
        return this.copy(attacker = pokemon)
    }

    fun updateDefender(pokemon: Pokemon) : Battle {
        return this.copy(defender = pokemon)
    }
}

// Move and Move Resolution/Execution System

interface Move {
    fun execute(battle: Battle): Battle
}

/*
power
powerPoints (PP)
accuracy
element
type: Status | Physical | Special

id
name
description
 */

interface Attempt {
    fun execute(battle: Battle): Battle
}

// region Effect Subsystem
fun interface Effect {
    fun apply(battle: Battle): Battle
}

val NoEffect: Effect = Effect { it }

// decorators
class SequenceEffect(private val effects: List<Effect>) : Effect {
    override fun apply(battle: Battle): Battle {
        return effects.fold(battle) { currentBattle, effect ->
            effect.apply(currentBattle)
        }
    }
}

class ConditionalEffect(
    private val onSuccess: Effect,
    private val onFail: Effect,
    private val condition: Condition<Battle>
) : Effect {
    override fun apply(battle: Battle): Battle = when {
        condition.check(battle) -> onSuccess.apply(battle)
        else -> onFail.apply(battle)
    }
}

// example Effects
class Faint(private val target: Target): Effect {
    override fun apply(battle: Battle): Battle {
        val actualTarget = target.resolve(battle).let { it: Pokemon ->
            // TODO
            it
        }
        return when (target) {
            Target.Attacker -> battle.updateAttacker(actualTarget)
            Target.Defender -> battle.updateDefender(actualTarget)
        }
    }
}
class Paralysis(private val target: Target): Effect {
    override fun apply(battle: Battle): Battle {
        TODO("Not yet implemented")
    }
}
// Usual damage calc based, types stats abilities etc
class FormulaDamage(
    private val number: Power
): Effect {
    override fun apply(battle: Battle): Battle {
        // attacker applies formula damage to defender
        TODO("Not yet implemented")
    }
}
class DirectDamage(
    private val target: Target,
    private val number: Power
): Effect {
    override fun apply(battle: Battle): Battle {
        // inflict number damage to target
        TODO()
    }
}
/*
Other examples:
OHKO
Paralyze
Drain
StatChange
RestoreHp
etc..
 */


// endregion

// Move Power
@JvmInline
value class Power(val value: Int) {
    init {
        require(value > 0)
    }
}

class BasicAttempt(
    private val accuracy: Condition<Battle>,
    // in case move is accurate
    private val onHit: Effect = NoEffect,
    // in case its not accurate
    private val onMiss: Effect = NoEffect,
    // runs always
    private val after: Effect = NoEffect
) : Attempt {
    override fun execute(battle: Battle): Battle {
        val afterConditionalEffect = when {
            accuracy.check(battle) -> onHit.apply(battle)
            else -> onMiss.apply(battle)
        }
        return after.apply(afterConditionalEffect)
    }
}

sealed interface Target {
    fun resolve(battle: Battle): Pokemon

    object Attacker : Target {
        override fun resolve(battle: Battle): Pokemon = battle.attacker
    }

    object Defender : Target {
        override fun resolve(battle: Battle): Pokemon = battle.defender
    }
}

interface Condition<T> {
    fun check(value: T): Boolean
}

// Example Pokemon conditions
class HasElement(val element: Element) : Condition<Pokemon> {
    override fun check(value: Pokemon): Boolean {
        return element == value.elements.primary
                || element == value.elements.secondary
    }
}

object IsParalyzed : Condition<Pokemon> {
    override fun check(value: Pokemon): Boolean {
        return value.majorStatus == MajorStatus.PARALYZED
    }
}

class Probability(val accuracy: Int = 100): Condition<Battle> {
    override fun check(value: Battle): Boolean {
        // TODO accuracy stat based calculation
        if (accuracy == 100) return true
        return Random.nextInt(100) < accuracy
    }
}

class For(
    private val target: Target,
    private val condition: Condition<Pokemon>
) : Condition<Battle> {
    override fun check(value: Battle): Boolean {
        val actualTarget = target.resolve(battle = value)
        return condition.check(actualTarget)
    }
}


// Eg But it failed!
class WithPrecondition(
    private val condition: Condition<Battle>,
    private val move: Move
) : Move by move {
    override fun execute(battle: Battle): Battle {
        return when {
            condition.check(battle) -> move.execute(battle)
            else -> battle
        }
    }
}

// It does not affect the target
class WithApplicability(
    private val condition: Condition<Pokemon>,
    private val move: Move
) : Move by move {
    override fun execute(battle: Battle): Battle {
        return when {
            condition.check(battle.defender) -> move.execute(battle)
            else -> battle
        }
    }
}

class BasicMove(
    val name: String,
    val element: Element,
    val attempt: Attempt
): Move {
    override fun execute(battle: Battle): Battle {
        return attempt.execute(battle)
    }
}

val tackle  = BasicMove(
    name = "Tackle",
    element = Element.NORMAL,
    attempt = BasicAttempt(
        accuracy = Probability(100),
        onHit = FormulaDamage(Power(45)),
    )
)
val bodySlam  = BasicMove(
    name = "Body Slam",
    element = Element.NORMAL,
    attempt = BasicAttempt(
        accuracy = Probability(100),
        onHit = SequenceEffect(
            effects = listOf(
                FormulaDamage(Power(85)),
                ConditionalEffect(
                    condition = Probability(30),
                    onSuccess = Paralysis(Target.Defender),
                    onFail = NoEffect
                )
            )
        ),
    )
)
*/