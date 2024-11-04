package org.example.models

import dk.brics.automaton.Automaton
import models.Lexems
import java.util.*
import kotlin.math.max
import kotlin.random.Random

data class MATAutomaton(
    val automaton: Automaton,
    val config: Config,
) {

    sealed class Config(
        open val maxLexemeLength: Int,
        open val maxParentheses: Int,
        open val mode: GeneratorMode
    ) {
        data class RandomConfig(
            override val mode: GeneratorMode,
            override val maxParentheses: Int,
            override val maxLexemeLength: Int,
            val eolAlphabet: Set<Int>,
            val alphabet: Set<Int>,
            val lexemeMap: Map<Lexems, Lexems.Config>
        ) : Config(maxLexemeLength, maxParentheses, mode) {
            companion object {
                fun factory(mode: String): RandomConfig {
                    val alphabet = (0..9).toSet()
                    return when (mode.lowercase(Locale.getDefault())) {
                        "easy" -> {
                            val eolAlphabet = getEolAlphabet(GeneratorMode.EASY, alphabet)
                            val finalAlphabet = alphabet - eolAlphabet
                            val maxLexemLength = Random.nextInt(3, 7)
                            RandomConfig(
                                mode = GeneratorMode.EASY,
                                maxParentheses = Random.nextInt(1, 3),
                                maxLexemeLength = maxLexemLength,
                                eolAlphabet = eolAlphabet,
                                alphabet = finalAlphabet,
                                lexemeMap = generateLexemSizesMap(GeneratorMode.EASY, finalAlphabet, maxLexemLength),
                            )
                        }

                        "normal" -> {
                            val eolAlphabet = getEolAlphabet(GeneratorMode.NORMAL, alphabet)
                            val finalAlphabet = alphabet - eolAlphabet
                            val maxLexemLength = Random.nextInt(7, 10)
                            RandomConfig(
                                mode = GeneratorMode.NORMAL,
                                maxParentheses = Random.nextInt(3, 5),
                                maxLexemeLength = maxLexemLength,
                                eolAlphabet = eolAlphabet,
                                alphabet = finalAlphabet,
                                lexemeMap = generateLexemSizesMap(
                                    GeneratorMode.NORMAL,
                                    finalAlphabet,
                                    maxLexemLength
                                ),
                            )
                        }

                        "hard" -> {
                            val eolAlphabet = getEolAlphabet(GeneratorMode.HARD, alphabet)
                            val finalAlphabet = alphabet - eolAlphabet
                            val maxLexemLength = Random.nextInt(10, 15)
                            RandomConfig(
                                mode = GeneratorMode.HARD,
                                maxParentheses = Random.nextInt(5, 7),
                                maxLexemeLength = maxLexemLength,
                                eolAlphabet = eolAlphabet,
                                alphabet = finalAlphabet,
                                lexemeMap = generateLexemSizesMap(GeneratorMode.HARD, finalAlphabet, maxLexemLength)
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
                        else -> throw Exception("Некорректный режим")
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
                        else -> throw Exception("Некорректный режим")
                    }
                    val lexemMap = mutableMapOf<Lexems, Lexems.Config>()
                    Lexems.entries.forEach { lexem ->
                        val states = Random.nextInt(min, max)
                        val acceptingStates = (when (generatorMode) {
                            GeneratorMode.EASY -> Random.nextInt(min - 1, states)
                            GeneratorMode.NORMAL -> Random.nextInt(min - 1, states)
                            GeneratorMode.HARD -> Random.nextInt(min - 1, states)
                            else -> throw Exception("Некорректный режим")
                        } + 1) / 2
                        val maxTransitions = (states * alphabet.size) / 2
                        val transitions = Random.nextInt(states - 1, maxTransitions)
                        lexemMap[lexem] = Lexems.Config(
                            states = states,
                            transitionsNum = transitions,
                            acceptingStates = acceptingStates
                        )
                    }
                    return lexemMap
                }
            }
        }

        data class FixedConfig(
            override val mode: GeneratorMode,
            override val maxParentheses: Int,
            override val maxLexemeLength: Int,
            val lexemeMap: Map<Lexems, Lexems.Config>,
        ) : Config(maxLexemeLength, maxParentheses, mode) {
            companion object {

                private var lexemeLengthsResults: Pair<Int, Int>? = null

                private const val ONE_PAR_TRIVIAL_STATES_NUM = 11
                private const val TWO_PAR_TRIVIAL_STATES_NUM = 30
                private const val THREE_PAR_TRIVIAL_STATES_NUM = 81
                private const val FOUR_PAR_TRIVIAL_STATES_NUM = 213
                private const val FIVE_PAR_TRIVIAL_STATES_NUM = 560

                private const val ONE_PAR_LEXEME_INCREMENT = 9
                private const val TWO_PAR_LEXEME_INCREMENT = 24
                private const val THREE_PAR_LEXEME_INCREMENT = 64
                private const val FOUR_PAR_LEXEME_INCREMENT = 168
                private const val FIVE_PAR_LEXEME_INCREMENT = 441

                private const val ONE_PAR_SPEC_LEXEME_INCREMENT = 1
                private const val TWO_PAR_SPEC_LEXEME_INCREMENT = 3
                private const val THREE_PAR_SPEC_LEXEME_INCREMENT = 9
                private const val FOUR_PAR_SPEC_LEXEME_INCREMENT = 24
                private const val FIVE_PAR_SPEC_LEXEME_INCREMENT = 64

                fun factory(size: Int): FixedConfig {

                    val (lexMap, maxLexemeLength, maxParentheses) = when {
                        size < 2 -> throw Exception("У автомата должно быть минимум 2 состояния")
                        size == 3 -> throw Exception("Невозможно сгенерировать автомат с 3 состояниями")
                        size in (2..10) -> {
                            val lexemeLength = if (size % 2 == 0) size / 2 else size / 3
                            val specLexemeLength = if (size % 2 == 0) 1 else 2
                            Triple(
                                totallyDisjoint(
                                    lexemeLength = lexemeLength,
                                    specLexeme = Lexems.ATOM,
                                    specLexemeLength = specLexemeLength,
                                    isAtomSingleState = false
                                ),
                                max(lexemeLength, specLexemeLength),
                                0
                            )
                        }
                        /**
                         * Длинная (неоптимальная проверка), позволяющая с большой вероятностью сгенерировать
                         * автомат с нетривиальными размерами автоматов лексем и максимальным количеством скобок
                         * (т.е. > 2 и > 1 соответственно). Проверки устроены так, что сверху вниз проверяются
                         * условия в зависимости от трудности их выполнения
                         */
                        else -> {
                            val (lexemeLength, maxParentheses, specLexemeLength) = when {
                                size >= 560 && findLexemeLengthsForSize(
                                    FIVE_PAR_TRIVIAL_STATES_NUM,
                                    FIVE_PAR_LEXEME_INCREMENT,
                                    FIVE_PAR_SPEC_LEXEME_INCREMENT,
                                    size
                                ) != null -> {
                                    val (lexemeLength, specLexemeLength) = lexemeLengthsResults!!
                                    Triple(
                                        lexemeLength,
                                        5,
                                        specLexemeLength
                                    )
                                }

                                size >= 213 && findLexemeLengthsForSize(
                                    FOUR_PAR_TRIVIAL_STATES_NUM,
                                    FOUR_PAR_LEXEME_INCREMENT,
                                    FOUR_PAR_SPEC_LEXEME_INCREMENT,
                                    size
                                ) != null -> {
                                    val (lexemeLength, specLexemeLength) = lexemeLengthsResults!!
                                    Triple(
                                        lexemeLength,
                                        4,
                                        specLexemeLength
                                    )
                                }

                                size >= 81 && findLexemeLengthsForSize(
                                    THREE_PAR_TRIVIAL_STATES_NUM,
                                    THREE_PAR_LEXEME_INCREMENT,
                                    THREE_PAR_SPEC_LEXEME_INCREMENT,
                                    size
                                ) != null -> {
                                    val (lexemeLength, specLexemeLength) = lexemeLengthsResults!!
                                    Triple(
                                        lexemeLength,
                                        3,
                                        specLexemeLength
                                    )
                                }

                                size >= 30 && findLexemeLengthsForSize(
                                    TWO_PAR_TRIVIAL_STATES_NUM,
                                    TWO_PAR_LEXEME_INCREMENT,
                                    TWO_PAR_SPEC_LEXEME_INCREMENT,
                                    size
                                ) != null -> {
                                    val (lexemeLength, specLexemeLength) = lexemeLengthsResults!!
                                    Triple(
                                        lexemeLength,
                                        2,
                                        specLexemeLength
                                    )
                                }

                                findLexemeLengthsForSize(
                                    ONE_PAR_TRIVIAL_STATES_NUM,
                                    ONE_PAR_LEXEME_INCREMENT,
                                    ONE_PAR_SPEC_LEXEME_INCREMENT,
                                    size
                                ) != null -> {
                                    val (lexemeLength, specLexemeLength) = lexemeLengthsResults!!
                                    Triple(
                                        lexemeLength,
                                        1,
                                        specLexemeLength
                                    )
                                }

                                else -> {
                                    Triple(1, 1, size - 10)
                                }
                            }

                            Triple(
                                totallyDisjoint(
                                    lexemeLength = lexemeLength,
                                    specLexeme = Lexems.DOT,
                                    specLexemeLength = specLexemeLength
                                ),
                                max(lexemeLength, specLexemeLength),
                                maxParentheses
                            )
                        }
                    }



                    return FixedConfig(
                        mode = GeneratorMode.FIXED,
                        maxLexemeLength = maxLexemeLength,
                        maxParentheses = maxParentheses,
                        lexemeMap = lexMap
                    )
                }

                private fun findLexemeLengthsForSize(
                    initialAutomatonSize: Int,
                    lexemeIncrement: Int,
                    specLexemeIncrement: Int,
                    size: Int
                ): Pair<Int, Int>? {
                    val C = initialAutomatonSize - lexemeIncrement - specLexemeIncrement
                    val T = size - C
                    for (lexemeLength in 1..(T / lexemeIncrement)) {
                        val remainder = T - lexemeIncrement * lexemeLength
                        if (remainder % specLexemeIncrement == 0) {
                            val specLexemeLength = remainder / specLexemeIncrement
                            if (specLexemeLength >= 1) {
                                println("Lexeme length: $lexemeLength, SpecLexemeLength: $specLexemeLength")
                                lexemeLengthsResults = Pair(lexemeLength, specLexemeLength)
                                return Pair(lexemeLength, specLexemeLength)
                            }
                        }
                    }
                    lexemeLengthsResults = null
                    return null
                }

                private fun totallyDisjoint(
                    lexemeLength: Int,
                    specLexeme: Lexems,
                    specLexemeLength: Int,
                    isAtomSingleState: Boolean = true
                ): Map<Lexems, Lexems.Config> {
                    val alphabet = (0..9).toMutableSet()
                    val lexMap = mutableMapOf<Lexems, Lexems.Config>()
                    Lexems.entries.forEach { lexeme ->
                        val lexemeAlphabet = alphabet.asSequence().take(2).toSet()
                        alphabet -= lexemeAlphabet
                        val transitions =
                            if (isAtomSingleState && lexeme == Lexems.ATOM) {
                                listOf(lexemeAlphabet.random())
                            } else {
                                List(if (lexeme == specLexeme) specLexemeLength else lexemeLength) { lexemeAlphabet.random() }
                            }

                        lexMap[lexeme] = Lexems.Config(
                            states = transitions.size + 1,
                            transitionsNum = transitions.size,
                            acceptingStates = 1,
                            transitions = transitions
                        )
                    }

                    return lexMap
                }
            }
        }
    }
}

enum class GeneratorMode {
    EASY, NORMAL, HARD, FIXED
}

fun generateRandomSet(size: Int): Set<Int> =
    generateSequence { Random.nextInt(0, 10) }
        .distinct()
        .take(size)
        .toSet()
