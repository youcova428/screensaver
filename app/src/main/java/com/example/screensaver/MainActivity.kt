package com.example.screensaver

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.controls.ControlsProviderService.TAG
import android.util.Log
import android.widget.Button
import android.widget.Switch
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.helper.widget.MotionEffect.TAG
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException
import java.io.Serializable


class MainActivity : FragmentActivity(R.layout.activity_main) {

    var mUriList: MutableList<Image>? = null
    lateinit var itemAdapter: UriAdapter
    var saveMutableList: MutableList<Image>? = null
    var mPrefUtils: PrefUtils? = null
    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recyclerview_list) }

    companion object {
        var image: Bitmap? = null
        const val URI_COLLECTION = "uri_collection_2"
        const val INTERACTIVE = "isInteractive"
        const val FULL_SCREEN = "isFullScreen"
        const val SCREEN_BRIGHT = "isScreenBright"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mUriList = mutableListOf()
        mPrefUtils = PrefUtils.with(applicationContext)

        val switchInteractive = findViewById<Switch>(R.id.switch_service_interactive)
        switchInteractive.isChecked = mPrefUtils!!.getBoolean(INTERACTIVE, false)
        Log.d("tag", switchInteractive.isChecked.toString())
        switchInteractive.setOnCheckedChangeListener { _, isChecked ->
            mPrefUtils!!.getEditor().apply() {
                putBoolean(INTERACTIVE, isChecked)
                apply()
            }
        }

        val switchFullscreen = findViewById<Switch>(R.id.switch_service_fullscreen)
        switchFullscreen.isChecked = mPrefUtils!!.getBoolean(FULL_SCREEN, true)
        Log.d("tag", switchFullscreen.isChecked.toString())
        switchFullscreen.setOnCheckedChangeListener { _, isChecked ->
            mPrefUtils!!.getEditor().apply() {
                putBoolean(FULL_SCREEN, isChecked)
                apply()
            }
        }

        val switchScreenBright = findViewById<Switch>(R.id.switch_service_screenbright)
        switchScreenBright.isChecked = mPrefUtils!!.getBoolean(SCREEN_BRIGHT, false)
        Log.d("tag", switchScreenBright.isChecked.toString())
        switchScreenBright.setOnCheckedChangeListener { _, isChecked ->
            mPrefUtils!!.getEditor().apply() {
                putBoolean(SCREEN_BRIGHT, isChecked)
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

        val httpButton = findViewById<Button>(R.id.http_start_button)
        httpButton.setOnClickListener {
            val handler = Handler(Looper.getMainLooper())
            val request = Request.Builder()
                .url("https://collectionapi.metmuseum.org/public/collection/v1/objects?metadataDate=2022-01-20")
                .build()
            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseText: String? = response.body?.string()
                    handler.post {
                        println(responseText)
                        val type = object : TypeToken<MuseumObject>() {}.type
                        val museumObject: MuseumObject = Gson().fromJson(responseText, type)
                        //Art, MuseumObjectIdsどちらもパースできることは確認済み
                        println(museumObject.objectIds.last())
                        val intent = Intent(this@MainActivity, MuseumActivity::class.java)
                        intent.putStringArrayListExtra("MuseumObjectIDs", museumObject.objectIds)
                        startActivity(intent)

//
//                      d(R.id.nav_fragment_container) as NavHostFragment
//                        val navController = navHostFragment.navController
//                        NavHostFragment.create(R.navigation.navigation_graph, intent.extras)
//                        findNavController(R.id.).setGraph(R.navigation.navigation_graph, intent.extras)
//                        navController.navigateUp()
//                        NavHostFragment.findNavController(navHostFragment).navigate(R.id.art_list_fragment)
//                        navController.navigate(R.id.art_list_fragment)
                    }
                }
            })
        }
        saveMutableList = mPrefUtils!!.getUriArray(URI_COLLECTION)
        if (!saveMutableList.isNullOrEmpty()) {
            setUpRecyclerView(imageListConvert(saveMutableList!!))
        }
    }

    /**
     * multiSelectPhoto()の結果を受け取りuriListを取得する。
     */
    private val multiActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uriList ->
            var permissionUriList: MutableList<Uri> = mutableListOf()
            if (uriList != null && uriList.size != 0) {
                //SAFから取得したUriにアクセス権限を付与する。
                for (uri in uriList) {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    permissionUriList.add(uri)
                }
                //SharedPreferencesからUriを取得できたとき
                if (saveMutableList!!.isNotEmpty() && saveMutableList != null) {
                    val addUriSet =
                        createAddUriList(imageListConvert(saveMutableList!!), permissionUriList)
                    if (addUriSet != imageListConvert(saveMutableList!!).toMutableSet()) {
                        itemAdapter.updateItem(addUriSet.toTypedArray())
                        itemAdapter.notifyDataSetChanged()
                    }
                    //Set<Uri> -> Set<Image>へ変換する。
                    val convertImageSet = addUriSet.map { Image(it.toString()) }.toSet()
                    mPrefUtils!!.saveUriSet(URI_COLLECTION, convertImageSet)
                } else {
                    //1度立ち上げたが、SharedPreferencesに保存されていない場合
                    if (recyclerView.layoutManager != null) {
                        //uriListが場合
                        val addUriSet =
                            createAddUriList(imageListConvert(saveMutableList!!), permissionUriList)
                        if (addUriSet != saveMutableList) {
                            itemAdapter.updateItem(addUriSet.toTypedArray())
                            itemAdapter.notifyDataSetChanged()
                        }
                        val convertImageSet = addUriSet.map { Image(it.toString()) }.toSet()
                        //sharedPreference追加する
                        mPrefUtils!!.saveUriSet(
                            URI_COLLECTION,
                            convertImageSet,
                        )
                    } else {
                        //RecyclerViewが設置されていない場合(初回), SharedPreferencesからUriSetを取得する
                        setUpRecyclerView(permissionUriList)
                        if (!permissionUriList.isNullOrEmpty()) {
                            itemAdapter.updateItem(permissionUriList.toTypedArray())
                            itemAdapter.notifyDataSetChanged()
                        }
                        //タップから取得したuriListをSharedPreferencesに保存する。
                        mPrefUtils!!.saveUriSet(
                            URI_COLLECTION,
                            uriListConvert(permissionUriList),
                        )
                    }
                }
            }
        }

    /**
     *　画像フォルダから複数枚写真を選択する
     */
    private fun multiSelectPhoto() {
        multiActivityResultLauncher.launch(arrayOf("image/*"))
    }

    /**
     *　RecyclerViewを設置する
     * @param uriList
     */
    private fun setUpRecyclerView(uriList: MutableList<Uri>) {
        itemAdapter = UriAdapter(uriList, applicationContext)
        with(recyclerView) {
            adapter = itemAdapter
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
            var removedUriList: MutableList<Uri> = mutableListOf()
            override fun OnItemLongClick(position: Int) {
                removedUriList = itemAdapter.removeItem(position)
                itemAdapter.notifyItemRemoved(position)
                mPrefUtils!!.saveUriSet(URI_COLLECTION, uriListConvert(removedUriList))
            }
        })
    }

    /**
     *　既存のuriリストと新たに取得したUriリストを足し合わせる
     * @param exitingList : 既存のUriリスト
     * @param uriList : 新たに取得したUriリスト
     */
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

    /**
     * MutableList<Uri> -> MutableSet<Image> 変換メソッド
     * @param list : Uriリスト
     */
    private fun uriListConvert(list: MutableList<Uri>): MutableSet<Image> {
        val mutableSet = mutableSetOf<Image>()
        for (uri in list) {
            val image = Image(uri.toString())
            mutableSet.add(image)
        }
        return mutableSet
    }

    /**
     * MutableList<Image> -> MutableList<Uri> 変換メソッド
     * @param imageList : Imageリスト
     */
    private fun imageListConvert(imageList: MutableList<Image>): MutableList<Uri> {
        val uriList = mutableListOf<Uri>()
        for (image in imageList) {
            uriList.add(Uri.parse(image.uri))
        }
        return uriList
    }

}