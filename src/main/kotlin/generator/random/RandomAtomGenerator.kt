package generator.random

import dk.brics.automaton.Automaton
import dk.brics.automaton.State
import dk.brics.automaton.Transition
import generator.random.RandomAutomatonGenerator.Companion.randomLabel
import kotlin.random.Random

class RandomAtomGenerator(
    private val statesNum: Int,
    private val transitions: Int,
    private val acceptingStatesNum: Int,
    private val alphabet: Set<Int>
) : RandomAbstractGenerator(
    statesNum = statesNum,
    transitions = transitions,
    acceptingStatesNum = acceptingStatesNum,
    alphabet = alphabet
) {
    private val transitionSet = mutableSetOf<Edge>()
    private val visitedStatesForMainDFS = mutableSetOf<State>()
    private var counter = 0
    private var hasCycles = false
    fun generate(): Automaton {
        val automaton = Automaton()
        states = List(statesNum) { State() }
        val startState = states[0]
        automaton.initialState = startState
        dfs(startState)
        // Проверяем, что язык автомата бесконечный, если нет -> добавляем цикл
        checkCycles(startState)
        if (!hasCycles) {
            addCycle(startState)
        }
        makeEndStatesAccepting(startState)
        makeRandomStatesAccepting()
        automaton.reduce()
        automaton.minimize()
        return automaton
    }

    private fun dfs(currentState: State) {
        visitedStatesForMainDFS.add(currentState)
        for (i in (1..<statesNum).shuffled()) {
            if (transitionSet.size >= transitions) {
                break
            }
            val targetState = states[i]

            if (Random.nextBoolean() || currentState.transitions.isEmpty()) {
                val edge = Edge(currentState, targetState)
                if (edge !in transitionSet) {
                    if (usedLabels[currentState] == null) usedLabels[currentState] = mutableSetOf()
                    val nonAvailableLabels = usedLabels[currentState]!!
                    randomLabel(alphabet, nonAvailableLabels)?.let { label ->
                        nonAvailableLabels.add(label)
                        currentState.addTransition(Transition(label.digitToChar(), targetState))
                        transitionSet.add(edge)
                        if (targetState !in visitedStatesForMainDFS) {
                            dfs(targetState)
                        }
                    }
                }
            }
        }
    }

    private fun makeRandomStatesAccepting() {
        val visitedStates = visitedStatesForMainDFS - states[0]
        while (acceptingStates.size < acceptingStatesNum && acceptingStates.size < visitedStatesForMainDFS.size / 2) {
            val randomState = visitedStates.random()
            if (!randomState.isAccept) {
                randomState.isAccept = true
                acceptingStates.add(randomState)
            }
        }
    }

    private enum class Colors {
        GREY, BLACK
    }

    private val colors = mutableMapOf<State, Colors>()

    private fun checkCycles(currentState: State) {
        colors[currentState] = Colors.GREY
        currentState.transitions.forEach { transition ->
            if (colors[transition.dest] == null) {
                checkCycles(transition.dest)
            }
            if (colors[transition.dest] == Colors.GREY) {
                hasCycles = true
            }
        }
        colors[currentState] = Colors.BLACK
    }

    private fun addCycle(initialState: State) {
        while(true) {
            val randState = visitedStatesForMainDFS.random()
            if (randState != initialState) {
                if (usedLabels[randState] == null) usedLabels[randState] = mutableSetOf()
                val label = randomLabel(alphabet, usedLabels[randState]!!)
                if (label != null) {
                    randState.addTransition(
                        Transition(
                            label.digitToChar(),
                            initialState
                        )
                    )
                    usedLabels[randState]!!.add(label)
                    break
                }
            }
        }
    }


    private fun clear() {
        transitionSet.clear()
        visitedStatesForMainDFS.clear()
        visitedStatesForAccepting.clear()
        acceptingStates.clear()
        usedLabels.clear()
        states = emptyList()
        counter = 0
    }
}

fun main() {
    for (i in (1..1000)) {
        RandomAtomGenerator(
            statesNum = 3,
            transitions = 2,
            alphabet = (2..9).toSet(),
            acceptingStatesNum = 1
        ).generate()
    }
}

data class Edge(
    val from: State,
    val to: State
)