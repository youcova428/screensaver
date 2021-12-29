package com.example.screensaver

import com.google.gson.annotations.SerializedName

data class JsonImage constructor(
    @SerializedName("uri")
    val uri: String
)


//        @Serializable
//         data class ImageUri(
//            @SerializedName("authority")
//            val authority: Authority?,
//            @SerializedName("fragment")
//            val fragment: Fragment?,
//            @SerializedName("path")
//            val path: Path?,
//            @SerializedName("query")
//            val query: Query?,
//            @SerializedName("scheme")
//            val scheme: String?,
//            @SerializedName("uriString")
//            val uriString: String?,
//            @SerializedName("host")
//            val host: String?,
//            @SerializedName("port")
//            val port: String?,
//        )
//
//
//
//    @Serializable
//    data class Authority(
//        @SerializedName("decoded")
//        val decoded: String,
//        @SerializedName("encoded")
//        val encoded: String
//    )
//
//    @Serializable
//    class Fragment(
//    )
//
//    @Serializable
//    data class Path(
//        @SerializedName("decoded")
//        val decoded: String,
//        @SerializedName("encoded")
//        val encoded: String
//    )
//
//    @Serializable
//    class Query()
