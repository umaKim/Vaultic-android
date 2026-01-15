package com.vaultic.eth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class RpcRequest(
    val jsonrpc: String = "2.0",
    val id: Int = 1,
    val method: String,
    val params: List<@kotlinx.serialization.Contextual Any>
)

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
        val response: RpcResponse<T> = client.post {
            url(url)
            setBody(RpcRequest(method = method, params = params))
        }.body()
        if (response.error != null) throw IllegalStateException(response.error.message)
        return response.result ?: throw IllegalStateException("Missing result")
    }
}
