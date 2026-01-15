package com.vaultic.eth

import com.vaultic.core.TransactionRecord
import com.vaultic.core.TransactionStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class EthHistoryResponse(
    val status: String,
    val message: String,
    val result: List<EthHistoryItem>
)

@Serializable
data class EthHistoryItem(
    val hash: String,
    val timeStamp: String,
    val confirmations: String,
    val isError: String
)

class EthHistoryClient {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun fetchTransactions(baseUrl: String, address: String): List<TransactionRecord> {
        val trimmed = baseUrl.trimEnd('/')
        if (trimmed.isEmpty()) return emptyList()
        val response: EthHistoryResponse = client.get {
            url("$trimmed/api?module=account&action=txlist&address=$address&sort=desc")
        }.body()
        return response.result.map {
            val confirmations = it.confirmations.toIntOrNull() ?: 0
            val status = when {
                it.isError == "1" -> TransactionStatus.Failed
                confirmations == 0 -> TransactionStatus.Pending
                else -> TransactionStatus.Confirmed
            }
            TransactionRecord(hash = it.hash, status = status, confirmations = confirmations)
        }
    }
}
