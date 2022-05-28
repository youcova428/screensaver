package com.example.screensaver

import android.content.Intent
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.screensaver.databinding.ActivityMainBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class MainActivity : FragmentActivity(R.layout.activity_main) {

    private lateinit var binding: ActivityMainBinding
    var mUriList: MutableList<Image>? = null
    lateinit var itemAdapter: UriAdapter
    var saveMutableList: MutableList<Image>? = null
    var mPrefUtils: PrefUtils? = null

    companion object {
        const val URI_COLLECTION = "uri_collection"
        const val SCREEN_SAVER_INFO = "screen_saver_info"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mUriList = mutableListOf()
        mPrefUtils = PrefUtils.with(applicationContext)

        // toolbar 設定アイコン画面遷移
        binding.topToolbar.apply {
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
        binding.bottomNavigationBar.setOnItemSelectedListener {
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
        binding.topToolbar.navigationIcon = null
        super.onBackPressed()
    }

    /**
     * multiSelectPhoto()の結果を受け取りuriListを取得する。
     */
    private val multiActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uriList ->
            val permissionUriList: MutableList<Uri> = mutableListOf()
            if (!uriList.isNullOrEmpty()) {
                //SAFから取得したUriにアクセス権限を付与する。
                for (uri in uriList) {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    permissionUriList.add(uri)
                }

                saveMutableList = mPrefUtils?.getUriArray(URI_COLLECTION)

                //SharedPreferencesからUriを取得できたとき
                if (!saveMutableList.isNullOrEmpty()) {
                    val addUriSet =
                        createAddUriList(imageListConvert(saveMutableList!!), permissionUriList)
                    if (addUriSet != imageListConvert(saveMutableList!!).toMutableSet()) {
                        itemAdapter.updateItem(addUriSet.toTypedArray())
                        itemAdapter.notifyItemRangeChanged(0, addUriSet.size)
                    }
                    //Set<Uri> -> Set<Image>へ変換する。
                    val convertImageSet = addUriSet.map { Image(it.toString()) }.toSet()
                    mPrefUtils?.saveUriSet(URI_COLLECTION, convertImageSet)
                } else {
                    //1度立ち上げたが、SharedPreferencesに保存されていない場合
                    binding.recyclerView.layoutManager?.let {
                        //uriListが場合
                        val addUriSet =
                            createAddUriList(imageListConvert(saveMutableList!!), permissionUriList)
                        if (addUriSet != saveMutableList) {
                            itemAdapter.updateItem(addUriSet.toTypedArray())
                            itemAdapter.notifyItemRangeChanged(0, addUriSet.size)
                        }
                        val convertImageSet = addUriSet.map { Image(it.toString()) }.toSet()
                        //sharedPreference追加する
                        mPrefUtils!!.saveUriSet(
                            URI_COLLECTION,
                            convertImageSet,
                        )
                    } ?:
                    //RecyclerViewが設置されていない場合(初回), SharedPreferencesからUriSetを取得する
                    setUpRecyclerView(permissionUriList)
                    if (!permissionUriList.isNullOrEmpty()) {
                        itemAdapter.updateItem(permissionUriList.toTypedArray())
                        itemAdapter.notifyItemRangeChanged(0, permissionUriList.size)
                    }
                    //タップから取得したuriListをSharedPreferencesに保存する。
                    mPrefUtils!!.saveUriSet(
                        URI_COLLECTION,
                        uriListConvert(permissionUriList),
                    )
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
        with(binding.recyclerView) {
            adapter = itemAdapter
            layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL).apply {
                    gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
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
                itemAdapter.notifyItemRangeChanged(position, removedUriList.size)
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
        val addUriList = exitingList.union(uriList)
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