package com.example.screensaver

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.WorkerThread
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArtListFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_art_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val objectList =
            arguments?.getStringArrayList("MuseumObjectIDs")
        val artImageProgress = view!!.findViewById<ProgressBar>(R.id.art_image_progress)
        var nowValue = artImageProgress.progress
        artImageProgress.max = 100
        val artImageMutableList = mutableListOf<Art>()

        GlobalScope.launch(Dispatchers.Main) {
            for (id in objectList!!) {
                if (nowValue == artImageProgress.max) {
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
        val recyclerview = view!!.findViewById<RecyclerView>(R.id.art_recyclerview)
        with(recyclerview) {
            adapter = artAdapter
            layoutManager =
                StaggeredGridLayoutManager(2, androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL).apply {
                    gapStrategy = androidx.recyclerview.widget.StaggeredGridLayoutManager.GAP_HANDLING_NONE
                }
        }
        artAdapter.setOnArtItemClickListener(object : ArtAdapter.OnArtItemClickListener {
            override fun OnArtItemClick(art: Art) {
                Toast.makeText(view!!.context, "${art.title}がタップされた。", Toast.LENGTH_SHORT)
                    .show()
//                val transaction = supportFragmentManager.beginTransaction()
//                transaction.add(R.id.fragment_container, ArtDetailFragment.newInstance(art.objectId))
//                transaction.commit()

                //ArtDetailFragmentへの遷移
                val action = ArtListFragmentDirections.actionArtListToDetailFragment(art.objectId)
                findNavController().navigate(action)
            }
        })
    }

}