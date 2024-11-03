package models

enum class Lexems {
    ATOM,
    LBR,
    RBR,
    EOL,
    DOT;

    data class Config(
        val states: Int,
        val transitionsNum: Int? = null,
        val acceptingStates: Int? = null,
        val transitions: List<Int> = emptyList()
    )
}