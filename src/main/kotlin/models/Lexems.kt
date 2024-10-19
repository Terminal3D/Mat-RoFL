package org.example.models

enum class Lexems {
    PROGRAM,
    EXPRESSION,
    ATOM,
    LIST,
    LBR,
    RBR,
    EOL,
    DOT;

    data class Config(
        val states: Int,
        val transitions: Int,
        val acceptingStates: Int
    )
}