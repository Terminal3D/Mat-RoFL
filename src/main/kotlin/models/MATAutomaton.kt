package org.example.models

import dk.brics.automaton.Automaton
import java.util.*
import kotlin.random.Random

data class MATAutomaton(
    val automaton : Automaton,
    val config : MATAutomaton.Config
) {
    data class Config(
        val mode : GeneratorMode,
        val maxParentheses : Int,
        val maxLexemLength : Int,
        val eolAlphabet : Set<Int>,
        val alphabet : Set<Int>,
    ) {
        companion object {
            fun factory(mode: String) : Config {
                val alphabet = (0..9).toSet()
                return when(mode.lowercase(Locale.getDefault())) {
                    "easy" -> {
                        val eolAlphabet = getEolAlphabet(GeneratorMode.EASY, alphabet)
                        Config(
                            mode = GeneratorMode.EASY,
                            maxParentheses = Random.nextInt(1, 10),
                            maxLexemLength = Random.nextInt(5, 15),
                            eolAlphabet = eolAlphabet,
                            alphabet = alphabet - eolAlphabet
                        )
                    }
                    "normal" -> {
                        val eolAlphabet = getEolAlphabet(GeneratorMode.NORMAL, alphabet)
                        Config(
                            mode = GeneratorMode.NORMAL,
                            maxParentheses = Random.nextInt(1, 10),
                            maxLexemLength = Random.nextInt(5, 15),
                            eolAlphabet = eolAlphabet,
                            alphabet = alphabet - eolAlphabet
                        )
                    }
                    "hard" -> {
                        val eolAlphabet = getEolAlphabet(GeneratorMode.HARD, alphabet)
                        Config(
                            mode = GeneratorMode.HARD,
                            maxParentheses = Random.nextInt(1, 10),
                            maxLexemLength = Random.nextInt(5, 15),
                            eolAlphabet = eolAlphabet,
                            alphabet = alphabet - eolAlphabet
                        )
                    }
                    else -> throw Exception("Некорректный режим")
                }
            }

            private fun getEolAlphabet(generatorMode: GeneratorMode, alphabet: Set<Int>) : Set<Int> {
                val iterationsNumber = when(generatorMode) {
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

        }
    }
}

enum class GeneratorMode {
    EASY, NORMAL, HARD
}