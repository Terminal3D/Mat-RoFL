package api

import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.generator.AutomatonGenerator
import org.example.models.MATAutomaton
import org.slf4j.event.Level
import java.time.Duration
import java.time.Instant


private lateinit var automaton : MATAutomaton

fun Application.module() {

    install(ContentNegotiation) {
        json()
    }

    install(CallLogging) {
        level = Level.INFO
    }

    routing {
        post("/checkWord") {
            try {
                val request = call.receive<CheckWordRequest>()

                val accepted = automaton.automaton.run(request.word)

                val response = CheckWordResponse(
                    response = if (accepted) "1" else "0"
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
                val diff = automaton.automaton.minus(automatonFromTable)
                val accepted = diff.isEmpty
                if (accepted) {
                    println("Guessed:")
                    println(automatonFromTable.toDot())
                }
                val response = CheckTableResponse(
                    response = if (!accepted) diff.getShortestExample(true) else "true"
                )
                val end = Instant.now()
                val duration = Duration.between(start, end)
                val totalMillis = duration.toMillis()
                val seconds = totalMillis / 1000
                val milliseconds = totalMillis % 1000
                println("Время обработки: %d:%03d".format(seconds, milliseconds))
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