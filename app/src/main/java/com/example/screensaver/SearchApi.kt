package com.example.screensaver

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException

class SearchApi {

    private val museumObjectService: MuseumObjectService = Retrofit.Builder()
        .baseUrl("https://collectionapi.metmuseum.org/public/collection/v1/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(MuseumObjectService::class.java)

    private val artService : ArtService = Retrofit.Builder()
        .baseUrl("https://collectionapi.metmuseum.org/public/collection/v1/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(ArtService::class.java)

    suspend fun searchMuseumObject(query: String): MuseumObject? {
        try {
            val response = museumObjectService.getMuseumObject(query)
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

    suspend fun searchLocalMsmObj(
        location: String,
        query: String,
    ): MuseumObject? {
        try {
            val response = museumObjectService.getDetailLocationSearch(true, location, query)
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

    suspend fun searchArt(id: String): ArtOjt? {
        try {
            val response = artService.getArt(id)
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