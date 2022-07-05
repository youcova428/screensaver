package com.example.screensaver

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.screensaver.databinding.FragmentArtDetailBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL


/**
 * 美術品詳細画面
 */
@DelicateCoroutinesApi
class ArtDetailFragment : Fragment() {

    private lateinit var binding: FragmentArtDetailBinding
    private var mArtDetail: ArtOjt? = null
    private val mViewModel : SearchViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_art_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentArtDetailBinding.bind(view)

        //ArtListFragmentからid受け取り
        val id = arguments?.get("ArtId") as String

        mViewModel.searchArtObject(id)
        mViewModel.artOjt.observe(viewLifecycleOwner) {
            mArtDetail = it
            Glide.with(this@ArtDetailFragment)
                .load(it.primaryImage)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding.detailImage)
        }

        binding.downloadButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                downloadArtImage(requireContext(), mArtDetail!!.primaryImage, mArtDetail!!.title)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("tag", "onDestroy start")
        val artArrayList = arguments?.get("ArtList") as ArrayList<Art>
        // ScreenBackした際にartList(前画面で表示させたartList)を渡す
        mViewModel.backToArtListFragment(artArrayList)
    }

    private fun downloadArtImage(context: Context, artUri: String, title: String) {
        val connection = URL(artUri).openConnection().apply {
            doInput = true
            connectTimeout = 5000
            readTimeout = 30000
            useCaches = true
        }
        val imageInputStream : InputStream = connection.getInputStream()
        val imageBitmap: Bitmap
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
                put(MediaStore.MediaColumns.RELATIVE_PATH , "DCIM/" + "MetropolitanMuseum ")
            }
            val cr = context.contentResolver
            cr.run {
                val uri = cr?.insert(mediaContentUri, values)
                imageOutStream = openOutputStream(uri!!)
            }
            imageBitmap = BitmapFactory.decodeStream(imageInputStream)
            imageInputStream.close()

        } else {
            val imagePath = Environment.getExternalStoragePublicDirectory(directory).absolutePath
            val image = File(imagePath, fileName)
            imageOutStream = FileOutputStream(image)
            imageBitmap =  MediaStore.Images.Media.getBitmap(context.contentResolver, Uri.parse(artUri))
        }

        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream)
        imageOutStream?.flush()
        imageOutStream?.close()

        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "$title をダウンロードした。", Toast.LENGTH_SHORT).show()
        }
    }
}