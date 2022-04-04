package com.example.screensaver

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Query

class SearchViewModel : ViewModel(){

    private lateinit var museumObjects : MuseumObj
    private val searchApi: SearchApi = SearchApi()

    fun searchMuseumObject(query: String) : MuseumObj? {
//       return withContext(Dispatchers.Default) {
//           searchApi.searchMuseumObject(query)
//        }
        // fixme museumObjects 戻り値がnullになる
         museumObjects = runBlocking {
             searchApi.searchMuseumObject(query)!!
        }
        return museumObjects
    }
}