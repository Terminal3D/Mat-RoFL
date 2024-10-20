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


private lateinit var automaton : MATAutomaton

fun Application.module() {
//
//    val etalon = Automaton.makeEmpty()
//    val stateB2 = State()
//    val stateA2 = State()
//    val stateBA2 = State()
//
//    etalon.initialState.addTransition(
//        Transition('a', stateA2)
//    )
//
//    etalon.initialState.addTransition(
//        Transition('b', stateB2)
//    )
//
//    stateA2.addTransition(
//        Transition('a', etalon.initialState)
//    )
//
//    stateA2.addTransition(
//        Transition('b', stateBA2)
//    )
//
//    stateB2.addTransition(
//        Transition('a', stateBA2)
//    )
//
//    stateBA2.addTransition(
//        Transition('a', stateB2)
//    )
//
//    stateB2.isAccept = true
//    println(etalon.toDot())
//
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
                    response = if (accepted) 1 else 0
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

                val request = call.receive<CheckTableRequest>()

                val automatonFromTable = request.toAutomaton()
                println(automatonFromTable.toDot())
                val diff = automaton.automaton.minus(automatonFromTable)
//                val accepted = automaton.automaton.minus(automatonFromTable).isEmpty
                val accepted = diff.isEmpty
                val response = CheckTableResponse(
                    response = if (!accepted) diff.getShortestExample(true) else "true"
                )
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
                println(automaton.automaton.toDot())

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