package org.jetbrains.kotlinconf.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.*
import kotlinx.serialization.json.*
import okhttp3.*
import org.jetbrains.kotlinconf.data.*
import retrofit2.*
import retrofit2.Call
import retrofit2.http.*

interface KotlinConfApi {
    @GET("all")
    fun getAll(): Call<AllData>

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

    companion object {
        const val END_POINT = "https://api.kotlinconf.com"

        const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

        fun create(userId: String): KotlinConfApi {

            val client = OkHttpClient.Builder().addInterceptor { chain ->
                val newRequest = chain.request()
                    .newBuilder()
                    .addHeader("Authorization", "Bearer $userId")
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(newRequest)
            }.build()

            val contentType = MediaType.parse("application/json")!!
            val json = JSON(nonstrict = true)

            val retrofit = Retrofit.Builder()
                .client(client)
                .baseUrl(KotlinConfApi.END_POINT)
                .addConverterFactory(stringBased(contentType, json::parse, json::stringify))
                .build()

            return retrofit.create(KotlinConfApi::class.java)
        }
    }
}
