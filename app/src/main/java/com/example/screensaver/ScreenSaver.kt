package com.example.screensaver

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.service.dreams.DreamService
import android.widget.ImageView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.screensaver.MainActivity.Companion.SCREEN_SAVER_INFO
import java.io.IOException

class ScreenSaver : DreamService() {

    companion object {
        const val URI_SHIBA_PHOTO = "android.resource://com.example.screensaver/drawable/shiba_dog"
        const val ACTION_IS_RUNNING = "DreamService_is_running"
    }

    private var screenImage: ImageView? = null
    private var screenImageInfo: List<String>? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // sharedPreference から値を取得する　設定されてなければデフォルトの値になる。
        val prefUtils = PrefUtils.with(applicationContext).apply {
            isInteractive = getBoolean(getString(R.string.pref_key_interactive), false)
            isFullscreen = getBoolean(getString(R.string.pref_key_fullscreen), true)
            isScreenBright = getBoolean(getString(R.string.pref_key_screen_bright), false)
        }

        setContentView(R.layout.screen_saver)
        screenImage = findViewById(R.id.screen_picture)
        prefUtils.apply {
            saveBoolean(getString(R.string.pref_key_interactive), isInteractive)
            saveBoolean(getString(R.string.pref_key_fullscreen), isFullscreen)
            saveBoolean(getString(R.string.pref_key_screen_bright), isScreenBright)
        }

        val defImageInfo = listOf(URI_SHIBA_PHOTO, "1")
        screenImageInfo = prefUtils.getScreenImageInfo(SCREEN_SAVER_INFO) ?: defImageInfo
        if (screenImageInfo?.isEmpty() == true) screenImageInfo = defImageInfo
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        screenImageInfo?.let {
            screenImage?.setImageURI(Uri.parse(it[0]))
        }
    }

    override fun onDreamingStopped() {
        super.onDreamingStopped()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    fun getDrawableFromAsset(url: String): Drawable? {
        try {
            return Drawable.createFromStream(applicationContext.assets.open(url), null)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return null
    }

    fun isMyServiceRunning(context: Context): Boolean {
        return LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(ACTION_IS_RUNNING))
    }

}