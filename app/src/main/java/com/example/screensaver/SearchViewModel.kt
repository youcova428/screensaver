package com.example.screensaver


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.screensaver.MuseumObjectService.MsmObjResponse
import kotlinx.coroutines.*

class SearchViewModel : ViewModel() {

    private val searchApi: SearchApi = SearchApi()
    var msmObjLiveData = MutableLiveData<MsmObjResponse>()
    var initialMsmObjLiveData =  MutableLiveData<MsmObjResponse>()

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
}