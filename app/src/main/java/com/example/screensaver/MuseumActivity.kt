package com.example.screensaver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MuseumActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_museum)

        val objectList: ArrayList<MuseumObject> =
            intent.getSerializableExtra("MuseumObjects") as ArrayList<MuseumObject>
        for (artObject in objectList) {
            println(artObject.toString())
        }
    }
}