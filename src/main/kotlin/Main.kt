package org.example

import dk.brics.automaton.Automaton
import dk.brics.automaton.RegExp
import kotlin.random.Random

fun main() {

    val randomSet = generateSequence { Random.nextInt(0, 10) }
        .take(10)
        .toSet()

    println(randomSet)
    val regex1 = "a*b"
    val regex2 = "ab*"

    val automaton1 = RegExp(regex1).toAutomaton()
    val automaton2 = RegExp(regex2).toAutomaton()

    val union = automaton1.union(automaton2)
    union.determinize()
    val final = Automaton.minimize(union)
    println(final.toDot())


}