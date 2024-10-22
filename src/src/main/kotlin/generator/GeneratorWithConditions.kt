package generator

import dk.brics.automaton.Automaton
import dk.brics.automaton.State
import dk.brics.automaton.Transition
import org.example.models.Lexems
import kotlin.random.Random

class GeneratorWithConditions(
    private val statesNum: Int,
    private val transitions: Int,
    private val acceptingStatesNum: Int,
    private val alphabet: Set<Int>,
    private val lexem: Lexems
) : AbstractGenerator(
    statesNum = statesNum,
    transitions = transitions,
    acceptingStatesNum = acceptingStatesNum,
    alphabet = alphabet
) {

    private var transitionsAdded = 0

    /**
     * Генерация автомата, имеющего конечный язык, непересекающийся с языками из списка
     * @param nonIntersectAutomata список автомат с языками которых не должен пересекаться генерируемый автомат
     */
    fun generateWithConditions(nonIntersectAutomata: List<Automaton>): Automaton {
        while (true) {
            transitionsAdded = 0
            val candidateAutomaton = generateFiniteAutomaton()
            makeEndStatesAccepting(states[0])
            val intersects = nonIntersectAutomata.any { otherAutomaton ->
                !candidateAutomaton.intersection(otherAutomaton).isEmpty
            }

            if (!intersects) {
                return candidateAutomaton
            }
        }
    }

    private fun generateFiniteAutomaton(): Automaton {
        val automaton = Automaton()
        states = List(statesNum) { State() }
        automaton.initialState = states[0]
        val availableTransitions = mutableListOf<Edge>()
        for (i in 1..<statesNum) {
            for (j in (i + 1)..<statesNum) {
                availableTransitions.add(Edge(states[i], states[j]))
            }
        }

        availableTransitions.shuffle()

        // Делаем хотя бы один переход из начального состояния
        addTransition(Edge(states[0], states[Random.nextInt(1, statesNum)]))

        for (e in availableTransitions) {
            if (transitionsAdded >= transitions) break
            addTransition(e)
        }
        automaton.reduce()
        return automaton
    }

    private fun addTransition(transition: Edge) {
        if (usedLabels[transition.from] == null) usedLabels[transition.from] = mutableSetOf()
        val nonAvailableLabels = usedLabels[transition.from]!!
        AutomatonGenerator.randomLabel(alphabet, nonAvailableLabels)?.let {
            transition.from.addTransition(
                Transition(
                    it.digitToChar(),
                    transition.to
                )
            )
            transitionsAdded++
        }
    }
}

fun main() {
    println(
        GeneratorWithConditions(
            statesNum = 10,
            transitions = 30,
            acceptingStatesNum = 10,
            alphabet = (2..9).toSet(),
            lexem = Lexems.DOT
        ).generateWithConditions(emptyList()).toDot()
    )
}