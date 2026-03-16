package me.logancamp.smartVillagers

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object LlmClient {

    private val httpClient = HttpClient.newHttpClient()

    fun sendPrompt(prompt: String): LlmVillagerResponse? {
        return try {
            val jsonBody = """
                {
                  "prompt": ${escapeJson(prompt)}
                }
            """.trimIndent()

            val request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:8000/villager"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() != 200) {
                return null
            }

            parseResponse(response.body())
        } catch (e: Exception) {
            null
        }
    }

    private fun parseResponse(json: String): LlmVillagerResponse? {
        val replyRegex = """"reply"\s*:\s*"([^"]*)"""".toRegex()
        val actionRegex = """"type"\s*:\s*"([^"]*)"""".toRegex()
        val amountRegex = """"amount"\s*:\s*(-?\d+)""".toRegex()

        val replyMatch = replyRegex.find(json) ?: return null
        val reply = replyMatch.groupValues[1]

        val actionType = actionRegex.find(json)?.groupValues?.get(1)
        val amount = amountRegex.find(json)?.groupValues?.get(1)?.toIntOrNull()

        val actions = if (actionType != null) {
            listOf(LlmAction(type = actionType, amount = amount))
        } else {
            emptyList()
        }

        return LlmVillagerResponse(
            reply = reply,
            actions = actions
        )
    }

    private fun escapeJson(text: String): String {
        return buildString {
            append('"')
            for (c in text) {
                when (c) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(c)
                }
            }
            append('"')
        }
    }
}