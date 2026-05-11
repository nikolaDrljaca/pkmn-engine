package com.drbrosdev


fun main() {
    val engine = BattleEngine()

    println("Pokemon Battle Engine")
    println("Type 'exit' to quit")
    println("═══════════════════════════════════════")

    while (true) {
        print("> ")
        val input = readlnOrNull() ?: break
        if (input.trim() == "exit") break
        if (input.isBlank()) continue

        try {
            val result = engine.execute(input)
            println(result)
        } catch (e: Throwable) {
            println("An unexpected error occurred: ${e.localizedMessage}")
            println(e.stackTrace.joinToString(separator = "\n") { it.toString() })
        }
    }

    println("Goodbye!")
}