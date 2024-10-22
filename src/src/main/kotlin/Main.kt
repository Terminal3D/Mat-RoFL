import api.module
import generator.AutomatonGenerator
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    for (i in (1..10000)) {
        AutomatonGenerator().create("normal")
    }
    embeddedServer(Netty, port = 8080) {
        module()
    }.start(wait = true)
}