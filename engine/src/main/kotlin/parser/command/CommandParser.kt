package com.drbrosdev.parser.command


fun interface CommandParser {
    fun parse(command: String): Command
}