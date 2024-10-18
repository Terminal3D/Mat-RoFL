package org.example.models

import dk.brics.automaton.Automaton
import java.util.*
import kotlin.random.Random

data class MATAutomaton(
    val automaton: Automaton,
    val config: MATAutomaton.Config
) {
    data class Config(
        val mode: GeneratorMode,
        val maxParentheses: Int,
        val maxLexemLength: Int,
        val eolAlphabet: Set<Int>,
        val alphabet: Set<Int>,
        val lexemSizeMap: Map<Lexems, Lexems.Config>
    ) {
        companion object {
            fun factory(mode: String): Config {
                val alphabet = (0..9).toSet()
                return when (mode.lowercase(Locale.getDefault())) {
                    "easy" -> {
                        val eolAlphabet = getEolAlphabet(GeneratorMode.EASY, alphabet)
                        Config(
                            mode = GeneratorMode.EASY,
                            maxParentheses = Random.nextInt(1, 10),
                            maxLexemLength = Random.nextInt(5, 15),
                            eolAlphabet = eolAlphabet,
                            alphabet = alphabet - eolAlphabet,
                            lexemSizeMap = generateLexemSizesMap(GeneratorMode.EASY),
                        )
                    }

                    "normal" -> {
                        val eolAlphabet = getEolAlphabet(GeneratorMode.NORMAL, alphabet)
                        Config(
                            mode = GeneratorMode.NORMAL,
                            maxParentheses = Random.nextInt(1, 10),
                            maxLexemLength = Random.nextInt(5, 15),
                            eolAlphabet = eolAlphabet,
                            alphabet = alphabet - eolAlphabet,
                            lexemSizeMap = generateLexemSizesMap(GeneratorMode.NORMAL),
                        )
                    }

                    "hard" -> {
                        val eolAlphabet = getEolAlphabet(GeneratorMode.HARD, alphabet)
                        Config(
                            mode = GeneratorMode.HARD,
                            maxParentheses = Random.nextInt(1, 10),
                            maxLexemLength = Random.nextInt(5, 15),
                            eolAlphabet = eolAlphabet,
                            alphabet = alphabet - eolAlphabet,
                            lexemSizeMap = generateLexemSizesMap(GeneratorMode.HARD)
                        )
                    }

                    else -> throw Exception("Некорректный режим")
                }
            }

            private fun getEolAlphabet(generatorMode: GeneratorMode, alphabet: Set<Int>): Set<Int> {
                val iterationsNumber = when (generatorMode) {
                    GeneratorMode.EASY -> 2
                    GeneratorMode.NORMAL -> 5
                    GeneratorMode.HARD -> 8
                }

                return alphabet.minus(
                    generateSequence { Random.nextInt(0, 10) }
                        .take(iterationsNumber)
                        .toSet()
                )
            }

            private fun generateLexemSizesMap(generatorMode: GeneratorMode): Map<Lexems, Lexems.Config> {
                val (min, max) = when (generatorMode) {
                    GeneratorMode.EASY -> Pair(2, 5)
                    GeneratorMode.NORMAL -> Pair(5, 15)
                    GeneratorMode.HARD -> Pair(15, 50)
                }
                val lexemMap = mutableMapOf<Lexems, Lexems.Config>()
                Lexems.entries.forEach { lexem ->
                    val states = Random.nextInt(min, max)
                    val acceptingStates = when (generatorMode) {
                        GeneratorMode.EASY -> Random.nextInt(min - 1, states - 1)
                        GeneratorMode.NORMAL -> Random.nextInt(min - 1, states - 1)
                        GeneratorMode.HARD -> Random.nextInt(min - 1, states - 1)
                    }
                    val maxTransitions = (1..states).sum()
                    val transitions = Random.nextInt(states - 1, maxTransitions)
                    lexemMap[lexem] = Lexems.Config(
                        states = states,
                        transitions = transitions,
                        acceptingStates = acceptingStates
                    )
                }
                return lexemMap
            }

        }
    }
}

enum class GeneratorMode {
    EASY, NORMAL, HARD
}

fun generateRandomSet(size: Int): Set<Int> =
    generateSequence { Random.nextInt(0, 10) }
        .distinct()
        .take(size)
        .toSet()
