package com.drbrosdev.battle.move.registry

import com.drbrosdev.RandomGen
import com.drbrosdev.battle.environment.EnvironmentUnit
import com.drbrosdev.battle.environment.SpikesEnvUnit
import com.drbrosdev.battle.environment.StealthRockEnvUnit
import com.drbrosdev.battle.environment.Weather
import com.drbrosdev.battle.item.Item
import com.drbrosdev.battle.move.*
import com.drbrosdev.battle.move.damagecalc.ApplyDamage
import com.drbrosdev.battle.pokemon.Element
import com.drbrosdev.battle.pokemon.MajorStatus
import com.drbrosdev.battle.pokemon.StrongJaw
import com.drbrosdev.battle.pokemon.VolatileStatus
import com.drbrosdev.battle.pokemon.stats.StatKey
import com.drbrosdev.battle.pokemon.stats.StatModification
import com.drbrosdev.battle.pokemon.stats.StatModifier
import com.drbrosdev.battle.pokemon.stats.StatModifiers

object MoveIndex {

    val HiddenPowerIce = buildMove {
        id = "hidden-power-ice"
        name = "Hidden Power"
        element = Element.ICE
        power = 60
        powerPoints = 24
        percentAccuracy(100)
        special()
        effects(ApplyDamage)
    }

    val UTurn = buildMove {
        id = "u-turn"
        name = "U-Turn"
        element = Element.BUG
        power = 70
        powerPoints = 32
        percentAccuracy(100)
        physical()
        effects(
            ApplyDamage,
            // TODO: Missing impl
        )
    }

    val Earthquake = buildMove {
        id = "earthquake"
        name = "Earthquake"
        element = Element.GROUND
        power = 100
        powerPoints = 16
        percentAccuracy(100)
        physical()
        effects(ApplyDamage)
    }

    val KnockOff = buildMove {
        id = "knock-off"
        name = "Knock Off"
        element = Element.DARK
        power = 65
        powerPoints = 32
        percentAccuracy(100)
        physical()
        effects(
            ApplyDamage,
            { battle ->
                val target = battle[targetId]
                val targetItem = target.item
                battle
                    .updateMons(target.copy(item = Item.NoItem))
                    .narrative("${target.name} has had their ${targetItem.name} knocked off!")
            }
        )
    }

    val PowerWhip = buildMove {
        id = "power-whip"
        name = "Power Whip"
        element = Element.GRASS
        power = 120
        powerPoints = 16
        percentAccuracy(85)
        physical()
        effects(ApplyDamage)
    }

    val GyroBall = buildMove {
        id = "gyro-ball"
        name = "Gyro Ball"
        element = Element.STEEL
        power = 80
        powerPoints = 32
        percentAccuracy(100)
        physical()
        effects(ApplyDamage)
    }

    val Crunch = buildMove {
        id = "crunch"
        name = "Crunch"
        element = Element.DARK
        power = 80
        powerPoints = 24
        percentAccuracy(100)
        physical()
        effects(
            ApplyDamage,
            ApplyStatModification { context ->
                val chance = when {
                    context.pokemon.ability == StrongJaw -> 50
                    else -> 20
                }
                if (RandomGen.nextInt(1, 101) > chance)
                    StatModifiers()
                else
                    StatModifiers(mapOf(StatKey.DEFENCE to StatModifier.negativeStage(1)))
            }
        )
    }

    val ThunderWave = buildMove {
        id = "thunder-wave"
        name = "Thunder Wave"
        element = Element.ELECTRIC
        power = 0
        powerPoints = 32
        percentAccuracy(100)
        status()
        effects(ApplyStatusCondition(Percentage(100), MajorStatus.Paralyzed))
    }

    val Psychic = buildMove {
        id = "psychic"
        name = "Psychic"
        element = Element.PSYCHIC
        power = 90
        powerPoints = 16
        percentAccuracy(100)
        special()
        effects(
            ApplyDamage,
            ApplyChanceStatModification(Percentage(10)) { _ ->
                StatModifiers(mapOf(StatKey.SPECIAL_DEFENCE to StatModifier.negativeStage(1)))
            }
        )
    }

    val GrassKnot = buildMove {
        id = "grass-knot"
        name = "Grass Knot"
        element = Element.GRASS
        power = 80
        powerPoints = 32
        percentAccuracy(100)
        special()
        effects(ApplyDamage)
    }

    val ShadowBall = buildMove {
        id = "shadow-ball"
        name = "Shadow Ball"
        element = Element.GHOST
        power = 80
        powerPoints = 24
        percentAccuracy(100)
        special()
        effects(
            ApplyDamage,
            ApplyChanceStatModification(Percentage(20)) {
                StatModifiers(mapOf(StatKey.SPECIAL_DEFENCE to StatModifier.negativeStage(1)))
            }
        )
    }

    val DracoMeteor = buildMove {
        id = "draco-meteor"
        name = "Draco Meteor"
        element = Element.DRAGON
        power = 130
        powerPoints = 8
        percentAccuracy(90)
        special()
        effects(
            ApplyDamage,
            ApplySelfStatModification {
                StatModifiers(mapOf(StatKey.SPECIAL_ATTACK to StatModifier.negativeStage(2)))
            }
        )
    }

    val Surf = buildMove {
        id = "surf"
        name = "Surf"
        element = Element.WATER
        power = 90
        powerPoints = 24
        percentAccuracy(100)
        special()
        effects(ApplyDamage)
    }

    val DragonPulse = buildMove {
        id = "dragon-pulse"
        name = "Dragon Pulses"
        element = Element.DRAGON
        power = 90
        powerPoints = 24
        percentAccuracy(100)
        special()
        effects(ApplyDamage)
    }

    val Spikes = buildMove {
        id = "spikes"
        name = "Spikes"
        element = Element.GROUND
        power = 0
        powerPoints = 32
        percentAccuracy(100)
        status()
        effects(ApplyEnvironmentUnit(SpikesEnvUnit))
    }

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
            ApplyStatusCondition(Percentage(10), MajorStatus.Burned)
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

    val Sandstorm = buildMove {
        id = "sandstorm"
        name = "Sandstorm"
        element = Element.GROUND
        power = 0
        powerPoints = 16
        percentAccuracy(100)
        status()
        effects(ApplyWeather { turnCount, shouldExtend ->
            Weather.sandstorm(turnCount, shouldExtend)
        })
    }

}
