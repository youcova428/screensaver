package com.example.screensaver

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.ContactsContract.Intents.Insert.ACTION
import android.service.dreams.DreamService
import android.widget.ImageView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.IOException

class ScreenSaver : DreamService() {

    val ACTION_IS_RUNNING = "DreamService_is_running"

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true
        isScreenBright = false
        setContentView(R.layout.screen_saver)
//        findViewById<ImageView>(R.id.screen_picture).setImageResource(R.drawable.pict_mvis)
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        findViewById<ImageView>(R.id.screen_picture).setImageResource(R.drawable.pict_mvis)
    }

    override fun onDreamingStopped() {
        super.onDreamingStopped()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    fun getDrawableFromAsset(url: String): Drawable? {
        try {
            return Drawable.createFromStream(applicationContext.assets.open(url),null)
        } catch (e: IOException ){
            e.printStackTrace()
        }catch (e : IllegalAccessException){
            e.printStackTrace()
        }
        return null
    }

    fun isMyServiceRunning(context: Context) : Boolean{
        return LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(ACTION_IS_RUNNING))
    }

}