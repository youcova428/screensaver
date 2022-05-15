package com.example.screensaver

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
class Art(parcel: Parcel) : Parcelable {

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


    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Parcelable.Creator<Art> {
        override fun createFromParcel(source: Parcel?): Art {
           return Art(source!!)
        }

        override fun newArray(size: Int): Array<Art?> {
            return arrayOfNulls(size)
        }
    }
}