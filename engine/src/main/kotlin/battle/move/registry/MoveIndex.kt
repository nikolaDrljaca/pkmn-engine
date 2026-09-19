package com.drbrosdev.battle.move.registry

import com.drbrosdev.RandomGen
import com.drbrosdev.battle.environment.TemporaryEffect
import com.drbrosdev.battle.environment.Weather
import com.drbrosdev.battle.environment.hazard.Layers
import com.drbrosdev.battle.environment.hazard.SpikesHazard
import com.drbrosdev.battle.environment.hazard.StealthRockHazard
import com.drbrosdev.battle.environment.hazard.ToxicSpikesHazard
import com.drbrosdev.battle.item.Item
import com.drbrosdev.battle.move.*
import com.drbrosdev.battle.move.damagecalc.ApplyDamage
import com.drbrosdev.battle.pokemon.Element
import com.drbrosdev.battle.pokemon.MajorStatus
import com.drbrosdev.battle.pokemon.StrongJaw
import com.drbrosdev.battle.pokemon.VolatileStatus
import com.drbrosdev.battle.pokemon.stats.StatKey
import com.drbrosdev.battle.pokemon.stats.StatModifier
import com.drbrosdev.battle.pokemon.stats.StatModifiers

object MoveIndex {

    private val HiddenPowerIce = buildMove {
        id = "hidden-power-ice"
        name = "Hidden Power"
        element = Element.ICE
        power = 60
        powerPoints = 24
        percentAccuracy(100)
        special()
        effects(ApplyDamage)
    }

    private val UTurn = buildMove {
        id = "u-turn"
        name = "U-Turn"
        element = Element.BUG
        power = 70
        powerPoints = 32
        contact = true
        percentAccuracy(100)
        physical()
        effects(
            ApplyDamage,
            // TODO: Missing impl
        )
    }

    private val Earthquake = buildMove {
        id = "earthquake"
        name = "Earthquake"
        element = Element.GROUND
        power = 100
        powerPoints = 16
        percentAccuracy(100)
        physical()
        effects(ApplyDamage)
    }

