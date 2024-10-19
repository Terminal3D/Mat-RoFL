package generator

import dk.brics.automaton.Automaton
import dk.brics.automaton.State
import dk.brics.automaton.Transition
import net.automatalib.alphabet.ArrayAlphabet
import net.automatalib.util.automaton.random.RandomAutomata
import org.example.generator.AutomatonGenerator.Companion.randomLabel
import kotlin.random.Random
import kotlin.random.asJavaRandom

class AtomGenerator(
    private val statesNum: Int,
    private val transitions: Int,
    private val acceptingStatesNum: Int,
    private val alphabet: Set<Int>
): AbstractGenerator(
    statesNum = statesNum,
    transitions = transitions,
    acceptingStatesNum = acceptingStatesNum,
    alphabet = alphabet
) {
    private val transitionSet = mutableSetOf<Edge>()
    private val visitedStatesForMainDFS = mutableSetOf<State>()
    private var counter = 0
    fun generate(): Automaton {

        val alph = ArrayAlphabet(*alphabet.map { it.digitToChar() }.toTypedArray())
        RandomAutomata.randomDFA(Random.Default.asJavaRandom(), statesNum, alph)



        val automaton = Automaton()
        states = List(statesNum) { State() }
        val startState = states[0]
        automaton.initialState = startState
        dfs(startState)
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

            if (Random.nextBoolean() || transitionSet.isEmpty()) {
                val edge = Edge(currentState, targetState)
                if (edge !in transitionSet) {
                    if (usedLabels[currentState] == null) usedLabels[currentState] = mutableSetOf()
                    val nonAvailableLabels = usedLabels[currentState]!!
                    randomLabel(alphabet, nonAvailableLabels)?.let { label ->
                        nonAvailableLabels.add(label)
                        currentState.addTransition(Transition(label.digitToChar(), targetState))
                        transitionSet.add(edge)
                        if (targetState !in visitedStatesForMainDFS){
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
        AtomGenerator(
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