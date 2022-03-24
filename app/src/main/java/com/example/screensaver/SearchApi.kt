package com.example.screensaver

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException

class SearchApi {
    var museumObject: MuseumObject? = null
    val service: MuseumObjectService = Retrofit.Builder()
        .baseUrl("https://collectionapi.metmuseum.org/public/collection/v1/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(MuseumObjectService::class.java)

    suspend fun searchMuseumObject(query: String): List<MuseumObject>? {
        try {
            val response = service.getMuseumObject(query)
            if (response.isSuccessful) {
                return response.body()
            } else {
                Log.d("tag", "GET ERROR")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}