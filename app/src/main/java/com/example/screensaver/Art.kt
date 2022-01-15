package com.example.screensaver

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

class Art {

    @SerializedName("objectID")
    val objectId: String = ""

    @SerializedName("primaryImage")
    val primaryImage: String = ""

    @SerializedName("primaryImageSmall")
    val primaryImageSmall: String = ""

    @SerializedName("additionalImages")
    val additionalImages = arrayListOf<String>()

    @SerializedName("constituents")
    val constituents = arrayListOf<Constituent>()

    @Serializable
    class Constituent {
        @SerializedName("constituentId")
        val constituentId: String = ""

        @SerializedName("role")
        val role: String = ""

        @SerializedName("constituentULAN_URL")
        val constituentULAN_URL: String = ""

        @SerializedName("constituentWikidata_URL")
        val constituentWikidata_URL : String = ""

        @SerializedName("gender")
        val gender : String = ""
    }

    @SerializedName("department")
    val department : String = ""

    @SerializedName("objectName")
    val objectName : String = ""

    @SerializedName("title")
    val  title :String = ""

}