package api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenerateRequest(
    val mode: String
)

@Serializable
data class GenerateResponse(
    val maxLexemeSize: Int,
    val maxBracketNesting: Int,
)
@Serializable
data class CheckWordRequest(
    val word: String
)

@Serializable
data class CheckWordResponse(
    val response: String
)

@Serializable
data class CheckTableRequest(
    @SerialName("main_prefixes")
    val mainPrefixes : String,
    @SerialName("non_main_prefixes")
    val nonMainPrefixes : String,
    val suffixes: String,
    val table: String
)

@Serializable
data class CheckTableResponse(
    val response: String,
    val type: String?
)