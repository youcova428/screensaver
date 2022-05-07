package com.example.screensaver


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*

class SearchViewModel : ViewModel() {

    private val searchApi: SearchApi = SearchApi()
    var msmObjLiveData = MutableLiveData<MuseumObject>()
    var initialMsmObjLiveData = MutableLiveData<MuseumObject>()
    var artOjt = MutableLiveData<ArtOjt>()


    fun searchMuseumObject(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            msmObjLiveData.postValue(searchApi.searchMuseumObject(query))
        }
    }

    fun searchInitialMsmObj(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            initialMsmObjLiveData.postValue(searchApi.searchMuseumObject(query))
        }
    }

    fun searchArtObject(id : String) {
        viewModelScope.launch( Dispatchers.IO) {
            artOjt.postValue(searchApi.searchArt(id))
        }
    }
}