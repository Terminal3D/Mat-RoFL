package generator

import dk.brics.automaton.Automaton
import org.example.models.Lexems
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

        val expressions = listOf(
            listOf(Lexems.ATOM),
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

        for ((index, expression) in expressions.withIndex()) {
            val word = expression.joinToString(separator = "") { lexeme ->
                generateWordFromLexeme(lexeme)
            }

            val accepted = programAutomaton.run(word)
            assertTrue(accepted, "Expression $index not accepted: $word")
        }
    }
}