package com.vaultic.btc

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class BtcApiClient {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    internal suspend inline fun <reified T> get(url: String): T {
        return client.get { url(url) }.body()
    }

    suspend fun postText(url: String, body: String): String {
        return client.post {
            url(url)
            setBody(body)
        }.body()
    }
}
