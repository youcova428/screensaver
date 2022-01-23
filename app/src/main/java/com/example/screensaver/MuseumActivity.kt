package com.example.screensaver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.WorkerThread
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.*

class MuseumActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_museum)

        val objectList =
            intent.getStringArrayListExtra("MuseumObjectIDs")
        val artImageProgress = findViewById<ProgressBar>(R.id.art_image_progress)
        var nowValue = artImageProgress.progress
        artImageProgress.max = 40
        val artImageMutableList = mutableListOf<Art>()

        GlobalScope.launch(Dispatchers.Main) {
            for (id in objectList!!) {
                if (nowValue == artImageProgress.max) {
                    artImageProgress.visibility = View.INVISIBLE
                    setUpRecyclerView(artImageMutableList)
                    return@launch
                }
                val artObject = getAsyncArtRequest(id)
                if (artObject.primaryImage.isNotEmpty()) {
                    artImageMutableList.add(artObject)
                    Log.d("tag", artImageMutableList[nowValue].primaryImage)
                    nowValue += 1
                }
            }
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

    private fun setUpRecyclerView(artList: MutableList<Art>) {
        val artAdapter = ArtAdapter(artList)
        val recyclerview = findViewById<RecyclerView>(R.id.art_recyclerview)
        with(recyclerview) {
            adapter = artAdapter
            layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL).apply {
                    gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
                }
        }
        artAdapter.setOnArtItemClickListener(object : ArtAdapter.OnArtItemClickListener {
            override fun OnArtItemClick(art: Art) {
                Toast.makeText(applicationContext, "${art.title}が入力された。", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}