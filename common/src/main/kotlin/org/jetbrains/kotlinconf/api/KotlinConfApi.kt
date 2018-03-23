package org.jetbrains.kotlinconf.api

import org.jetbrains.kotlinconf.data.*
import io.ktor.common.client.*
import io.ktor.common.client.http.*
import kotlinx.serialization.json.*

private val END_POINT = "api.kotlinconf.com"
private typealias Config = HttpRequestBuilder.() -> Unit

class KotlinConfApi(val userId: String) {

    suspend fun getAll(): AllData =
            JSON.parse(AllData.serializer(), get("all", {}).withCheck().body)

    suspend fun postFavorite(favorite: Favorite) {
        post("favorites", JSON.stringify(Favorite.serializer(), favorite), {}).withCheck()
    }

    suspend fun deleteFavorite(favorite: Favorite) {
        delete("favorites", JSON.stringify(Favorite.serializer(), favorite), {}).withCheck()
    }

    suspend fun postVote(vote: Vote) {
        post("votes", JSON.stringify(Vote.serializer(), vote), {}).withCheck()
    }

    suspend fun deleteVote(vote: Vote) {
        delete("votes", JSON.stringify(Vote.serializer(), vote), {}).withCheck()
    }

    private suspend fun get(path: String, block: Config): HttpResponse =
            request(HttpMethod.Get, path, null) {
                headers["Accept"] = listOf("application/json")
                block()
            }

    private suspend fun post(path: String, body: String, block: Config): HttpResponse =
            request(HttpMethod.Post, path, body) {
                headers["ContentType"] = listOf("application/json")
                block()
            }

    private suspend fun delete(path: String, body: String, block: Config): HttpResponse =
            request(HttpMethod.Delete, path, body) {
                headers["ContentType"] = listOf("application/json")
                block()
            }

    private suspend fun request(
            method: HttpMethod, path: String, body: String?, block: Config
    ) = client.request {
        setupDefault()
        this.method = method
        url.encodedPath = "/$path"
        this.body = body
        block()
    }

    private fun HttpRequestBuilder.setupDefault() {
        headers["Authorization"] = listOf("Bearer $userId")
        url.protocol = URLProtocol.HTTPS
        url.host = END_POINT
        url.port = 443
    }

    companion object {
        private val client = HttpClient()

        suspend fun createUser(userId: String): Boolean {
            val response = client.request {
                method = HttpMethod.Post
                url.protocol = URLProtocol.HTTPS
                url.host = END_POINT
                url.port = 443
                url.encodedPath = "/users"
                body = userId
            }

            return response.isSuccess()
        }
    }
}

private fun HttpResponse.withCheck(): HttpResponse {
    if (!isSuccess()) throw ApiException(this)
    return this
}

private fun HttpResponse.isSuccess() = statusCode / 100 == 2

class ApiException(val response: HttpResponse) : Throwable()
