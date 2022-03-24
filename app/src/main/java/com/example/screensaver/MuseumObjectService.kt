package com.example.screensaver

import android.telecom.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MuseumObjectService {
    // 検索用のURLを書く
    @GET("search")
    suspend fun getMuseumObject (
        // 検索するためのクエリ
        @Query("q") searchWord : String?
    ): Response<List<MuseumObject>>
}