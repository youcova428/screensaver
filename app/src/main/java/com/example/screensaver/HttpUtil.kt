package com.example.screensaver

import okhttp3.Request

class HttpUtil {

    fun httpGet(url : String) : String? {
        val request = Request.Builder()
            .url(url)
            .build()

        val response = HttpClient.instance.newCall(request).execute()
        return response.body?.string()
    }
}