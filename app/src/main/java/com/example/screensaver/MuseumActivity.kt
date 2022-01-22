package com.example.screensaver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
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
import kotlin.math.max

class MuseumActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_museum)

        val objectList =
            intent.getStringArrayListExtra("MuseumObjectIDs")
        val artImageProgress = findViewById<ProgressBar>(R.id.art_image_progress)
        var nowValue = artImageProgress.progress
        artImageProgress.max = 100
        val artImageMutableList = mutableListOf<Art>()

        GlobalScope.launch(Dispatchers.Main) {
                for (id in objectList!!) {
                    if (nowValue == artImageProgress.max) {
                        artImageProgress.visibility = View.INVISIBLE
                        break
                    }
                    val artObject = getAsyncArtRequest(id)
                    if (artObject.primaryImage.isNotEmpty()) {
                        artImageMutableList.add(artObject)
                        Log.d("tag", artImageMutableList[nowValue].primaryImage)
                        nowValue += 1
                    }
            }
            val artImageUrl = findViewById<TextView>(R.id.art_image_url)
            artImageUrl.text = artImageMutableList[(1..100).random()].primaryImage
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