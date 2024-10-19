import dk.brics.automaton.RegExp
import org.example.generator.AutomatonGenerator

fun main() {
    for (i in (1..20000)) {
        AutomatonGenerator().create("easy")
        println(i)
    }
}