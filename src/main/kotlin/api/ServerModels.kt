package api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenerateRequest(
    val mode: String,
    val size: Int? = null
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
data class CheckWordBatchRequest(
    val wordList: List<String>
)

@Serializable
data class CheckWordBatchResponse(
    val responseList: List<Boolean>
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
    val type: Boolean?
)