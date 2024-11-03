package generator.random

import dk.brics.automaton.Automaton
import generator.buildProgramAutomaton
import models.Lexems
import org.example.models.MATAutomaton

class RandomAutomatonGenerator {

    private lateinit var config: MATAutomaton.Config.RandomConfig
    private val lexemeAutomata = mutableMapOf<Lexems, Automaton>()

    fun getLexemeAutomata(): Map<Lexems, Automaton> = lexemeAutomata

    fun create(mode: String): MATAutomaton {
        config = MATAutomaton.Config.RandomConfig.factory(mode)
        val automaton = generateAutomaton()
        return MATAutomaton(
            automaton = automaton,
            config = config
        )
    }

    private fun generateAutomaton(): Automaton {
        lexemeAutomata[Lexems.ATOM] = generateAtomAutomaton(
            statesNum = config.lexemeMap[Lexems.ATOM]?.states ?: 2,
            transitionsNum = config.lexemeMap[Lexems.ATOM]?.transitionsNum ?: 1,
            acceptingStatesNum = config.lexemeMap[Lexems.ATOM]?.acceptingStates ?: 1,
            alphabet = config.alphabet
        )
        lexemeAutomata[Lexems.EOL] = generateWithConditions(
            statesNum = config.lexemeMap[Lexems.EOL]?.states ?: 2,
            transitionsNum = config.lexemeMap[Lexems.EOL]?.transitionsNum ?: 1,
            acceptingStatesNum = config.lexemeMap[Lexems.EOL]?.acceptingStates ?: 1,
            alphabet = config.eolAlphabet,
            nonIntersectAutomata = emptyList(),
            lexem = Lexems.EOL
        )
        lexemeAutomata[Lexems.DOT] = generateWithConditions(
            statesNum = config.lexemeMap[Lexems.DOT]?.states ?: 2,
            transitionsNum = config.lexemeMap[Lexems.DOT]?.transitionsNum ?: 1,
            acceptingStatesNum = config.lexemeMap[Lexems.DOT]?.acceptingStates ?: 1,
            alphabet = config.alphabet,
            nonIntersectAutomata = listOf(lexemeAutomata[Lexems.ATOM]!!),
            lexem = Lexems.DOT
        )
        while (true) {
            val lbr = generateWithConditions(
                statesNum = config.lexemeMap[Lexems.LBR]?.states ?: 2,
                transitionsNum = config.lexemeMap[Lexems.LBR]?.transitionsNum ?: 1,
                acceptingStatesNum = config.lexemeMap[Lexems.LBR]?.acceptingStates ?: 1,
                alphabet = config.alphabet,
                nonIntersectAutomata = listOf(lexemeAutomata[Lexems.ATOM]!!),
                lexem = Lexems.LBR
            )
            val rbr = generateWithConditions(
                statesNum = config.lexemeMap[Lexems.RBR]?.states ?: 2,
                transitionsNum = config.lexemeMap[Lexems.RBR]?.transitionsNum ?: 1,
                acceptingStatesNum = config.lexemeMap[Lexems.RBR]?.acceptingStates ?: 1,
                alphabet = config.alphabet,
                nonIntersectAutomata = listOf(lexemeAutomata[Lexems.ATOM]!!, lbr),
                lexem = Lexems.RBR
            )
            val concat = lbr.concatenate(rbr)
            if (concat.intersection(lbr).isEmpty && concat.intersection(rbr).isEmpty) {
                lexemeAutomata[Lexems.LBR] = lbr
                lexemeAutomata[Lexems.RBR] = rbr
                break
            }
        }
        val result = buildProgramAutomaton(
            lexemeAutomataMap = lexemeAutomata,
            maxParenthesesValue = config.maxParentheses
        )
        result.reduce()
        println("RESULT")

        println("EOL: ${lexemeAutomata[Lexems.EOL]!!.numberOfStates}, ${lexemeAutomata[Lexems.EOL]!!.numberOfTransitions}, ${lexemeAutomata[Lexems.EOL]!!.acceptStates.size}")
        println("LBR: ${lexemeAutomata[Lexems.LBR]!!.numberOfStates}, ${lexemeAutomata[Lexems.LBR]!!.numberOfTransitions}, ${lexemeAutomata[Lexems.LBR]!!.acceptStates.size}")
        println("RBR: ${lexemeAutomata[Lexems.RBR]!!.numberOfStates}, ${lexemeAutomata[Lexems.RBR]!!.numberOfTransitions}, ${lexemeAutomata[Lexems.RBR]!!.acceptStates.size}")
        println("ATOM: ${lexemeAutomata[Lexems.ATOM]!!.numberOfStates}, ${lexemeAutomata[Lexems.ATOM]!!.numberOfTransitions}, ${lexemeAutomata[Lexems.ATOM]!!.acceptStates.size}")
        println("DOT: ${lexemeAutomata[Lexems.DOT]!!.numberOfStates}, ${lexemeAutomata[Lexems.DOT]!!.numberOfTransitions}, ${lexemeAutomata[Lexems.DOT]!!.acceptStates.size}")

        println(
            "Кол-во состояний: ${result.numberOfStates}, кол-во переходов: ${result.numberOfTransitions}, " +
                "кол-во принимающих состояний: ${result.acceptStates.size}, " +
                "макс вложенность скобок: ${config.maxParentheses}"
        )
        return result
    }

    private fun generateWithConditions(
        statesNum: Int,
        transitionsNum: Int,
        acceptingStatesNum: Int,
        alphabet: Set<Int>,
        nonIntersectAutomata: List<Automaton>,
        lexem: Lexems
    ) = RandomGeneratorWithConditions(
        statesNum = statesNum,
        transitions = transitionsNum,
        acceptingStatesNum = acceptingStatesNum,
        alphabet = alphabet,
        lexem = lexem
    ).generateWithConditions(
        nonIntersectAutomata
    )

    private fun generateAtomAutomaton(
        statesNum: Int,
        transitionsNum: Int,
        acceptingStatesNum: Int,
        alphabet: Set<Int>,
    ) = RandomAtomGenerator(
        statesNum = statesNum,
        transitions = transitionsNum,
        acceptingStatesNum = acceptingStatesNum,
        alphabet = alphabet
    ).generate()

    companion object {
        fun randomLabel(alphabet: Set<Int>, usedLabels: Set<Int>): Int? {
            val available = alphabet - usedLabels
            if (available.isEmpty()) {
                return null
            }
            return available.random()
        }
    }
}
