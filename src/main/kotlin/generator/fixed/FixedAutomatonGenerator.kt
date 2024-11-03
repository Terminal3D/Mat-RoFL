package generator.fixed

import dk.brics.automaton.Automaton
import dk.brics.automaton.State
import dk.brics.automaton.Transition
import generator.buildProgramAutomaton
import models.Lexems
import org.example.models.MATAutomaton

class FixedAutomatonGenerator {

    private lateinit var config: MATAutomaton.Config.FixedConfig
    private val lexemeAutomata = mutableMapOf<Lexems, Automaton>()

    fun create(size: Int): MATAutomaton {
        while (true) {
            config = MATAutomaton.Config.FixedConfig.factory(size)
            val automaton = generateAutomaton()
            if (automaton.numberOfStates != size) continue
            println("States: ${automaton.numberOfStates}, Transitions: ${automaton.numberOfTransitions}, AcceptingState: ${automaton.acceptStates.size}")
            println("Max parentheses: ${config.maxParentheses}, max lexeme length: ${config.maxLexemeLength}")
            return MATAutomaton(
                automaton = automaton,
                config = config
            )
        }

    }

    private fun generateAutomaton(): Automaton {
        config.lexemeMap.forEach { (lexeme, config) ->
            val automaton = when (lexeme) {
                Lexems.ATOM -> generateLexemeAutomaton(config, true)
                else -> generateLexemeAutomaton(config, false)
            }
            lexemeAutomata[lexeme] = automaton
            // println("${lexeme.name}\n${automaton.toDot()}")
        }

        val result =
            buildProgramAutomaton(lexemeAutomataMap = lexemeAutomata, maxParenthesesValue = config.maxParentheses)
        result.determinize()
        result.minimize()
        // println("RESULT:\n${result.toDot()}")

        return result
    }

    private fun generateLexemeAutomaton(config: Lexems.Config, addCycle: Boolean): Automaton {
        val automaton = Automaton.makeEmpty()
        var state = automaton.initialState
        val alphabet = config.transitions
        alphabet.forEach { label ->
            val newState = State()
            state.addTransition(
                Transition(
                    label.digitToChar(),
                    newState
                )
            )
            state = newState
        }
        state.isAccept = true
        if (addCycle) {
            state.addTransition(
                Transition(
                    alphabet.random().digitToChar(),
                    automaton.initialState
                )
            )
        }
        automaton.determinize()
        automaton.minimize()
        return automaton
    }
}