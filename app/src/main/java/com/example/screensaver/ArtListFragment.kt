package com.example.screensaver

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.WorkerThread
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.screensaver.databinding.FragmentArtListBinding
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArtListFragment : Fragment() {

    private lateinit var binding: FragmentArtListBinding
    lateinit var mView: View
    private var mArtImageMutableList = mutableListOf<Art>()
    private var mGeoLocation: String? = null
    private var mMedium: String? = null
    private val viewModel : SearchViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_art_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentArtListBinding.bind(view)
        mView = view

        // 検索バーの設置　
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                // 検索ボタンを押下されたらリストが初期化される。
                mArtImageMutableList = mutableListOf()

                val geoChipBoolList = mutableListOf<Boolean>().apply {
                    println("===== GeoLocationChipGroup ====")
                    binding.chipGroupGeolocation.children.forEach {
                        Log.d("tag", (it as Chip).isChecked.toString())
                        add(it.isChecked)
                    }
                }

                val mediumChipBoolList = mutableListOf<Boolean>().apply {
                    println("===== MediumChipGroup ====")
                    binding.chipGroupMedium.children.forEach {
                        Log.d("tag", (it as Chip).isChecked.toString())
                        add(it.isChecked)
                    }
                }

                val geoIsContains = geoChipBoolList.contains(true)
                val mediumContains = mediumChipBoolList.contains(true)

                when {
                    geoIsContains && mediumContains -> {
                        viewModel.searchLocationMediumMsmObj(mGeoLocation!!, mMedium!!, query!!)
                        Log.d("tag", "chip both")
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
                binding.chipGroupGeolocation.clearCheck()
                binding.chipGroupMedium.clearCheck()
            }
        }

        // ChipGroup設置
        binding.chipGroupGeolocation.clearCheck()
        binding.chipGroupGeolocation.children.forEach {
            (it as Chip).setOnClickListener { chip ->
                mGeoLocation = (chip as Chip).text as String?
                val checkBool = chip.isChecked
                Log.d("tag", "$mGeoLocation, クリックされている$checkBool")
            }
        }

        binding.chipGroupMedium.clearCheck()
        binding.chipGroupMedium.children.forEach {
            (it as Chip).setOnClickListener { chip ->
                mMedium = (chip as Chip).text as String?
                val checkBool = chip.isChecked
                Log.d("tag", "$mMedium, クリックされている$checkBool")
            }
        }

        viewModel.artListLiveData.observe(viewLifecycleOwner) {
            Log.d("tag", "artListLiveData start")
            binding.progressBar.visibility = View.INVISIBLE
            setUpRecyclerView(it.toMutableList())
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
        with(binding.recyclerview) {
            adapter = artAdapter
            layoutManager =
                StaggeredGridLayoutManager(
                    2,
                    StaggeredGridLayoutManager.VERTICAL
                ).apply {
                    gapStrategy =
                        androidx.recyclerview.widget.StaggeredGridLayoutManager.GAP_HANDLING_NONE
                }
        }
        artAdapter.setOnArtItemClickListener(object : ArtAdapter.OnArtItemClickListener {
            override fun OnArtItemClick(art: Art, view: View, artList: MutableList<Art>) {
                Toast.makeText(mView.context, "${art.title}がタップされた。", Toast.LENGTH_SHORT)
                    .show()
                //ArtDetailFragmentへの遷移
                (requireActivity() as MuseumActivity).navigateToArtDetail(art.objectId, artList)
            }
        })
    }

    private fun searchResultSet(msmObject: MuseumObject) {
        var nowValue = 0
        binding.progressBar.max = 20
        binding.progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.Main).launch {
            for (id in msmObject.objectIDs) {
                if (nowValue == binding.progressBar.max) {
                    binding.progressBar.visibility = View.INVISIBLE
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


