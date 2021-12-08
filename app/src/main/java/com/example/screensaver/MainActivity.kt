package com.example.screensaver

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.service.dreams.DreamService
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val serviceIntent = Intent(this, ScreenSaver::class.java)
        serviceIntent.action = Settings.ACTION_DREAM_SETTINGS
        startService(serviceIntent)
    }
}