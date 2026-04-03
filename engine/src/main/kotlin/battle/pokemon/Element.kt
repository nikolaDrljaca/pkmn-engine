package com.drbrosdev.battle.pokemon

import com.drbrosdev.battle.pokemon.Element.*

enum class Element {
    NORMAL,
    FIRE,
    WATER,
    GRASS,
    ELECTRIC,
    ICE,
    FIGHTING,
    POISON,
    GROUND,
    FLYING,
    PSYCHIC,
    BUG,
    ROCK,
    GHOST,
    DRAGON,
    DARK,
    STEEL
}

data class Elements(
    val values: Set<Element> // each type is unique
) {
    init {
        require(values.isNotEmpty())
        require(values.size <= 2)
    }

    companion object {
        fun of(vararg elements: Element) = Elements(elements.toSet())
    }
}

fun Elements.hasAnyOf(vararg element: Element) = element.any { it in values }
fun Elements.hasAnyOf(element: List<Element>) = element.any { it in values }

data class ElementRelations(
    val superEffective: Set<Element> = emptySet(),
    val notVeryEffective: Set<Element> = emptySet(),
    val immune: Set<Element> = emptySet()
)

val Element.relations
    get() = when (this) {
        NORMAL -> ElementRelations(
            notVeryEffective = setOf(ROCK, STEEL),
            immune = setOf(GHOST)
        )

        FIRE -> ElementRelations(
            superEffective = setOf(GRASS, ICE, BUG, STEEL),
            notVeryEffective = setOf(FIRE, WATER, ROCK, DRAGON)
        )

        WATER -> ElementRelations(
            superEffective = setOf(FIRE, GROUND, ROCK),
            notVeryEffective = setOf(WATER, GRASS, DRAGON)
        )

        GRASS -> ElementRelations(
            superEffective = setOf(WATER, GROUND, ROCK),
            notVeryEffective = setOf(FIRE, GRASS, POISON, FLYING, BUG, DRAGON, STEEL)
        )

        ELECTRIC -> ElementRelations(
            superEffective = setOf(WATER, FLYING),
            notVeryEffective = setOf(GRASS, ELECTRIC, DRAGON),
            immune = setOf(GROUND)
        )

        ICE -> ElementRelations(
            superEffective = setOf(GRASS, GROUND, FLYING, DRAGON),
            notVeryEffective = setOf(WATER, ICE, STEEL)
        )

        FIGHTING -> ElementRelations(
            superEffective = setOf(NORMAL, ICE, ROCK, DARK, STEEL),
            notVeryEffective = setOf(POISON, FLYING, PSYCHIC, BUG),
            immune = setOf(GHOST)
        )

        POISON -> ElementRelations(
            superEffective = setOf(GRASS),
            notVeryEffective = setOf(POISON, GROUND, ROCK, GHOST),
            immune = setOf(STEEL)
        )

        GROUND -> ElementRelations(
            superEffective = setOf(FIRE, ELECTRIC, POISON, ROCK, STEEL),
            notVeryEffective = setOf(GRASS, BUG),
            immune = setOf(FLYING)
        )

        FLYING -> ElementRelations(
            superEffective = setOf(GRASS, FIGHTING, BUG),
            notVeryEffective = setOf(ELECTRIC, ROCK, STEEL)
        )

        PSYCHIC -> ElementRelations(
            superEffective = setOf(FIGHTING, POISON),
            notVeryEffective = setOf(PSYCHIC, STEEL),
            immune = setOf(DARK)
        )

        BUG -> ElementRelations(
            superEffective = setOf(GRASS, PSYCHIC, DARK),
            notVeryEffective = setOf(FIRE, FIGHTING, FLYING, GHOST, STEEL)
        )

        ROCK -> ElementRelations(
            superEffective = setOf(FIRE, ICE, FLYING, BUG),
            notVeryEffective = setOf(FIGHTING, GROUND, STEEL)
        )

        GHOST -> ElementRelations(
            superEffective = setOf(PSYCHIC, GHOST),
            notVeryEffective = setOf(DARK),
            immune = setOf(NORMAL)
        )

        DRAGON -> ElementRelations(
            superEffective = setOf(DRAGON),
            notVeryEffective = setOf(STEEL)
        )

        DARK -> ElementRelations(
            superEffective = setOf(PSYCHIC, GHOST),
            notVeryEffective = setOf(FIGHTING, DARK, STEEL)
        )

        STEEL -> ElementRelations(
            superEffective = setOf(ICE, ROCK),
            notVeryEffective = setOf(FIRE, WATER, ELECTRIC, STEEL)
        )
    }

enum class Effectiveness {
    IMMUNE,
    NOT_VERY,
    NEUTRAL,
    SUPER
}

val Effectiveness.multiplier
    get() = when (this) {
        Effectiveness.IMMUNE -> 0
        Effectiveness.NOT_VERY -> 50
        Effectiveness.NEUTRAL -> 100
        Effectiveness.SUPER -> 200
    }


fun effectiveness(attacker: Element, defender: Element): Effectiveness =
    when (defender) {
        in attacker.relations.superEffective -> Effectiveness.SUPER
        in attacker.relations.notVeryEffective -> Effectiveness.NOT_VERY
        in attacker.relations.immune -> Effectiveness.IMMUNE
        else -> Effectiveness.NEUTRAL
    }

fun Effectiveness.combine(other: Effectiveness): Effectiveness = when {
    this == Effectiveness.IMMUNE || other == Effectiveness.IMMUNE -> Effectiveness.IMMUNE
    this == Effectiveness.SUPER && other == Effectiveness.SUPER -> Effectiveness.SUPER
    this == Effectiveness.NOT_VERY && other == Effectiveness.NOT_VERY -> Effectiveness.NOT_VERY
    this == Effectiveness.SUPER && other == Effectiveness.NOT_VERY -> Effectiveness.NEUTRAL
    this == Effectiveness.NOT_VERY && other == Effectiveness.SUPER -> Effectiveness.NEUTRAL
    else -> other
}

fun effectiveness(attacker: Element, defenders: Elements): Effectiveness =
    defenders.values
        .map { effectiveness(attacker, it) }
        .reduce { acc, e -> acc.combine(e) }


