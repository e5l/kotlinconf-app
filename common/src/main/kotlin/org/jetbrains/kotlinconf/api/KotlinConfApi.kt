package org.jetbrains.kotlinconf.api

import org.jetbrains.kotlinconf.data.*
import io.ktor.common.client.*
import io.ktor.common.client.http.*

private val END_POINT = "api.kotlinconf.com"

class KotlinConfApi(val userId: String) {

    /*
    @POST("users")
    fun postUserId(@Body uuid: RequestBody): Call<ResponseBody>

    @POST("favorites")
    fun postFavorite(@Body favorite: Favorite): Call<ResponseBody>

    @HTTP(method = "DELETE", path = "favorites", hasBody = true)
    fun deleteFavorite(@Body favorite: Favorite): Call<ResponseBody>

    @POST("votes")
    fun postVote(@Body vote: Vote): Call<ResponseBody>

    @HTTP(method = "DELETE", path = "votes", hasBody = true)
    fun deleteVote(@Body vote: Vote): Call<ResponseBody>
     */


    suspend fun getAll(): AllData {
        val get = get("all")
        println(get.body)
        throw ApiException(get)
    }

    suspend fun postFavorite(favorite: Favorite): Unit {
    }
    suspend fun deleteFavorite(favorite: Favorite): Unit {

    }

    suspend fun postVote(vote: Vote): Unit {
    }

    suspend fun deleteVote(vote: Vote): Unit {
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

class ApiException(val response: HttpResponse) : Throwable()
