package generator

import dk.brics.automaton.Automaton
import org.example.models.Lexems
import org.junit.jupiter.api.Assertions.assertFalse
import kotlin.test.Test
import kotlin.test.assertTrue

class AutomatonGeneratorTest {

    private val automatonGenerator = AutomatonGenerator()
    private lateinit var lexemeAutomata: Map<Lexems, Automaton>
    private lateinit var programAutomaton: Automaton

    private fun setup() {
        val matautomaton = automatonGenerator.create("normal")
        programAutomaton = matautomaton.automaton
        lexemeAutomata = automatonGenerator.getLexemeAutomata()
    }

    private fun generateWordFromLexeme(lexeme: Lexems): String {
        val automaton = lexemeAutomata[lexeme] ?: error("Automaton for $lexeme not found")
        return automaton.getShortestExample(true)
    }

    @Test
    fun testProgramAutomaton() {
        setup()

        val expressionsValid = listOf(
            listOf(Lexems.ATOM),
            listOf(Lexems.ATOM, Lexems.ATOM), // Тоже валидно, т.к. [program] ::= [eol]*([expression][eol]*)+
            listOf(Lexems.EOL, Lexems.ATOM),
            listOf(Lexems.ATOM, Lexems.EOL),
            listOf(Lexems.LBR, Lexems.ATOM, Lexems.DOT, Lexems.ATOM, Lexems.RBR),
            listOf(Lexems.LBR, Lexems.LBR, Lexems.ATOM, Lexems.DOT, Lexems.ATOM, Lexems.RBR, Lexems.DOT, Lexems.ATOM, Lexems.RBR),
            listOf(Lexems.LBR, Lexems.ATOM, Lexems.DOT, Lexems.LBR, Lexems.ATOM, Lexems.DOT, Lexems.ATOM, Lexems.RBR, Lexems.RBR),
            listOf(Lexems.LBR, Lexems.ATOM, Lexems.DOT, Lexems.ATOM, Lexems.RBR, Lexems.EOL),
            listOf(Lexems.EOL, Lexems.LBR, Lexems.ATOM, Lexems.DOT, Lexems.ATOM, Lexems.RBR),
            listOf(Lexems.LBR, Lexems.ATOM, Lexems.DOT, Lexems.ATOM, Lexems.RBR, Lexems.EOL, Lexems.ATOM),
            listOf(Lexems.LBR, Lexems.ATOM, Lexems.DOT, Lexems.ATOM, Lexems.RBR, Lexems.EOL, Lexems.LBR, Lexems.ATOM, Lexems.DOT, Lexems.ATOM, Lexems.RBR),
        )

        for ((index, expression) in expressionsValid.withIndex()) {
            val word = expression.joinToString(separator = "") { lexeme ->
                generateWordFromLexeme(lexeme)
            }

            val accepted = programAutomaton.run(word)
            assertTrue(accepted, "Expression $index not accepted: $word")
        }
    }

    @Test
    fun testProgramAutomatonInvalid() {
        setup()

        val expressionsInvalid = listOf(
            listOf(Lexems.LBR, Lexems.ATOM, Lexems.DOT, Lexems.ATOM),
            listOf(Lexems.ATOM, Lexems.DOT, Lexems.ATOM, Lexems.RBR),
            listOf(Lexems.DOT, Lexems.ATOM),
            listOf(Lexems.ATOM, Lexems.ATOM, Lexems.DOT),
            listOf(Lexems.LBR, Lexems.RBR),
            listOf(Lexems.DOT, Lexems.DOT),
            listOf(Lexems.RBR, Lexems.LBR),
            listOf(Lexems.LBR, Lexems.DOT, Lexems.ATOM, Lexems.RBR),
        )

        for ((index, expression) in expressionsInvalid.withIndex()) {
            val word = expression.joinToString(separator = "") { lexeme ->
                generateWordFromLexeme(lexeme)
            }

            val accepted = programAutomaton.run(word)
            assertFalse(accepted, "Invalid expression $index incorrectly accepted: $word")
        }
    }
}
