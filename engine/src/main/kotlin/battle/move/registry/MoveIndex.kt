package com.drbrosdev.battle.move.registry

import com.drbrosdev.RandomGen
import com.drbrosdev.battle.environment.EnvironmentUnit
import com.drbrosdev.battle.environment.StealthRockEnvUnit
import com.drbrosdev.battle.move.ApplyAccuracyChange
import com.drbrosdev.battle.move.ApplyDirectDamage
import com.drbrosdev.battle.move.ApplyEnvironmentUnit
import com.drbrosdev.battle.move.ApplyStatModification
import com.drbrosdev.battle.move.ApplyStatusCondition
import com.drbrosdev.battle.move.ApplyVolatileStatusCondition
import com.drbrosdev.battle.move.MoveType
import com.drbrosdev.battle.move.Percentage
import com.drbrosdev.battle.move.buildMove
import com.drbrosdev.battle.move.damagecalc.ApplyDamage
import com.drbrosdev.battle.pokemon.Element
import com.drbrosdev.battle.pokemon.MajorStatus
import com.drbrosdev.battle.pokemon.VolatileStatus
import com.drbrosdev.battle.pokemon.stats.StatKey
import com.drbrosdev.battle.pokemon.stats.StatModifier
import com.drbrosdev.battle.pokemon.stats.StatModifiers

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
    power = 0
    powerPoints = 20
    percentAccuracy(90)
    special()
    effects(ApplyDirectDamage(20))
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

val StealthRock = buildMove {
    id = "stealth-rock"
    name = "Stealth Rock"
    element = Element.ROCK
    power = 0
    powerPoints = 32
    percentAccuracy(100)
    status()
    effects(ApplyEnvironmentUnit(StealthRockEnvUnit))
}

val Reflect = buildMove {
    id = "reflect"
    name = "Reflect"
    element = Element.PSYCHIC
    power = 0
    powerPoints = 32
    percentAccuracy(100)
    status()
    effects({ battle ->
        val user = battle[userId]
        val lowerBound = 5
        val upperBound = when {
            user.item.id.value == "light-clay" -> 9
            else -> 6
        }
        val roll = RandomGen.nextInt(lowerBound, upperBound)
        val unit = EnvironmentUnit(
            id = "reflect",
            expiresOnTurn = battle.turnCount + roll
        )
        battle.updateEnvironment(userId, unit)
    })
}

val LightScreen = buildMove {
    id = "light-screen"
    name = "Light Screen"
    element = Element.PSYCHIC
    power = 0
    powerPoints = 32
    percentAccuracy(100)
    status()
    effects({ battle ->
        val user = battle[userId]
        val lowerBound = 5
        val upperBound = when {
            user.item.id.value == "light-clay" -> 9
            else -> 6
        }
        val roll = RandomGen.nextInt(lowerBound, upperBound)
        val unit = EnvironmentUnit(
            id = "light-screen",
            expiresOnTurn = battle.turnCount + roll
        )
        battle.updateEnvironment(userId, unit)
    })
}