package com.example.screensaver

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import java.util.ArrayList

class MuseumObject {

    @SerializedName("total")
    val total : String = ""

    @SerializedName("objectIDs")
    val objectIds = arrayListOf<String>()
}

data class MuseumObj (
    @Json(name = "objectIds") val objectIds : List<String>
)

