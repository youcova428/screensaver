package com.example.screensaver

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException


/**
 * 美術品詳細画面
 */
class ArtDetailFragment : Fragment() {

    private var mImageView: ImageView? = null
    private var mDownloadButton: Button? = null

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
                    val artDetail: Art = Gson().fromJson(responseText, type)
                    Glide.with(this@ArtDetailFragment).load(artDetail.primaryImage)
                        .into(view.findViewById(R.id.art_detail_image))
                }
            }
        })

        mDownloadButton!!.setOnClickListener {
            //todo ダウンロードメソッドを追加する

        }
    }

}