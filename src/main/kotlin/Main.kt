import api.module
import dk.brics.automaton.Automaton
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.example.generator.AutomatonGenerator

fun main() {
    AutomatonGenerator().create("easy")
    embeddedServer(Netty, port = 8080) {
        module()
    }.start(wait = true)
}