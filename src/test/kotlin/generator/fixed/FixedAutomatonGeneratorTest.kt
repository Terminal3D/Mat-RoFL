package generator.fixed

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.DefaultAsserter.assertNotNull
import kotlin.test.DefaultAsserter.fail

class FixedAutomatonGeneratorTest {

    private lateinit var generator: FixedAutomatonGenerator

    @BeforeEach
    fun setUp() {
        generator = FixedAutomatonGenerator()
    }

    @Test
    fun testAutomatonGenerationRange() {
        val sizesToTest = generateSequence(4) { it + 1 }.takeWhile { it <= 1000 }

        sizesToTest.forEach { size ->
            try {
                val automaton = generator.create(size)
                assertNotNull("Автомат не должен быть null для размера $size", automaton)
                assertEquals(
                    "Количество состояний должно соответствовать запрошенному размеру $size",
                    size,
                    automaton.automaton.numberOfStates
                )
            } catch (e: Exception) {
                fail("Ошибка при генерации автомата размера $size: ${e.message}")
            }
        }
    }
}
