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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.nio.file.Files.size

class MainActivity : FragmentActivity(R.layout.activity_main) {

    var mImageView: ImageView? = null
    var mUriList: MutableList<Uri>? = null
    lateinit var itemAdapter: UriAdapter
    var exampleList: MutableList<Uri>? = null
    private lateinit var sharedPreferences: SharedPreferences
    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recyclerview_list) }

    companion object {
        var image: Bitmap? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mUriList = mutableListOf()
        sharedPreferences = getSharedPreferences("ScreenSaver", Context.MODE_PRIVATE)

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

//        val exampleUri = Uri.parse("android.resource://${packageName}/drawable/pict_mvis")
//        exampleList = mutableListOf(exampleUri)
//        setUpRecyclerView(exampleList!!.toTypedArray())
        Log.d("tag", "onCreate")

//        val uriSet = sharedPreferences.getStringSet("uriListSet", mutableSetOf<String>())
//        val uriArray = uriSet!!.toTypedArray()
//        var uriMutableArray = mutableListOf<Uri>()
//        for (uriString in uriArray) {
//            uriMutableArray.add(Uri.parse(uriString))
//        }
        exampleList = getUriArray("uri_collections",sharedPreferences)
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
            if (uriList != null && uriList.size != 0) {
                if (exampleList!!.isNotEmpty()) {
                    //TODO 既存リストに無ければ追加する　既存リストをSharedPreferences かdatabaseかにする
                    val addUriSet = createAddUriList(exampleList!!, uriList)
                    if (addUriSet != (exampleList)) {
                        itemAdapter.updateItem(addUriSet.toTypedArray())
                        itemAdapter.notifyDataSetChanged()
                    }
//                    var uriString: String
//                    var uriStringSet = mutableSetOf<String>()
//
//                    for (uri in addUriList) {
//                        uriString = uri.toString()
//                        uriStringSet.add(uriString)
//                    }
//                    sharedPreferences.edit().putStringSet("uriListSet", uriStringSet).apply()
                        saveUriSet(addUriSet, "uri_collections", sharedPreferences)
                } else {
                    if (recyclerView.layoutManager != null) {
                        //uriListが場合
                        val addUriSet = createAddUriList(exampleList!!, uriList)
                        if (addUriSet != exampleList) {
                            itemAdapter.updateItem(addUriSet.toTypedArray())
                            itemAdapter.notifyDataSetChanged()
                        }
                        //sharedPreference追加する
//                        var uriString: String
//                        var uriStringSet = mutableSetOf<String>()
//
//                        for (uri in addUriList) {
//                            uriString = uri.toString()
//                            uriStringSet.add(uriString)
//                        }
//                        sharedPreferences.edit().putStringSet("uriListSet", uriStringSet).apply()
                        saveUriSet(addUriSet, "uri_collections", sharedPreferences)
                    } else {
                        //RecyclerViewが設置されていないが場合(初回), SharedPreferencesからUriSetを取得する
                        setUpRecyclerView(uriList.toTypedArray())
                        if (!uriList.isNullOrEmpty()) {
                            itemAdapter.updateItem(uriList.toTypedArray())
                            itemAdapter.notifyDataSetChanged()
                        }

//                        var uriString: String
//                        var uriStringSet = mutableSetOf<String>()
//
//                        for (uri in uriList) {
//                            uriString = uri.toString()
//                            uriStringSet.add(uriString)
//                        }
//                        sharedPreferences.edit().putStringSet("uriListSet", uriStringSet).apply()
                        saveUriSet(uriList, "uri_collections", sharedPreferences)
                    }
                }
            } else {
                mImageView!!.setImageResource(R.drawable.pict_mvis)
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
        itemAdapter = UriAdapter(exampleList!!)
        with(recyclerView) {
            adapter = itemAdapter
            //fixme layout修正　とりあえずStaggeredGridLayoutを実装した感じ　もっときれいにしたい
            layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL).apply {
                    gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
                }
        }
        //ItemClickListener実装
        itemAdapter.setOnImageItemClickListener(object : UriAdapter.OnImageItemClickListener {
            override fun OnItemClick(uri: Uri) {
                Log.d("tag", "画像クリック ${uri.toString()}")
                val inputStream = contentResolver?.openInputStream(uri)
                image = BitmapFactory.decodeStream(inputStream)
            }
        })

        //ItemLongClickListener実装
        itemAdapter.setImageItemLongClickListener(object : UriAdapter.OnImageItemLongClickListener {
            override fun OnItemLongClick(position: Int) {
                itemAdapter.removeItem(position)
                itemAdapter.notifyItemRemoved(position)
            }
        })
    }

    private fun createAddUriList(
        exitingList: MutableList<Uri>,
        uriList: MutableList<Uri>
    ): Set<Uri> {
        val addUriList = exitingList!!.union(uriList)
        for (addListUri in addUriList) {
            Log.d("tag", "画像追加 ${addListUri.toString()}")
        }
        return addUriList
//        if (!addUriList.equals(exitingList)) {
//            itemAdapter.updateItem(addUriList.toTypedArray())
//            itemAdapter.notifyDataSetChanged()
//        }
    }

    fun saveUriSet(uriSet: Set<Uri>, key: String, sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(uriSet)
        editor.putString(key, json)
        editor.apply()
    }


    fun saveUriSet(uriList: List<Uri>, key: String, sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(uriList)
        editor.putString(key, json)
        editor.apply()
    }


    fun getUriArray(key: String, sharedPreferences: SharedPreferences): MutableList<Uri> {
        val gson = Gson()
        val emptyList = Gson().toJson(mutableListOf<Uri>())
        val json = sharedPreferences.getString(key, emptyList)
        val type = object : TypeToken<Array<Uri>>() {}.type
        return gson.fromJson(json,type)
    }
}