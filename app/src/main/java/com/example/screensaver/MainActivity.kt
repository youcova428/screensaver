package com.example.screensaver

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.os.Build.VERSION.SDK_INT
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class MainActivity : FragmentActivity(R.layout.activity_main) {

    var mImageView: ImageView? = null
    var mUriList: MutableList<Image>? = null
    lateinit var itemAdapter: UriAdapter
    var saveMutableList: MutableList<Image>? = null
    private lateinit var sharedPreferences: SharedPreferences
    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recyclerview_list) }

    companion object {
        var image: Bitmap? = null
        const val URI_COLLECTION = "uri_collection_2"
        const val OPEN_DOCUMENT_PERMISSION = "open_document_permission"
        const val PERMISSIONS_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Permission
        if (SDK_INT >= Build.VERSION_CODES.P) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                File(externalStoragePath()).listFiles().map { file ->
                    println(file.path)
                }
            } else {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            }
        }

        mUriList = mutableListOf()
        sharedPreferences = getSharedPreferences("ScreenSaver", MODE_PRIVATE)

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


        saveMutableList = getUriArray(URI_COLLECTION, sharedPreferences)
        if (!saveMutableList.isNullOrEmpty() && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//            itemAdapter = UriAdapter(imageListConvert(saveMutableList!!))
            //fixme SecurityException で落ちる　ファイルを開ける権限がないっぽい？

//            if (SDK_INT >= Build.VERSION_CODES.P) {
//                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).also {
//                    it.addCategory(Intent.CATEGORY_OPENABLE)
//                    it.type = "*/*"type
//                    it.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
//                    it.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                }
//                startForResult.launch(intent)
//            }
            setUpRecyclerView(imageListConvert(saveMutableList!!))
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
            if (uriList != null && uriList.size != 0) {
                //SharedPrefenecesからUriを取得できたとき
                if (saveMutableList!!.isNotEmpty() && saveMutableList != null) {
                    val addUriSet =
                        createAddUriList(imageListConvert(saveMutableList!!), uriList)
                    if (addUriSet != imageListConvert(saveMutableList!!).toMutableSet()) {
                        itemAdapter.updateItem(addUriSet.toTypedArray())
                        itemAdapter.notifyDataSetChanged()
                    }
                    //Set<Uri> -> Set<Image>へ変換する。
                    val convertImageSet = addUriSet.map { Image(it.toString()) }.toSet()
                    saveUriSet(convertImageSet, URI_COLLECTION, sharedPreferences)
                } else {
                    //1度立ち上げたが、SharedPrefenecesに保存されていない場合
                    if (recyclerView.layoutManager != null) {
                        //uriListが場合
                        val addUriSet =
                            createAddUriList(imageListConvert(saveMutableList!!), uriList)
                        if (addUriSet != saveMutableList) {
                            itemAdapter.updateItem(addUriSet.toTypedArray())
                            itemAdapter.notifyDataSetChanged()
                        }
                        val convertImageSet = addUriSet.map { Image(it.toString()) }.toSet()
                        //sharedPreference追加する
                        saveUriSet(
                            convertImageSet,
                            URI_COLLECTION,
                            sharedPreferences
                        )
                    } else {
                        //RecyclerViewが設置されていないが場合(初回), SharedPreferencesからUriSetを取得する
                        setUpRecyclerView(uriList)
                        if (!uriList.isNullOrEmpty()) {
                            itemAdapter.updateItem(uriList.toTypedArray())
                            itemAdapter.notifyDataSetChanged()
                        }
                        //タップから取得したuriListをSharedPreferencesに保存する。
                        saveUriSet(
                            uriListConvert(uriList),
                            URI_COLLECTION,
                            sharedPreferences
                        )
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

    private fun setUpRecyclerView(uriList: MutableList<Uri>) {
        itemAdapter = UriAdapter(uriList)
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
        itemAdapter.setImageItemLongClickListener(object :
            UriAdapter.OnImageItemLongClickListener {
            var removedUriList: MutableList<Uri> = mutableListOf()
            override fun OnItemLongClick(position: Int) {
                removedUriList = itemAdapter.removeItem(position)
                itemAdapter.notifyItemRemoved(position)
                saveUriSet(uriListConvert(removedUriList), URI_COLLECTION, sharedPreferences)
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
    }

    fun saveUriSet(uriSet: Set<Image>, key: String, sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(uriSet)
        editor.putString(key, json)
        editor.apply()
    }


    fun saveUriSet(uriList: List<Image>, key: String, sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(uriList)
        editor.putString(key, json)
        editor.apply()
    }


    fun getUriArray(key: String, sharedPreferences: SharedPreferences): MutableList<Image> {
        val emptyList = Gson().toJson(mutableListOf<Image>())
        val json = sharedPreferences.getString(key, emptyList)
        Log.d("tag", json!!)
        val type = object : TypeToken<MutableList<Image>>() {}.type
        return Gson().fromJson(json, type)
    }

    //MutableList<Uri> -> MutableSet<Image> 変換メソッド
    private fun uriListConvert(list: MutableList<Uri>): MutableSet<Image> {
        val mutableSet = mutableSetOf<Image>()
        for (uri in list) {
            val image = Image(uri.toString())
            mutableSet.add(image)
        }
        return mutableSet
    }

    //MutableList<Image> -> MutableList<Uri> 変換メソッド
    private fun imageListConvert(imageList: MutableList<Image>): MutableList<Uri> {
        val uriList = mutableListOf<Uri>()
        for (image in imageList) {
            uriList.add(Uri.parse(image.uri))
        }
        return uriList
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
            if (result?.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri: Uri ->
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    sharedPreferences.edit().putString(OPEN_DOCUMENT_PERMISSION, uri.toString())
                }
            }
        }

    private fun externalStoragePath(): String {
        return application.getExternalFilesDir(null).toString()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                //権限が許可されていた場合
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    File(externalStoragePath()).listFiles().map { file ->
                        println(file.name)
                    }
                } else {
                    Handler(Looper.getMainLooper()).post(Runnable {
                        RuntimePermissionUtils().showAlertDialog(
                            supportFragmentManager,
                            "ストレージの読み込み"
                        )
                    })
                }
                return
            }
        }
    }
}

