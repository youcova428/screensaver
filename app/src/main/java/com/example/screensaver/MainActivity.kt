package com.example.screensaver

import android.content.Intent
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class MainActivity : FragmentActivity(R.layout.activity_main) {

    var mUriList: MutableList<Image>? = null
    lateinit var itemAdapter: UriAdapter
    var saveMutableList: MutableList<Image>? = null
    var mPrefUtils: PrefUtils? = null
    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recyclerview_list) }
    private var mToolBar : MaterialToolbar? = null

    companion object {
        const val URI_COLLECTION = "uri_collection_2"
        const val INTERACTIVE = "isInteractive"
        const val FULL_SCREEN = "isFullScreen"
        const val SCREEN_BRIGHT = "isScreenBright"
        const val SCREEN_SAVER_INFO = "screen_saver_info"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mUriList = mutableListOf()
        mPrefUtils = PrefUtils.with(applicationContext)

        // toolbar 設定アイコン画面遷移
        mToolBar = findViewById<MaterialToolbar>(R.id.top_toolbar).apply {
            title = getString(R.string.app_name)
            setOnMenuItemClickListener {
                val shownFragment = supportFragmentManager.findFragmentById(R.id.main_container)
                when (it.itemId) {
                    R.id.setting -> {
                        if (shownFragment !is SettingFragment) {
                            supportFragmentManager
                                .beginTransaction()
                                .addToBackStack(null)
                                .replace(R.id.main_container, SettingFragment())
                                .commit()
                        }
                    }
                }

                navigationIcon ?: setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
                // 押下時 処理
                setNavigationOnClickListener {
                    navigationIcon?.let {
                        onBackPressed()
                    }
                }
                true
            }
        }

        // bottom navigation　installation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_bar)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.setting_screen_saver ->
                    startActivity(Intent(Settings.ACTION_DREAM_SETTINGS))
                R.id.add_image ->
                    multiSelectPhoto()
                R.id.open_metron_mus -> {
                    val intent = Intent(this@MainActivity, MuseumActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }

        // RecyclerView installation
        saveMutableList = mPrefUtils!!.getUriArray(URI_COLLECTION)
        if (!saveMutableList.isNullOrEmpty()) {
            setUpRecyclerView(imageListConvert(saveMutableList!!))
        }
    }

    override fun onBackPressed() {
        mToolBar?.navigationIcon = null
        super.onBackPressed()
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
                    mPrefUtils?.saveUriSet(URI_COLLECTION, convertImageSet)
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
                Log.d("tag", "画像クリック $uri")
                val inputStream = contentResolver?.openInputStream(uri)
                // 画像の向きを取得する
                val exifInterface = ExifInterface(inputStream!!)
                val direction = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                )
                var d: String = direction.toString()
                when (direction) {
                    ExifInterface.ORIENTATION_NORMAL ->
                        Log.d("tag", "画像の向きは正常　$d")
                    ExifInterface.ORIENTATION_ROTATE_90 ->
                        Log.d("tag", "画像の向きは左に90° $d")
                    ExifInterface.ORIENTATION_ROTATE_270 ->
                        Log.d("tag", "画像の向きは右に90° $d")
                    ExifInterface.ORIENTATION_ROTATE_180 ->
                        Log.d("tag", "画像の向きは右に180° $d")
                    else ->
                        Log.d("tag", "その他？？ $d")
                }
                //　画像の向きを正常に修正する
                if (direction != 1) d = "1"
                val ssImageInfo: Set<String> = setOf(uri.toString(), d)
                mPrefUtils?.saveScreenImageInfo(SCREEN_SAVER_INFO, ssImageInfo)
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

    /**
     * 昨日の日付取得メソッド
     * return 昨日の日付
     */
    private fun getYesterdayDate(): String {
        val todayDate = LocalDate.now()
        val ytdDate = todayDate.minusDays(1)
        val dtFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        Log.d("tag", "昨日の日付 $ytdDate")
        return dtFormat.format(ytdDate)
    }

}