package generator

import dk.brics.automaton.Automaton
import dk.brics.automaton.State
import org.example.models.Lexems
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GeneratorWithConditionsTest {

    @Test
    fun testNoCycles() {
        for (i in 1..100) {
            val generator = GeneratorWithConditions(
                statesNum = Random.nextInt(5, 15),
                transitions = Random.nextInt(5, 30),
                acceptingStatesNum = Random.nextInt(1, 5),
                alphabet = (0..9).toSet(),
                lexem = Lexems.DOT
            )
            val automaton = generator.generateWithConditions(emptyList())

            val hasCycles = hasCycles(automaton)
            assertFalse(hasCycles, "Автомат содержит цикл")
        }
    }

    private fun hasCycles(automaton: Automaton): Boolean {
        val visited = mutableSetOf<State>()
        val stack = mutableSetOf<State>()
        return dfs(automaton.initialState, visited, stack)
    }

    private fun dfs(state: State, visited: MutableSet<State>, stack: MutableSet<State>): Boolean {
        if (stack.contains(state)) {
            return true
        }
        if (visited.contains(state)) {
            return false
        }
        visited.add(state)
        stack.add(state)
        for (transition in state.transitions) {
            val dest = transition.dest
            if (dfs(dest, visited, stack)) {
                return true
            }
        }
        stack.remove(state)
        return false
    }

    @Test
    fun testNonIntersectingAutomata() {
        val nonIntersectAutomata = mutableListOf<Automaton>()
        for (i in 1..10) {
            val generator = GeneratorWithConditions(
                statesNum = Random.nextInt(5, 15),
                transitions = Random.nextInt(5, 30),
                acceptingStatesNum = Random.nextInt(1, 5),
                alphabet = (0..9).toSet(),
                lexem = Lexems.ATOM
            )
            val automaton = generator.generateWithConditions(emptyList())
            nonIntersectAutomata.add(automaton)
        }

        val generator = GeneratorWithConditions(
            statesNum = Random.nextInt(5, 15),
            transitions = Random.nextInt(5, 30),
            acceptingStatesNum = Random.nextInt(1, 5),
            alphabet = (0..9).toSet(),
            lexem = Lexems.DOT
        )
        val automaton = generator.generateWithConditions(nonIntersectAutomata)

        for ((index, otherAutomaton) in nonIntersectAutomata.withIndex()) {
            val intersection = automaton.intersection(otherAutomaton)
            assertTrue(
                intersection.isEmpty,
                "Автомат пересекается с автоматом из списка под индексом $index",
            )
        }
    }
}
