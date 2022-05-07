package com.example.screensaver


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    fun searchLocationMsmObj(location: String, query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            msmObjLiveData.postValue(searchApi.searchLocalMsmObj(location, query))
        }
    }

    fun searchMediumMsmObj(medium: String, query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            msmObjLiveData.postValue(searchApi.searchMediumMsmObj(medium, query))
        }
    }

    fun searchArtObject(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            artOjt.postValue(searchApi.searchArt(id))
        }
    }
}