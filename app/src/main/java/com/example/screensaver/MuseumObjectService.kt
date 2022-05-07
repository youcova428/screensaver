package com.example.screensaver

import com.squareup.moshi.Json
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MuseumObjectService {

    // 検索用のURLを書く
    @GET("search")
    suspend fun getMuseumObject (
        // 検索するためのクエリ
        @Query("q") searchWord: String?
    ): Response<MuseumObject>

    // todo Chipが押下された際の検索
}


data class MuseumObject (
    @Json(name ="total" ) val total : String,
    @Json(name = "objectIDs") val objectIDs : List<String>
)
