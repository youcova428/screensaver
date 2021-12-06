package com.example.screensaver

import android.content.Intent
import android.os.Bundle
import android.service.dreams.DreamService
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val serviceIntent = Intent(this, ScreenSaver::class.java)
        startService(serviceIntent)




    }
}