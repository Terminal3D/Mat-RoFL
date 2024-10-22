package generator

import dk.brics.automaton.State

abstract class AbstractGenerator(
    private val statesNum: Int,
    private val transitions: Int,
    private val acceptingStatesNum: Int,
    private val alphabet: Set<Int>
) {

    internal val usedLabels = mutableMapOf<State, MutableSet<Int>>()
    internal lateinit var states: List<State>
    internal val visitedStatesForAccepting = mutableSetOf<State>()
    internal val acceptingStates = mutableSetOf<State>()
    internal fun makeEndStatesAccepting(currentState: State) {
        if (currentState in visitedStatesForAccepting) return
        visitedStatesForAccepting.add(currentState)
        if (currentState.transitions.isEmpty()) {
            currentState.isAccept = true
            acceptingStates.add(currentState)
            return
        }
        currentState.transitions.forEach { makeEndStatesAccepting(it.dest) }
    }
}