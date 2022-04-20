package com.example.screensaver

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import kotlinx.serialization.Serializable
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.Path

interface ArtService {

    @GET("objects/{objectID}")
    suspend fun getArt(
       @Path("objectID") objectId: String
    ): Response<ArtOjt>

}

data class ArtOjt(
    @Json(name = "objectID") val objectId: String,
    @Json(name = "primaryImage") val primaryImage: String,
    @Json(name = "primaryImageSmall") val primaryImageSmall: String,
    @Json(name = "additionalImages") val additionalImages: List<String>,
    @Json(name = "constituents") val constituents: List<Constituent>,
    @Json(name = "constituent") val constituent: Constituent,
    @Json(name = "department") val department: String,
    @Json(name = "objectName") val objectName: String,
    @Json(name = "title") val title: String,
)

data class Constituent(
    @Json(name = "constituentId") val constituentId: String,
    @Json(name = "role") val role: String,
    @Json(name = "constituentULAN_URL") val constituentULAN_URL: String,
    @Json(name = "constituentWikidata_URL") val constituentWikidata_URL: String,
    @Json(name = "gender") val gender: String,
)