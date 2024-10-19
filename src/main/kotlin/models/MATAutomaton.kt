package org.example.models

import dk.brics.automaton.Automaton
import java.util.*
import kotlin.math.max
import kotlin.random.Random

data class MATAutomaton(
    val automaton: Automaton,
    val config: MATAutomaton.Config,
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
                        val finalAlphabet = alphabet - eolAlphabet
                        val maxLexemLength = Random.nextInt(3, 7)
                        Config(
                            mode = GeneratorMode.EASY,
                            maxParentheses = Random.nextInt(1,3),
                            maxLexemLength = maxLexemLength,
                            eolAlphabet = eolAlphabet,
                            alphabet = finalAlphabet,
                            lexemSizeMap = generateLexemSizesMap(GeneratorMode.EASY, finalAlphabet, maxLexemLength),
                        )
                    }

                    "normal" -> {
                        val eolAlphabet = getEolAlphabet(GeneratorMode.NORMAL, alphabet)
                        val finalAlphabet = alphabet - eolAlphabet
                        val maxLexemLength = Random.nextInt(7, 10)
                        Config(
                            mode = GeneratorMode.NORMAL,
                            maxParentheses = Random.nextInt(3, 5),
                            maxLexemLength = maxLexemLength,
                            eolAlphabet = eolAlphabet,
                            alphabet = finalAlphabet,
                            lexemSizeMap = generateLexemSizesMap(GeneratorMode.NORMAL, finalAlphabet, maxLexemLength),
                        )
                    }

                    "hard" -> {
                        val eolAlphabet = getEolAlphabet(GeneratorMode.HARD, alphabet)
                        val finalAlphabet = alphabet - eolAlphabet
                        val maxLexemLength = Random.nextInt(10, 15)
                        Config(
                            mode = GeneratorMode.HARD,
                            maxParentheses = Random.nextInt(5, 6),
                            maxLexemLength = maxLexemLength,
                            eolAlphabet = eolAlphabet,
                            alphabet = finalAlphabet,
                            lexemSizeMap = generateLexemSizesMap(GeneratorMode.HARD, finalAlphabet, maxLexemLength)
                        )
                    }

                    else -> throw Exception("Некорректный режим")
                }
            }

            private fun getEolAlphabet(generatorMode: GeneratorMode, alphabet: Set<Int>): Set<Int> {
                val iterationsNumber = when (generatorMode) {
                    GeneratorMode.EASY -> 6
                    GeneratorMode.NORMAL -> 4
                    GeneratorMode.HARD -> 2
                }

                return generateSequence { Random.nextInt(0, 10) }
                        .take(iterationsNumber)
                        .toSet()

            }

            private fun generateLexemSizesMap(
                generatorMode: GeneratorMode,
                alphabet: Set<Int>,
                maxLexemLength: Int
            ): Map<Lexems, Lexems.Config> {
                val (min, max) = when (generatorMode) {
                    GeneratorMode.EASY -> Pair(2, maxLexemLength)
                    GeneratorMode.NORMAL -> Pair(5, maxLexemLength)
                    GeneratorMode.HARD -> Pair(8, maxLexemLength)
                }
                val lexemMap = mutableMapOf<Lexems, Lexems.Config>()
                Lexems.entries.forEach { lexem ->
                    val states = Random.nextInt(min, max)
                    val acceptingStates = (when (generatorMode) {
                        GeneratorMode.EASY -> Random.nextInt(min - 1, states)
                        GeneratorMode.NORMAL -> Random.nextInt(min - 1, states)
                        GeneratorMode.HARD -> Random.nextInt(min - 1, states)
                    } + 1) / 2
                    val maxTransitions = (states * alphabet.size) / 2
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
