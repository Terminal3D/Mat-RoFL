package generator

import dk.brics.automaton.Automaton
import dk.brics.automaton.State
import dk.brics.automaton.Transition
import kotlin.random.Random

class AtomGenerator(
    private val statesNum: Int,
    private val transitions: Int,
    private val acceptingStatesNum: Int,
    private val alphabet: Set<Int>
) {
    private val transitionSet = mutableSetOf<Edge>()
    private val visitedStatesForMainDFS = mutableSetOf<State>()
    private val visitedStatesForAccepting = mutableSetOf<State>()
    private val acceptingStates = mutableSetOf<State>()
    private val usedLabels = mutableMapOf<State, MutableSet<Int>>()
    private lateinit var states: List<State>
    fun generate(): Automaton {
        val automaton = Automaton()
        states = List(statesNum) { State() }
        val startState = states[0]
        automaton.initialState = startState
        dfs(startState)
        makeEndStatesAccepting(startState)
        makeRandomStatesAccepting()
        visitedStatesForMainDFS.clear()
        automaton.minimize()
        return automaton
    }

    private fun dfs(currentState: State) {
        if (currentState in visitedStatesForMainDFS) return
        visitedStatesForMainDFS.add(currentState)
        for (i in (1..<statesNum)) {
            if (transitionSet.size > transitions) break
            val targetState = states[i]

            if (Random.nextBoolean() || transitionSet.isEmpty()) {
                val edge = Edge(currentState, targetState)
                if (Edge(currentState, targetState) !in transitionSet) {
                    if (usedLabels[currentState] == null) usedLabels[currentState] = mutableSetOf()
                    val nonAvailableLabels = usedLabels[currentState]!!
                    randomLabel(nonAvailableLabels)?.let { label ->
                        usedLabels[currentState]?.add(label)
                        currentState.addTransition(Transition(label.digitToChar(), targetState))
                        transitionSet.add(edge)
                        dfs(targetState)
                    }
                }
            }
        }
    }

    private fun makeEndStatesAccepting(currentState: State) {
        if (currentState in visitedStatesForAccepting) return
        visitedStatesForAccepting.add(currentState)
        if (currentState.transitions.isEmpty()) {
            currentState.isAccept = true
            acceptingStates.add(currentState)
            return
        }
        currentState.transitions.forEach { makeEndStatesAccepting(it.dest) }
    }

    private fun makeRandomStatesAccepting() {
        while (acceptingStates.size < acceptingStatesNum) {
            val randomState = states[Random.nextInt(1, statesNum)]
            if (!randomState.isAccept && visitedStatesForMainDFS.contains(randomState)) {
                randomState.isAccept = true
                acceptingStates.add(randomState)
            }
        }
    }

    private fun randomLabel(usedLabels: Set<Int>): Int? {
        val available = alphabet - usedLabels
        if (available.isEmpty()) return null
        return available.random()
    }
}

fun main() {
    println(
        AtomGenerator(
            statesNum = 10230,
            transitions = 1544400,
            alphabet = (2..9).toSet(),
            acceptingStatesNum = 12
        ).generate().isDeterministic
    )
}


data class Edge(
    val from: State,
    val to: State
)