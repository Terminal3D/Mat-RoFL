import api.module
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.example.generator.AutomatonGenerator

fun main() {
    embeddedServer(Netty, port = 8080) {
        module()
    }.start(wait = true)
}