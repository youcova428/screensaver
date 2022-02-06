package com.example.screensaver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.WorkerThread
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.*

class MuseumActivity : AppCompatActivity() {

    private var mObjectList: ArrayList<String>? = null
    private var mArgs: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_museum)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(
                R.id.nav_fragment_container,
                ArtListFragment(),
                null,
            ).commit()
        }

        mObjectList =
            intent.getStringArrayListExtra("MuseumObjectIDs")

        val firstFragment = ArtListFragment()
        mArgs = Bundle().apply {
            putStringArrayList("MuseumObjectIDs", mObjectList)
        }


        findNavController(R.id.nav_fragment_container)
            .setGraph(R.navigation.navigation_graph, mArgs)
    }

    override fun onStart() {
        super.onStart()
    }

    fun navigateToArtDetail( artId : String) {
        val fragment = ArtDetailFragment()
        val args = Bundle().apply {
            putString("ArtId", artId)
        }
        fragment.arguments = args
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_fragment_container , fragment , null)
            .commit()
    }
}