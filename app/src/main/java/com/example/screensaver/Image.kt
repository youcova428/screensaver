package com.example.screensaver

import android.net.Uri
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
//fixme コンストラクタの設定
@Serializable
data class Image  constructor(_uri : String){
    var uri: String =_uri
}




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
