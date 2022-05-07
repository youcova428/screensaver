package com.example.screensaver

import com.squareup.moshi.Json
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MuseumObjectService {

    // 条件なし検索
    @GET("search")
    suspend fun getMuseumObject (
        // 検索するためのクエリ
        @Query("q") searchWord: String?
    ): Response<MuseumObject>

    // 検索条件　画像付き　地域
    @GET("search")
    suspend fun getDetailLocationSearch(
        @Query("hasImages") hasImages : Boolean = true,
        @Query("geoLocation") geoLocation: String,
        @Query("q") searchWord: String
    ): Response<MuseumObject>
}


data class MuseumObject (
    @Json(name ="total" ) val total : String,
    @Json(name = "objectIDs") val objectIDs : List<String>
)
