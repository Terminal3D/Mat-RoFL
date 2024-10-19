package org.example.generator

import dk.brics.automaton.Automaton
import generator.AtomGenerator
import generator.GeneratorWithConditions
import net.automatalib.alphabet.ArrayAlphabet
import net.automatalib.brics.BricsNFA
import net.automatalib.util.automaton.fsa.NFAs
import net.automatalib.util.automaton.minimizer.hopcroft.HopcroftMinimization
import org.example.models.Lexems
import org.example.models.MATAutomaton

class AutomatonGenerator {

    private lateinit var config: MATAutomaton.Config
    private val lexemeAutomata = mutableMapOf<Lexems, Automaton>()


    fun getLexemeAutomata() : Map<Lexems, Automaton> = lexemeAutomata

    fun create(mode: String): MATAutomaton {
        config = MATAutomaton.Config.factory(mode)
        val automaton = generateAutomaton(config)
        return MATAutomaton(
            automaton = automaton,
            config = config
        )
    }

    private fun generateAutomaton(config: MATAutomaton.Config): Automaton {
        lexemeAutomata[Lexems.ATOM] = generateAtomAutomaton(
            statesNum = config.lexemSizeMap[Lexems.ATOM]?.states ?: 2,
            transitionsNum = config.lexemSizeMap[Lexems.ATOM]?.transitions ?: 1,
            acceptingStatesNum = config.lexemSizeMap[Lexems.ATOM]?.acceptingStates ?: 1,
            alphabet = config.alphabet
        )
        lexemeAutomata[Lexems.EOL] = generateWithConditions(
            statesNum = config.lexemSizeMap[Lexems.EOL]?.states ?: 2,
            transitionsNum = config.lexemSizeMap[Lexems.EOL]?.transitions ?: 1,
            acceptingStatesNum = config.lexemSizeMap[Lexems.EOL]?.acceptingStates ?: 1,
            alphabet = config.eolAlphabet,
            nonIntersectAutomata = emptyList(),
            lexem = Lexems.EOL
        )
        lexemeAutomata[Lexems.DOT] = generateWithConditions(
            statesNum = config.lexemSizeMap[Lexems.DOT]?.states ?: 2,
            transitionsNum = config.lexemSizeMap[Lexems.DOT]?.transitions ?: 1,
            acceptingStatesNum = config.lexemSizeMap[Lexems.DOT]?.acceptingStates ?: 1,
            alphabet = config.alphabet,
            nonIntersectAutomata = listOf(lexemeAutomata[Lexems.ATOM]!!),
            lexem = Lexems.DOT
        )
        while (true) {
            val lbr = generateWithConditions(
                statesNum = config.lexemSizeMap[Lexems.LBR]?.states ?: 2,
                transitionsNum = config.lexemSizeMap[Lexems.LBR]?.transitions ?: 1,
                acceptingStatesNum = config.lexemSizeMap[Lexems.LBR]?.acceptingStates ?: 1,
                alphabet = config.alphabet,
                nonIntersectAutomata = listOf(lexemeAutomata[Lexems.ATOM]!!),
                lexem = Lexems.LBR
            )
            val rbr = generateWithConditions(
                statesNum = config.lexemSizeMap[Lexems.RBR]?.states ?: 2,
                transitionsNum = config.lexemSizeMap[Lexems.RBR]?.transitions ?: 1,
                acceptingStatesNum = config.lexemSizeMap[Lexems.RBR]?.acceptingStates ?: 1,
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
//        println("EOL")
//        println(lexemeAutomata[Lexems.EOL]!!.toDot())
//        println("LBR")
//        println(lexemeAutomata[Lexems.LBR]!!.toDot())
//        println("RBR")
//        println(lexemeAutomata[Lexems.RBR]!!.toDot())
//        println("ATOM")
//        println(lexemeAutomata[Lexems.ATOM]!!.toDot())
        val result = buildProgramAutomaton()
        result.reduce()

        result.determinize()
        result.minimize()
        println("RESULT")
//        println(result.toDot())

        println("EOL: ${lexemeAutomata[Lexems.EOL]!!.numberOfStates}, ${lexemeAutomata[Lexems.EOL]!!.numberOfTransitions}, ${lexemeAutomata[Lexems.EOL]!!.acceptStates.size}")
        println("LBR: ${lexemeAutomata[Lexems.LBR]!!.numberOfStates}, ${lexemeAutomata[Lexems.LBR]!!.numberOfTransitions}, ${lexemeAutomata[Lexems.LBR]!!.acceptStates.size}")
        println("RBR: ${lexemeAutomata[Lexems.RBR]!!.numberOfStates}, ${lexemeAutomata[Lexems.RBR]!!.numberOfTransitions}, ${lexemeAutomata[Lexems.RBR]!!.acceptStates.size}")
        println("ATOM: ${lexemeAutomata[Lexems.ATOM]!!.numberOfStates}, ${lexemeAutomata[Lexems.ATOM]!!.numberOfTransitions}, ${lexemeAutomata[Lexems.ATOM]!!.acceptStates.size}")
        println("DOT: ${lexemeAutomata[Lexems.DOT]!!.numberOfStates}, ${lexemeAutomata[Lexems.DOT]!!.numberOfTransitions}, ${lexemeAutomata[Lexems.DOT]!!.acceptStates.size}")


        println("${result.numberOfStates}, ${result.numberOfTransitions}, ${result.acceptStates.size}, ${config.maxParentheses}")
        return result
    }

    private fun generateWithConditions(
        statesNum: Int,
        transitionsNum: Int,
        acceptingStatesNum: Int,
        alphabet: Set<Int>,
        nonIntersectAutomata: List<Automaton>,
        lexem: Lexems
    ) = GeneratorWithConditions(
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
    ) = AtomGenerator(
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

    // [program] ::= [eol]*([expression][eol]*)+
    fun buildProgramAutomaton(): Automaton {
        val eolStar = lexemeAutomata[Lexems.EOL]!!.repeat()
        val expressionAutomaton = buildExpressionEolAutomaton()
        val expressionEolStar = expressionAutomaton.concatenate(lexemeAutomata[Lexems.EOL]!!.repeat())
        val expressionEolPlus = expressionEolStar.repeat(1)
        val programAutomaton = eolStar.concatenate(expressionEolPlus)
        return programAutomaton
    }

    // [expression'] ::= [expression][eol] | [eol][expression]
    fun buildExpressionEolAutomaton(): Automaton {
        val expr = buildExpressionAutomaton(0)
        val eolStar = lexemeAutomata[Lexems.EOL]!!.repeat()
        return eolStar.concatenate(expr).union(expr.concatenate(eolStar))
    }

    //  [expression] ::= [atom]| [eol][expression] | [expression][eol]
    //  | [lbr] [expression] [dot] [expression] [rbr]
    //  | [list]
    fun buildExpressionAutomaton(depth: Int): Automaton {
        if (depth >= config.maxParentheses) {
            // Базовый случай: возвращаем автомат для [atom]
            return lexemeAutomata[Lexems.ATOM]!!.clone()
        }

        val lexemList = mutableListOf<Automaton>()

        // [atom]
        lexemList.add(lexemeAutomata[Lexems.ATOM]!!.clone())

        // [lbr] [expression] [dot] [expression] [rbr]
        val complexExprAutomaton = lexemeAutomata[Lexems.LBR]!!.clone()
            .concatenate(buildExpressionAutomaton(depth + 1))
            .concatenate(lexemeAutomata[Lexems.DOT]!!.clone())
            .concatenate(buildExpressionAutomaton(depth + 1))
            .concatenate(lexemeAutomata[Lexems.RBR]!!.clone())
        lexemList.add(complexExprAutomaton)

        // [list]
        lexemList.add(buildListAutomaton(depth + 1))

        // Объединяем альтернативы с помощью union
        return lexemList.reduce { acc, automaton -> acc.union(automaton) }
    }

    //  [list] ::= [lbr] [expression]+ [rbr]
    fun buildListAutomaton(depth: Int): Automaton {
        val lbr = lexemeAutomata[Lexems.LBR]!!
        val exprPlus = buildExpressionAutomaton(depth + 1).repeat(1)
        val rbr = lexemeAutomata[Lexems.RBR]!!
        return lbr.concatenate(exprPlus).concatenate(rbr)
    }
}
