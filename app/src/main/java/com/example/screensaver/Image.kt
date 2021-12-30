package com.example.screensaver

import com.google.gson.annotations.SerializedName

class Image  constructor(_uri : String){
    @SerializedName("uriName")
    var uri: String = _uri
}
