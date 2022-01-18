package com.example.screensaver

import com.google.gson.annotations.SerializedName

class MuseumObject {

    @SerializedName("total")
    val total : String = ""

    @SerializedName("objectIDs")
    val objectIds = arrayListOf<String>()
}