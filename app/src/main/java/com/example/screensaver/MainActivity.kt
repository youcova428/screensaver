package com.example.screensaver

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class MainActivity : FragmentActivity(R.layout.activity_main) {

    var imageView: ImageView? = null

    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recyclerview_list) }

    companion object {
        var image: Bitmap? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        imageView = findViewById<ImageView>(R.id.image_view)
        //デフォルトでとんかつの画像が挿入される
//        imageView!!.setImageResource(R.drawable.pict_mvis)

        val sharedPreferences: SharedPreferences =
            getSharedPreferences("SharedPreference", Context.MODE_PRIVATE)
        val switchInteractive = findViewById<Switch>(R.id.switch_service_interactive)
        switchInteractive.isChecked = sharedPreferences.getBoolean("isInteractive", true)
        switchInteractive.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().apply() {
                putBoolean("isInteractive", isChecked)
                apply()
            }
        }

        val switchFullscreen = findViewById<Switch>(R.id.switch_service_fullscreen)
        switchFullscreen.isChecked = sharedPreferences.getBoolean("isFullScreen", true)
        switchFullscreen.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().apply() {
                putBoolean("isFullScreen", isChecked)
                apply()
            }
        }

        val switchScreenBright = findViewById<Switch>(R.id.switch_service_screenbright)
        switchFullscreen.isChecked = sharedPreferences.getBoolean("isScreenBright", true)
        switchScreenBright.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().apply() {
                putBoolean("isScreenBright", isChecked)
                apply()
            }
        }

        val photoButton = findViewById<Button>(R.id.photo_button)
        photoButton.setOnClickListener {
            multiSelectPhoto()
        }

        val dreamServiceButton = findViewById<Button>(R.id.service_dream_start_button)
        dreamServiceButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_DREAM_SETTINGS)
            startActivity(intent)
        }

    }

    /**
     * selectPhoto()の結果を受け取りImageViewに挿入する
     */
    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("registerForActivityRegister(result)", result.toString())
        if (result.resultCode != RESULT_OK) {
            return@registerForActivityResult
        } else {
            try {
                result.data?.data?.also { uri: Uri ->
//                    val inputStream = contentResolver?.openInputStream(uri)
//                    image = BitmapFactory.decodeStream(inputStream)
//                    imageView!!.setImageBitmap(image)
                }
            } catch (e: Exception) {
                Toast.makeText(this, "エラーが発生しました", Toast.LENGTH_LONG).show()
            }
        }
    }


    private val multiActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uriList ->
            if (uriList != null) {
                for (imageUri in uriList) {
                    Log.d("tag", "画像選択 ${imageUri.toString()}")
                }
                if (uriList.size != 0) {
                    setUpRecyclerView(uriList.toTypedArray());
                } else {
                    imageView!!.setImageResource(R.drawable.pict_mvis)
                }
            }
        }

    /**
     *画像フォルダから1枚写真を選択する
     */
    private fun singleSelectPhoto() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        activityResultLauncher.launch(intent)
    }

    /**
     *画像フォルダから複数枚写真を選択する
     */
    private fun multiSelectPhoto() {
        multiActivityResultLauncher.launch("image/*")
    }

    private fun setUpRecyclerView(uriList: Array<Uri>) {
        var itemAdapter = UriAdapter(uriList)
        with(recyclerView) {
            adapter = itemAdapter
            //fixme layout修正　とりあえずStaggeredGridLayoutを実装した感じ
            layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL).apply {
                    gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
                }
        }
        //ItemClickListener実装
        itemAdapter.setOnImageItemClicklistener(object : UriAdapter.OnImageItemClickListener {
            override fun OnItemClick(uri: Uri) {
                Log.d("tag", "画像クリック ${uri.toString()}")
                val inputStream = contentResolver?.openInputStream(uri)
                image = BitmapFactory.decodeStream(inputStream)
            }
        })
    }


}