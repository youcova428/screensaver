package com.example.screensaver

import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
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
import java.time.LocalTime


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
                downloadArtImage(requireContext(), mArtDetail!!.primaryImage, mArtDetail!!.title)
            }

        }
    }

    private fun downloadArtImage(context: Context, artUri: String, title: String) {
        val imageBitmap = BitmapFactory.decodeFile(artUri)
        val fileName = "{$title.jpg}"
        val mimeType = "image/jpeg"
        val directory = Environment.DIRECTORY_PICTURES
        val mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        var imageOutStream: OutputStream?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(MediaStore.Images.Media.RELATIVE_PATH, directory)
            }

            val cr = context.contentResolver
            cr.run {
                val uri = cr?.insert(mediaContentUri, values)
                imageOutStream = openOutputStream(uri!!)
            }
        } else {
            val imagePath = Environment.getExternalStoragePublicDirectory(directory).absolutePath
            val image = File(imagePath, fileName)
            imageOutStream = FileOutputStream(image)
        }

        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream)

        Toast.makeText(context, "{$title}を押下した。", Toast.LENGTH_SHORT).show()
    }

    private fun bitmapInitial(name: String, artUri: String): Bitmap {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        }
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val contentResolver = context?.contentResolver
        val item = contentResolver?.insert(collection, values)!!

        // Bitmap 初期化
        var bitmap: Bitmap? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val source = ImageDecoder.createSource(contentResolver, Uri.parse(artUri))
            //fixme FileNotFoundException
            bitmap = ImageDecoder.decodeBitmap(source)
        }
        return bitmap!!
    }

}