    private val KnockOff = buildMove {
        id = "knock-off"
        name = "Knock Off"
        element = Element.DARK
        power = 65
        powerPoints = 32
        contact = true
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

    private val PowerWhip = buildMove {
        id = "power-whip"
        name = "Power Whip"
        element = Element.GRASS
        power = 120
        powerPoints = 16
        contact = true
        percentAccuracy(85)
        physical()
        effects(ApplyDamage)
    }

    private val GyroBall = buildMove {
        id = "gyro-ball"
        name = "Gyro Ball"
        element = Element.STEEL
        power = 80
        powerPoints = 32
        contact = true
        percentAccuracy(100)
        physical()
        effects(ApplyDamage)
    }

    private val Crunch = buildMove {
        id = "crunch"
        name = "Crunch"
        element = Element.DARK
        power = 80
        powerPoints = 24
        contact = true
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

    private val ThunderWave = buildMove {
        id = "thunder-wave"
        name = "Thunder Wave"
        element = Element.ELECTRIC
        power = 0
        powerPoints = 32
        percentAccuracy(100)
        status()
        effects(ApplyStatusCondition(Percentage(100), MajorStatus.Paralyzed))
    }

    private val Psychic = buildMove {
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

    private val GrassKnot = buildMove {
        id = "grass-knot"
        name = "Grass Knot"
        element = Element.GRASS
        power = 80
        powerPoints = 32
        contact = true
        percentAccuracy(100)
        special()
        effects(ApplyDamage)
    }

    private val ShadowBall = buildMove {
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

    private val DracoMeteor = buildMove {
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

    private val Surf = buildMove {
        id = "surf"
        name = "Surf"
        element = Element.WATER
        power = 90
        powerPoints = 24
        percentAccuracy(100)
        special()
        effects(ApplyDamage)
    }

    private val DragonPulse = buildMove {
        id = "dragon-pulse"
        name = "Dragon Pulse"
        element = Element.DRAGON
        power = 90
        powerPoints = 24
        percentAccuracy(100)
        special()
        effects(ApplyDamage)
    }

    private val Spikes = buildMove {
        id = "spikes"
        name = "Spikes"
        element = Element.GROUND
        power = 0
        powerPoints = 32
        percentAccuracy(100)
        status()
        effects({ battle ->
            val targetTeam = battle.team(targetId)
            val existingHazard = battle.entryHazards(targetId)
                .filterIsInstance<SpikesHazard>()
                .firstOrNull()
            val updatedHazard = when {
                existingHazard == null -> SpikesHazard(Layers.One)
                else -> existingHazard.copy(layers = existingHazard.layers.stack(2))
            }
            when {
                existingHazard == null -> battle
                else -> battle.copy(
                    environment = battle.environment
                        .withEntryHazard(
                            targetTeam.id,
                            updatedHazard
                        )
                )
            }
        })
    }

    private val ToxicSpikes = buildMove {
        id = "toxic-spikes"
        name = "Toxic Spikes"
        element = Element.POISON
        power = 0
        powerPoints = 32
        percentAccuracy(100)
        status()
        effects({ battle ->
            val targetTeam = battle.team(targetId)
            val existingHazard = battle.entryHazards(targetId)
                .filterIsInstance<ToxicSpikesHazard>()
                .firstOrNull()
            val updatedHazard = when {
                existingHazard == null -> SpikesHazard(Layers.One)
                else -> existingHazard.copy(layers = existingHazard.layers.stack(2))
            }
            when {
                existingHazard == null -> battle
                else -> battle.copy(
                    environment = battle.environment
                        .withEntryHazard(
                            targetTeam.id,
                            updatedHazard
                        )
                )
            }
        })
    }

    private val Flamethrower = buildMove {
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

    val Scratch = buildMove {
        id = "scratch"
        name = "Scratch"
        element = Element.NORMAL
        power = 40
        powerPoints = 35
        contact = true
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

    private val Pursuit = buildMove {
        id = "pursuit"
        name = "Pursuit"
        element = Element.DARK
        power = 40
        powerPoints = 20
        percentAccuracy(100)
        type = MoveType.PHYSICAL
        effects(ApplyDamage)
    }

    private val SonicBoom = buildMove {
        id = "sonic-boom"
        name = "Sonic Boom"
        element = Element.NORMAL
        power = 0
        powerPoints = 20
        percentAccuracy(90)
        special()
        effects(ApplyDirectDamage(20))
    }

    private val SandAttack = buildMove {
        id = "sand-attack"
        name = "Sand Attack"
        element = Element.GROUND
        power = 0
        powerPoints = 24
        percentAccuracy(100)
        status()
        effects(ApplyAccuracyChange(StatModifier.negativeStage(1)))
    }

    private val ConfuseRay = buildMove {
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

    private val StealthRock = buildMove {
        id = "stealth-rock"
        name = "Stealth Rock"
        element = Element.ROCK
        power = 0
        powerPoints = 32
        percentAccuracy(100)
        status()
        effects(ApplyEntryHazard(StealthRockHazard))
    }

    private val Reflect = buildMove {
        id = "reflect"
        name = "Reflect"
        element = Element.PSYCHIC
        power = 0
        powerPoints = 32
        percentAccuracy(100)
        status()
        effects(
            ApplyTemporaryEffect(
                factory = { currentTurn, user ->
                    val upperBound = when {
                        // TODO: @drljacan magic string
                        user.item.id.value == "light-clay" -> 9
                        else -> 5
                    }
                    val roll = RandomGen.nextInt(5, upperBound)
                    TemporaryEffect.Reflect(expiresOnTurn = currentTurn + roll)
                }
            )
        )
    }

    private val LightScreen = buildMove {
        id = "light-screen"
        name = "Light Screen"
        element = Element.PSYCHIC
        power = 0
        powerPoints = 32
        percentAccuracy(100)
        status()
        effects(ApplyTemporaryEffect(factory = { currentTurn, user ->
            val upperBound = when {
                // TODO: @drljacan magic string
                user.item.id.value == "light-clay" -> 9
                else -> 5
            }
            val roll = RandomGen.nextInt(5, upperBound)
            TemporaryEffect.LightScreen(expiresOnTurn = currentTurn + roll)
        }))
    }

    private val Tailwind = buildMove {
        id = "tailwind"
        name = "Tailwind"
        element = Element.FLYING
        power = 0
        powerPoints = 24
        percentAccuracy(100)
        status()
        effects(ApplyTemporaryEffect(factory = { currentTurn, _ ->
            TemporaryEffect.Tailwind.create(currentTurn)
        }))
    }

    private val Sandstorm = buildMove {
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

    val lookup = mapOf(
        HiddenPowerIce.name to HiddenPowerIce,
        UTurn.name to UTurn,
        Earthquake.name to Earthquake,
        KnockOff.name to KnockOff,
        PowerWhip.name to PowerWhip,
        GyroBall.name to GyroBall,
        Crunch.name to Crunch,
        ThunderWave.name to ThunderWave,
        Psychic.name to Psychic,
        GrassKnot.name to GrassKnot,
        ShadowBall.name to ShadowBall,
        DracoMeteor.name to DracoMeteor,
        Surf.name to Surf,
        DragonPulse.name to DragonPulse,
        Spikes.name to Spikes,
        Flamethrower.name to Flamethrower,
        Scratch.name to Scratch,
        Leer.name to Leer,
        Growl.name to Growl,
        Pursuit.name to Pursuit,
        SonicBoom.name to SonicBoom,
        SandAttack.name to SandAttack,
        ConfuseRay.name to ConfuseRay,
        StealthRock.name to StealthRock,
        Reflect.name to Reflect,
        LightScreen.name to LightScreen,
        Sandstorm.name to Sandstorm,
        Tailwind.name to Tailwind
    )
}
