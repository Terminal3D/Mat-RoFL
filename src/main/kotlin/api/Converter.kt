package api

import dk.brics.automaton.Automaton
import dk.brics.automaton.State
import dk.brics.automaton.Transition
import org.example.models.GeneratorMode
import java.util.*

private var mainPrefixesMapped: List<String> = emptyList()
private var nonMainPrefixesMapped: List<String> = emptyList()
private var suffixesMapped: List<String> = emptyList()
private var tableMapped: List<List<Boolean>> = emptyList()
private var maxSuffixLength: Int = 0
private var maxPrefixLength: Int = 0
private var transitionNum: Int = 0

private val equivClasses: MutableMap<List<Boolean>, State> = mutableMapOf()

fun CheckTableRequest.toAutomaton(): Automaton {
    this.map()

    val automaton = Automaton.makeEmpty()
    automaton.transformTable()
    transitionNum = automaton.numberOfTransitions
    return automaton
}

fun Automaton.getExample(mode: GeneratorMode): String {

    val minLength = when(mode) {
        GeneratorMode.EASY -> return this.getShortestExample(true)
        GeneratorMode.NORMAL -> maxPrefixLength + maxSuffixLength
        GeneratorMode.HARD -> maxPrefixLength + maxSuffixLength
    }

    val maxLength = transitionNum

    val queue: Queue<Pair<State, String>> = LinkedList()
    val visited = mutableSetOf<State>()

    while (queue.isNotEmpty()) {
        val (state, path) = queue.poll()

        if (state !in visited) {
            visited.add(state)

            if (path.length > maxLength) continue

            if (state.isAccept && path.length >= minLength) {
                return path
            }

            for (transition in state.transitions) {
                val nextState = transition.dest
                queue.add(Pair(nextState, path + transition.max))
            }
        }
    }

    return this.getShortestExample(true)
}
private fun Automaton.transformTable() {
    (mainPrefixesMapped).forEachIndexed { i, prefix ->
        this.addEquivClasses(prefix + suffixesMapped[0], i)
        maxPrefixLength = if (prefix.length > maxPrefixLength) prefix.length else maxPrefixLength
    }

    nonMainPrefixesMapped.forEachIndexed { i, prefix ->
        this.addWords(prefix + suffixesMapped[0], i + mainPrefixesMapped.size)
        maxPrefixLength = if (prefix.length > maxPrefixLength) prefix.length else maxPrefixLength
    }

    suffixesMapped.forEach { suffix ->
        maxSuffixLength = if (suffix.length > maxSuffixLength) suffix.length else maxSuffixLength
    }
}

private fun Automaton.addWords(word: String, i: Int) {
    val equivClass = equivClasses[tableMapped[i]]
        ?: throw Exception("Для префикса $word из нижней таблицы не найдено класса эквивалентности")
    var currentState = initialState
    for (j in (0 until word.length - 1)) {
        val next = currentState.transitions.firstOrNull { it.max == word[j] }
        if (next == null) {
            val newState = State()
            currentState.addTransition(
                Transition(
                    word[j], newState
                )
            )
            currentState = newState
        } else {
            currentState = next.dest
        }
    }
    currentState.addTransition(
        Transition(
            word.last(), equivClass
        )
    )
}



private fun Automaton.addEquivClasses(word: String, i: Int) {
    var currentState = this.initialState
    for (s in word) {
        val next = currentState.transitions.firstOrNull { it.max == s }
        if (next == null) {
            val newState = State()
            currentState.addTransition(
                Transition(
                    s, newState
                )
            )
            currentState = newState
        } else {
            currentState = next.dest
        }
    }
    if (tableMapped[i][0]) currentState.isAccept = true
    equivClasses[tableMapped[i]] = currentState
}

private fun CheckTableRequest.map() {
    mainPrefixesMapped = parseWords(this.mainPrefixes)
    nonMainPrefixesMapped = parseWords(this.nonMainPrefixes)
    suffixesMapped = parseWords(this.suffixes)

    val tableValues = this.table.trim().split(" ").map { it.toInt() }

    val numRows = mainPrefixesMapped.size + nonMainPrefixesMapped.size
    val numCols = suffixesMapped.size

    if (tableValues.size != numRows * numCols) {
        throw IllegalArgumentException("Размер таблицы не соответствует ожидаемым размерам: ожидается" +
                " ${numRows * numCols}, получено ${tableValues.size}")
    }

    tableMapped = tableValues.chunked(numCols).map { row -> row.map { it == 1 } }
}

private fun parseWords(s: String): List<String> {
    val result = mutableListOf<String>()
    var i = 0
    while (i < s.length) {
        if (s[i] == ' ') {
            i++
        } else {
            val sb = StringBuilder()
            while (i < s.length && s[i] != ' ') {
                if (s[i] != 'ε') {
                    sb.append(s[i])
                }
                i += 1
            }
            result.add(sb.toString())
        }
    }
    return result
}

private enum class Token(val value: String) {
    Epsilon("epsilon")
}