package com.example.screensaver

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL


/**
 * 美術品詳細画面
 */
@DelicateCoroutinesApi
class ArtDetailFragment : Fragment() {

    private var mImageView: ImageView? = null
    private var mDownloadButton: Button? = null
    private var mArtDetail: Art? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_art_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mImageView = view.findViewById(R.id.art_detail_image)
        mDownloadButton = view.findViewById(R.id.art_detail_download_button)

        //ArtListFragmentからid受け取り
        val id = arguments?.get("ArtId")

        val handler = Handler(Looper.getMainLooper())
        val request = Request.Builder()
            .url("https://collectionapi.metmuseum.org/public/collection/v1/objects/${id}").build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val responseText: String? = response.body?.string()
                handler.post {
                    println(responseText)
                    val type = object : TypeToken<Art>() {}.type
                    mArtDetail = Gson().fromJson(responseText, type)
                    Glide.with(this@ArtDetailFragment).load(mArtDetail!!.primaryImage)
                        .into(view.findViewById(R.id.art_detail_image))
                }
            }
        })

        mDownloadButton!!.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                downloadArtImage(view.context, mArtDetail!!.primaryImage)
            }

        }
    }

    //todo ダウンロードbitmapファイルを内部ストレージ保存できたっぽいけど、中身自体を確認できない。
    private suspend fun downloadArtImage(context: Context, artUri: String) {
//        val bitmap = Glide.with(context).asBitmap().load(artUri).submit().get()
//        val directory = ContextWrapper(context).getDir(
//            "image",
//            Context.MODE_PRIVATE
//        )
//        val file = File(directory, "file_name")
//        FileOutputStream(file).use { stream ->
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
//        }
        val url = URL(artUri)
        val connection = url.openConnection() as HttpURLConnection
        connection.allowUserInteraction = false
        connection.requestMethod = "GET"
        connection.connect()

        val statusCode = connection.responseCode
        if (statusCode != HttpURLConnection.HTTP_OK) {
            throw Exception("HTTP Status {$statusCode}")
        }
        Log.d("tag", "Content-Type:{$statusCode}")

        val dataInStream = DataInputStream(connection.inputStream)
        val dataOutputStream = DataOutputStream(
            BufferedOutputStream(
                // todo downloadファイルのpathがわからない
                FileOutputStream("/storage/emulated/0/Download")
            )
        )
        val buffer = ByteArray(4096)
        var readByte : Int = 0

        readByte = dataInStream.read(buffer)
        while ( readByte != -1 ) {
            dataOutputStream.write(buffer,0,readByte)
        }
        dataInStream.close()
        dataOutputStream.close()
    }

}