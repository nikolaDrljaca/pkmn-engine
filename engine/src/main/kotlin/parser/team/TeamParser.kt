package com.drbrosdev.parser.team

import com.drbrosdev.battle.Team

fun interface TeamParser {
    fun parse(input: String): Team
}