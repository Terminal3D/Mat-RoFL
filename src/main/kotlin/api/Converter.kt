package api

import dk.brics.automaton.Automaton
import dk.brics.automaton.State
import dk.brics.automaton.Transition

private var mainPrefixesMapped: List<String> = emptyList()
private var nonMainPrefixesMapped: List<String> = emptyList()
private var suffixesMapped: List<String> = emptyList()
private var tableMapped: List<List<Boolean>> = emptyList()

private val equivClasses: MutableMap<List<Boolean>, State> = mutableMapOf()

fun CheckTableRequest.toAutomaton(): Automaton {
    this.map()

    val automaton = Automaton.makeEmpty()
    automaton.transformTable()

    return automaton
}


private fun Automaton.transformTable() {
    (mainPrefixesMapped).forEachIndexed { i, prefix ->
        this.addEquivClasses(prefix + suffixesMapped[0], i)
    }

    nonMainPrefixesMapped.forEachIndexed { i, prefix ->
        this.addWords(prefix + suffixesMapped[0], i + mainPrefixesMapped.size)
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