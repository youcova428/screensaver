package com.example.screensaver

import android.net.Uri

class Image {

    var uri: Uri? = null
        get() = field
        set(value) {
            field = uri
        }
    constructor(uri: Uri)
}