package com.drbrosdev


fun main() {
    val engine = BattleEngine()
    val reader = System.`in`.bufferedReader()

    println("Pokemon Battle Engine")
    println("Type 'exit' to quit")
    println("═══════════════════════════════════════")

    while (true) {
        print("> ")
        val input = reader.readLine() ?: break
        if (input.trim() == "exit") break
        if (input.isBlank()) continue

        val result = engine.execute(input)
        println(result)
    }

    println("Goodbye!")
}