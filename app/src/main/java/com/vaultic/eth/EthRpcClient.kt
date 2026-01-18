package com.vaultic.eth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject

@Serializable
data class RpcResponse<T>(
    val jsonrpc: String,
    val id: Int,
    val result: T? = null,
    val error: RpcError? = null
)

@Serializable
data class RpcError(
    val code: Int,
    val message: String
)

class EthRpcClient {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    internal suspend inline fun <reified T> call(url: String, method: String, params: List<Any>): T {
        val payload = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", 1)
            put("method", method)
            put("params", JSONArray(params.map { toJsonValue(it) }))
        }
        val response: RpcResponse<T> = client.post {
            url(url)
            header("Content-Type", "application/json")
            setBody(payload.toString())
        }.body()
        if (response.error != null) throw IllegalStateException(response.error.message)
        return response.result ?: throw IllegalStateException("Missing result")
    }

    private fun toJsonValue(value: Any?): Any {
        return when (value) {
            null -> JSONObject.NULL
            is String, is Number, is Boolean -> value
            is Map<*, *> -> JSONObject(
                value.entries.associate { (key, entryValue) ->
                    key.toString() to toJsonValue(entryValue)
                }
            )
            is Iterable<*> -> JSONArray(value.map { toJsonValue(it) })
            else -> value.toString()
        }
    }
}
