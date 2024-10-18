package org.example.generator

import dk.brics.automaton.Automaton
import generator.AtomGenerator
import org.example.models.GeneratorMode
import org.example.models.Lexems
import org.example.models.MATAutomaton

class AutomatonGenerator {

    private lateinit var mode: GeneratorMode
    private lateinit var config: MATAutomaton.Config
    private val lexemMap = mutableMapOf<Lexems, Automaton>()

    fun create(mode: String): MATAutomaton {
        config = MATAutomaton.Config.factory(mode)
        val automaton = generateAutomaton(config)
        return MATAutomaton(
            automaton = automaton,
            config = config
        )
    }

    private fun generateAutomaton(config: MATAutomaton.Config): Automaton {
        generateAtomAutomaton(
            statesNum = config.lexemSizeMap[Lexems.ATOM]?.states ?: 2,
            transitionsNum = config.lexemSizeMap[Lexems.ATOM]?.transitions ?: 1,
            acceptingStatesNum = config.lexemSizeMap[Lexems.ATOM]?.acceptingStates ?: 1,
            alphabet = config.alphabet
        )
        return Automaton.makeEmpty()
    }

    private fun generateEOLAutomaton() = null
    private fun generateDOTAutomaton(

    ) = null

    private fun generateAtomAutomaton(
        statesNum: Int,
        transitionsNum: Int,
        acceptingStatesNum: Int,
        alphabet: Set<Int>,
    ) = AtomGenerator(
        statesNum = statesNum,
        transitions = transitionsNum,
        acceptingStatesNum = acceptingStatesNum,
        alphabet = alphabet
    ).generate()

    private fun generateLBR() = null
    private fun generateRBR() = null
}
