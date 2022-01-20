package com.example.screensaver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.WorkerThread
import com.github.kittinunf.fuel.toolbox.HttpClient
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.lang.ref.ReferenceQueue

class MuseumActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_museum)

        val objectList =
            intent.getStringArrayListExtra("MuseumObjectIDs")

        val artImageMutableList = mutableListOf<String>()
        var i = 0
        runBlocking {
            for (id in objectList!!) {
                if (i > 30) {
                    return@runBlocking
                }
                val urlImage = getAsyncArtRequest(id).primaryImage
                if(urlImage.isNotEmpty()){
                    artImageMutableList.add(urlImage)
                    Log.d("tag", artImageMutableList[i])
                    i += 1
                }
            }
        }
    }

    private fun getArtRequest(id: String): Art {
        val request = Request.Builder()
            .url("https://collectionapi.metmuseum.org/public/collection/v1/objects/${id}").build()
        OkHttpClient().newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("$response")
            }
            val responseText: String? = response.body.toString()
            val type = object : TypeToken<Art>() {}.type
            return Gson().fromJson(responseText, type)
        }
    }

    @WorkerThread
    private suspend fun getAsyncArtRequest(id: String): Art {
        return withContext(Dispatchers.Default) {
            val http = HttpUtil()
            val response: String? =
                http.httpGet("https://collectionapi.metmuseum.org/public/collection/v1/objects/${id}")
            val type = object : TypeToken<Art>() {}.type
            Gson().fromJson(response, type)
        }
    }

}