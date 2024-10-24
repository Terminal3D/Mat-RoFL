package api

import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import generator.AutomatonGenerator
import org.example.models.MATAutomaton
import org.slf4j.event.Level
import java.time.Duration
import java.time.Instant


private lateinit var automaton: MATAutomaton

fun Application.module() {

    install(ContentNegotiation) {
        json()
    }

    install(CallLogging) {
        level = Level.INFO
    }

    var checkTableCounter = 0
    var checkWord = 0

    routing {
        post("/checkWord") {
            try {
                val request = call.receive<CheckWordRequest>()

                val accepted = automaton.automaton.run(request.word)

                val response = CheckWordResponse(
                    response = if (accepted) "1" else "0"
                )
                checkWord++
                call.respond(response)
            } catch (e: Exception) {
                call.respond(
                    status = BadRequest,
                    message = mapOf("error" to (e.message ?: "Unknown error"))
                )
            }
        }

        post("/check-word-batch") {
            try {
                val request = call.receive<CheckWordBatchRequest>()

                val response = CheckWordBatchResponse(
                    responseList = request.wordList.map { automaton.automaton.run(it) }
                )
                call.respond(response)
            } catch (e: Exception) {
                call.respond(
                    status = BadRequest,
                    message = mapOf("error" to (e.message ?: "Unknown error"))
                )
            }
        }

        post("/checkTable") {
            try {
                val start = Instant.now()
                val request = call.receive<CheckTableRequest>()

                val automatonFromTable = request.toAutomaton()

                val diff1 = automaton.automaton.minus(automatonFromTable)

                val response: CheckTableResponse

                if (!diff1.isEmpty) {
                    val counterExample = diff1.getExample(automaton.config.mode)
                    response = CheckTableResponse(
                        response = counterExample,
                        type = true
                    )
                } else {
                    val diff2 = automatonFromTable.minus(automaton.automaton)

                    if (!diff2.isEmpty) {
                        val counterExample = diff2.getExample(automaton.config.mode)
                        response = CheckTableResponse(
                            response = counterExample,
                            type = false
                        )
                    } else {
                        println("Guessed:")
                        println(automatonFromTable.toDot())
                        println("Table:$checkTableCounter, word:$checkWord")
                        response = CheckTableResponse(
                            response = "true",
                            type = null
                        )
                    }
                }
                val end = Instant.now()
                val duration = Duration.between(start, end)
                val totalMillis = duration.toMillis()
                val seconds = totalMillis / 1000
                val milliseconds = totalMillis % 1000
                println("Время обработки: %d:%03d".format(seconds, milliseconds))
                checkTableCounter++
                call.respond(response)
            } catch (e: Exception) {
                call.respond(
                    status = BadRequest,
                    message = mapOf("error" to (e.message ?: "Unknown error"))
                )
            }
        }

        post("/generate") {
            try {
                val request = call.receive<GenerateRequest>()

                automaton = AutomatonGenerator().create(request.mode)

                val response = GenerateResponse(
                    maxBracketNesting = automaton.config.maxParentheses,
                    maxLexemeSize = automaton.config.maxLexemLength
                )
                call.respond(response)
            } catch (e: Exception) {
                call.respond(
                    status = BadRequest,
                    message = mapOf("error" to (e.message ?: "Unknown error"))
                )
            }
        }
    }
}