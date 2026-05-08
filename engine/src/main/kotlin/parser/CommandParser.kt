package com.drbrosdev.parser


fun interface CommandParser {
    fun parse(command: String): Command
}