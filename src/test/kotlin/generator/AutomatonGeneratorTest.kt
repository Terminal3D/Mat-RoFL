package generator

import dk.brics.automaton.Automaton
import org.example.generator.AutomatonGenerator
import org.example.models.Lexems
import org.example.models.MATAutomaton
import kotlin.test.Test
import kotlin.test.assertTrue

class AutomatonGeneratorTest {

    private val automatonGenerator = AutomatonGenerator()
    private lateinit var lexemeAutomata: Map<Lexems, Automaton>
    private lateinit var programAutomaton: Automaton

    // Initialize the automata before running tests
    private fun setup() {
        val matautomaton = automatonGenerator.create("hard")
        programAutomaton = matautomaton.automaton
        lexemeAutomata = automatonGenerator.getLexemeAutomata()
    }

    // Helper function to generate a word accepted by a lexeme automaton
    private fun generateWordFromLexeme(lexeme: Lexems): String {
        val automaton = lexemeAutomata[lexeme] ?: error("Automaton for $lexeme not found")
        return automaton.getShortestExample(true)
    }

    @Test
    fun testProgramAutomaton() {
        setup() // Initialize automata

        // List of expressions to test
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
            // Add more expressions as needed
        )

        for ((index, expression) in expressions.withIndex()) {
            // Generate the word by concatenating words from lexeme automata
            val word = expression.joinToString(separator = "") { lexeme ->
                generateWordFromLexeme(lexeme)
            }

            // Check if the word is accepted by the program automaton
            val accepted = programAutomaton.run(word)
            assertTrue(accepted, "Expression $index not accepted: $word")
        }
    }
}