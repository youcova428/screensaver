package com.example.screensaver

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Switch
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val serviceIntent = Intent(this, ScreenSaver::class.java)
//        serviceIntent.action = Settings.ACTION_DREAM_SETTINGS
//        startService(serviceIntent)

        val sharedPreferences: SharedPreferences =
            getSharedPreferences("SharedPreference", Context.MODE_PRIVATE)
        val switchInteractive = findViewById<Switch>(R.id.switch_service_interactive)
        switchInteractive.isChecked = sharedPreferences.getBoolean("isInteractive", true)
        switchInteractive.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("isInteractive", isChecked)
            sharedPreferences.edit().apply()
        }

        val switchFullscreen = findViewById<Switch>(R.id.switch_service_fullscreen)
        switchFullscreen.isChecked = sharedPreferences.getBoolean("isFullScreen", true)
        switchFullscreen.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("isFullScreen", isChecked)
            sharedPreferences.edit().apply()
        }

        val switchScreenBright = findViewById<Switch>(R.id.switch_service_screenbright)
        switchFullscreen.isChecked = sharedPreferences.getBoolean("isScreenBright", true)
        switchScreenBright.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("isScreenBright", isChecked)
            sharedPreferences.edit().apply()
        }

        val photoButton = findViewById<Button>(R.id.photo_button)
        photoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.setType("image/*")
            //startActivityForResultから書き換え
            activityResultLauncher.launch("image/*")
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { url ->
            Log.d("tag", "uri =${url}")
        }
}