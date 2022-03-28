package com.example.screensaver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Query

class SearchViewModel : ViewModel(){

    var museumObjects : MuseumObj? = null
    private val searchApi: SearchApi = SearchApi()

    fun searchMuseumObject(query: String) : MuseumObj{
        viewModelScope.launch(Dispatchers.IO) {
            museumObjects = searchApi.searchMuseumObject(query)
        }
        return museumObjects!!
    }
}