package org.jetbrains.kotlinconf.api

import org.jetbrains.kotlinconf.data.*
import io.ktor.common.client.*
import io.ktor.common.client.http.*
import kotlinx.serialization.json.*

private val END_POINT = "api.kotlinconf.com"

class KotlinConfApi(val userId: String) {

    suspend fun getAll(): AllData = JSON.parse(AllData.serializer(), get("all").withCheck().body)

    suspend fun postFavorite(favorite: Favorite) {
        post("favorites", JSON.stringify(Favorite.serializer(), favorite)).withCheck()
    }

    suspend fun deleteFavorite(favorite: Favorite): Unit {
        delete("favorites", JSON.stringify(Favorite.serializer(), favorite)).withCheck()
    }

    suspend fun postVote(vote: Vote): Unit {
        post("votes", JSON.stringify(Vote.serializer(), vote)).withCheck()
    }

    suspend fun deleteVote(vote: Vote): Unit {
        delete("votes", JSON.stringify(Vote.serializer(), vote)).withCheck()
    }

    private suspend fun get(path: String): HttpResponse = request(HttpMethod.Get, path, null)
    private suspend fun post(path: String, body: String): HttpResponse = request(HttpMethod.Post, path, body)
    private suspend fun delete(path: String, body: String): HttpResponse = request(HttpMethod.Delete, path, body)

    private suspend fun request(method: HttpMethod, path: String, body: String?) = client.request {
        setupDefault()
        this.method = method
        url.encodedPath = "/$path"
        this.body = body
    }

    private fun HttpRequestBuilder.setupDefault() {
        headers["Accept"] = listOf("application/json")
        headers["Authrization"] = listOf("Bearer $userId")
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
            }

            return (response.statusCode / 100 == 2)
        }
    }
}

private fun HttpResponse.withCheck(): HttpResponse {
    if (!isSuccess()) throw ApiException(this)
    return this
}
private fun HttpResponse.isSuccess() = statusCode / 100 == 2

class ApiException(val response: HttpResponse) : Throwable()
