package com.example.screensaver

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.WorkerThread
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArtListFragment : Fragment() {

    lateinit var mView: View
    private var mArtSearchView: SearchView? = null
    private var mArtImageMutableList = mutableListOf<Art>()
    private var mArtImageProgress: ProgressBar? = null
    private var mGeoLocation: String? = null
    private var mMedium: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_art_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mView = view

        mArtImageProgress = view.findViewById<ProgressBar>(R.id.art_image_progress)
        mArtSearchView = view.findViewById(R.id.art_simple_search_view)
        val viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        val geoChipGroup = view.findViewById<ChipGroup>(R.id.chip_group_geolocation)
        val mediumChipGroup = view.findViewById<ChipGroup>(R.id.chip_group_medium)

        // 検索バーの設置　
        mArtSearchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                // 検索ボタンを押下されたらリストが初期化される。
                mArtImageMutableList = mutableListOf()

                val geoChipBoolList = mutableListOf<Boolean>().apply {
                    geoChipGroup.children.forEach {
                        Log.d("tag", (it as Chip).isChecked.toString())
                        add(it.isChecked)
                    }
                }

                val mediumChipBoolList = mutableListOf<Boolean>().apply {
                    mediumChipGroup.children.forEach {
                        Log.d("tag", (it as Chip).isChecked.toString())
                        add(it.isChecked)
                    }
                }

                val geoIsContains = geoChipBoolList.contains(true)
                val mediumContains = mediumChipBoolList.contains(true)

                when {
                    geoIsContains && mediumContains -> {
                        // todo geoLocation と medium　の検索条件の場合
                        Log.d("tag", "chipどっちもON")
                    }
                    geoIsContains -> {
                        viewModel.searchLocationMsmObj(mGeoLocation!!, query!!)
                        Log.d("tag", "chip geoLocation")
                    }
                    mediumContains -> {
                        viewModel.searchMediumMsmObj(mMedium!!, query!!)
                        Log.d("tag", "chip medium")
                    }
                    else -> {
                        query?.let { viewModel.searchMuseumObject(it) }
                        Log.d("tag", "chip条件なし")
                    }
                }
                return false
            }
        })

        // 画面遷移した後でAPIを叩く
        // todo トップページには何を表示させるのか考える
        if (mArtImageMutableList.isEmpty()) {
            viewModel.searchInitialMsmObj("sea")
        }

        viewModel.initialMsmObjLiveData.observe(viewLifecycleOwner) {
            if (mArtImageMutableList.isEmpty()) {
                searchResultSet(it)
            }
        }

        viewModel.msmObjLiveData.observe(viewLifecycleOwner) {
            if (mArtImageMutableList.isEmpty()) {
                searchResultSet(it)
                // チェックを外す
                geoChipGroup.clearCheck()
                mediumChipGroup.clearCheck()
            }
        }

        // ChipGroup設置
        geoChipGroup.clearCheck()
        geoChipGroup.children.forEach {
            (it as Chip).setOnClickListener { chip ->
                mGeoLocation = (chip as Chip).text as String?
                val checkBool = chip.isChecked
                Log.d("tag", "$mGeoLocation, クリックされている$checkBool")
            }
        }

        mediumChipGroup.clearCheck()
        mediumChipGroup.children.forEach {
            (it as Chip).setOnClickListener { chip ->
                mMedium = (chip as Chip).text as String?
                val checkBool = chip.isChecked
                Log.d("tag", "$mMedium, クリックされている$checkBool")
            }
        }


    }

    @WorkerThread
    private suspend fun getAsyncArtRequest(id: String): Art {
        return withContext(Dispatchers.Default) {
            val http = HttpUtil()
            val response: String? =
                http.httpGet("https://collectionapi.metmuseum.org/public/collection/v1/objects/${id}")
            val type = object : TypeToken<Art>() {}.type
            Gson().fromJson(response, type)
        }
    }

    private fun setUpRecyclerView(artList: MutableList<Art>) {
        val artAdapter = ArtAdapter(artList)
        val recyclerview = mView.findViewById<RecyclerView>(R.id.art_recyclerview)
        with(recyclerview) {
            adapter = artAdapter
            layoutManager =
                StaggeredGridLayoutManager(
                    2,
                    androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
                ).apply {
                    gapStrategy =
                        androidx.recyclerview.widget.StaggeredGridLayoutManager.GAP_HANDLING_NONE
                }
        }
        artAdapter.setOnArtItemClickListener(object : ArtAdapter.OnArtItemClickListener {
            override fun OnArtItemClick(art: Art, view: View) {
                Toast.makeText(mView.context, "${art.title}がタップされた。", Toast.LENGTH_SHORT)
                    .show()
                //ArtDetailFragmentへの遷移
                (requireActivity() as MuseumActivity).navigateToArtDetail(art.objectId)
            }
        })
    }

    private fun searchResultSet(msmObject: MuseumObject) {
        var nowValue = 0
        mArtImageProgress?.max = 20
        mArtImageProgress?.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.Main).launch {
            for (id in msmObject.objectIDs) {
                if (nowValue == mArtImageProgress?.max) {
                    mArtImageProgress?.visibility = View.INVISIBLE
                    setUpRecyclerView(mArtImageMutableList)
                    return@launch
                }
                val artObject = getAsyncArtRequest(id)
                if (artObject.primaryImage.isNotEmpty()) {
                    mArtImageMutableList.add(artObject)
                    Log.d("tag", mArtImageMutableList[nowValue].primaryImage)
                    nowValue += 1
                }
            }
        }
    }

}


