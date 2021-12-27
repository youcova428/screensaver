package com.example.screensaver

import android.net.Uri
import com.google.gson.annotations.SerializedName

class Image {

    @SerializedName("uriName")
    var uri: Uri? = null
        get() = field
        set(value) {
            field = uri
        }

    constructor(uri: Uri)
}