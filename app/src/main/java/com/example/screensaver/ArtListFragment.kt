package com.example.screensaver

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.Toolbar
import androidx.annotation.WorkerThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.ferfalk.simplesearchview.SimpleSearchView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException

class ArtListFragment : Fragment() , SimpleSearchView.SearchViewListener{

    lateinit var mView: View
    var mSearchBarFlag : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_art_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mView = view
        val objectList =
            arguments?.getStringArrayList("MuseumObjectIDs")

        val artImageProgress = view.findViewById<ProgressBar>(R.id.art_image_progress)
        val artSearchView = view.findViewById<SimpleSearchView>(R.id.art_simple_search_view)
        val toolbar = view.findViewById<Toolbar>(R.id.art_list_toolbar)
        val viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)

        artSearchView.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            artSearchView.showSearch()
        }

        // 検索バーの表示・非表示
        toolbar.setOnClickListener {
            if (mSearchBarFlag) artSearchView.closeSearch() else artSearchView.showSearch()
        }

        // 検索バーの設置　
        artSearchView.setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

            override fun onQueryTextCleared(): Boolean {
                return false
            }

            // Enter押下時？？　検索時の動作を書けば良き？？
            override fun onQueryTextSubmit(query: String?): Boolean {
                // 番号取得までやると　→　リスト再度表示
                // fixme response null
//                val url = "https://collectionapi.metmuseum.org/public/collection/v1/search?medium=Paintings&hasImages=true&q={$query}"
//
//                val handler = Handler(Looper.getMainLooper())
//                val request = Request.Builder()
//                    .url(url)
//                    .build()
//                val client = OkHttpClient()
//                client.newCall(request).enqueue(object : Callback {
//                    override fun onFailure(call: Call, e: IOException) {
//                        Log.d("tag", "{$e}: request 失敗")
//                    }
//
//                    override fun onResponse(call: Call, response: Response) {
//                        val responseText: String? = response.body?.string()
//                        handler.post {
//                            println(responseText)
//                            val type = object : TypeToken<MuseumObject>() {}.type
//                            val museumObject: MuseumObject = Gson().fromJson(responseText, type)
//                            println(museumObject.objectIds.last())
//                        }
//                    }
//                })
                val museumObject = viewModel.searchMuseumObject(query!!)
                println(museumObject)
                return false
            }
        })

        var nowValue = artImageProgress.progress
        artImageProgress.max = 10
        val artImageMutableList = mutableListOf<Art>()

        GlobalScope.launch(Dispatchers.Main) launch@ {
            if (objectList != null) {
                for (id in objectList) {
                    if (nowValue == artImageProgress.max) {
                        //fixme progressbar disappears
                        artImageProgress.visibility = View.INVISIBLE
                        setUpRecyclerView(artImageMutableList)
                        return@launch
                    }
                    val artObject = getAsyncArtRequest(id)
                    if (artObject.primaryImage.isNotEmpty()) {
                        artImageMutableList.add(artObject)
                        Log.d("tag", artImageMutableList[nowValue].primaryImage)
                        nowValue += 1
                    }
                }
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
                Toast.makeText(mView!!.context, "${art.title}がタップされた。", Toast.LENGTH_SHORT)
                    .show()
                //ArtDetailFragmentへの遷移
                (requireActivity() as MuseumActivity).navigateToArtDetail(art.objectId)
            }
        })
    }

    override fun onSearchViewClosed() {
        mSearchBarFlag = false
        Log.d("SimpleSearchView", "onSearchViewShown")
    }

    override fun onSearchViewClosedAnimation() {
        Log.d("SimpleSearchView", "onSearchViewClosedAnimation")
    }

    override fun onSearchViewShown() {
        mSearchBarFlag = true
        Log.d("SimpleSearchView", "onSearchViewShown")
    }

    override fun onSearchViewShownAnimation() {
        Log.d("SimpleSearchView", "onSearchViewShownAnimation")
    }
    
}


