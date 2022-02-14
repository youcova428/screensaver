package com.example.screensaver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MuseumActivity : AppCompatActivity() {

    private var mObjectList: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_museum)

        mObjectList =
            intent.getStringArrayListExtra("MuseumObjectIDs")

        if (savedInstanceState == null && mObjectList != null) {
            val args = Bundle().apply {
                putStringArrayList(
                    "MuseumObjectIDs",
                    mObjectList
                )
            }
            val fragment = ArtListFragment()
            fragment.arguments = args
            supportFragmentManager.beginTransaction().add(
                R.id.nav_fragment_container,
                fragment,
                null,
            ).commit()
        }
    }

    override fun onStart() {
        super.onStart()
    }

    fun navigateToArtDetail(artId: String) {
        val fragment = ArtDetailFragment()
        val args = Bundle().apply {
            putString("ArtId", artId)
        }
        fragment.arguments = args
        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.nav_fragment_container, fragment, null)
            .commit()
    }
}