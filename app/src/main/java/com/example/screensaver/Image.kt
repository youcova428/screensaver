package com.example.screensaver

import android.net.Uri
import com.google.gson.annotations.SerializedName

class Image constructor(_uri: Uri){

    @SerializedName("uriName")
    var uri: Uri = _uri

}