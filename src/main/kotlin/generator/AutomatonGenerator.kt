package org.example.generator

import dk.brics.automaton.Automaton
import org.example.models.GeneratorMode
import org.example.models.Lexems
import org.example.models.MATAutomaton
import java.util.*
import kotlin.collections.HashSet
import kotlin.random.Random

class AutomatonGenerator {

    private lateinit var mode : GeneratorMode
    private lateinit var config : MATAutomaton.Config
    private val lexemMap = mutableMapOf<Lexems, Automaton>()
    fun create(mode: String) : MATAutomaton {
        config = MATAutomaton.Config.factory(mode)
        val automaton = Automaton.makeEmpty()
        return MATAutomaton(
            automaton = automaton,
            config = config
        )
    }

    private fun generateAutomaton(config : MATAutomaton.Config) : Automaton {
        val eolAlphabetRandomIterations =  when(mode) {
            GeneratorMode.EASY -> 2
            GeneratorMode.NORMAL -> 5
            GeneratorMode.HARD -> 8
        }
        val eolAlphabet = generateSequence { Random.nextInt(0, 10) }
            .take(eolAlphabetRandomIterations)
            .toSet()

        val alphabet = (0..9).toSet() - eolAlphabet
        Lexems.entries.forEach { lexem ->
            val lexemsAlphabet = when(lexem) {
                Lexems.EOL -> eolAlphabet
                else -> alphabet
            }
        }
        return Automaton.makeEmpty()
    }

    private fun generateEOLAutomaton() = null
    private fun generateDOTAutomaton() = null
    private fun generateAtomAutomaton() = null
    private fun generateLBR() = null
    private fun generateRBR() = null
}