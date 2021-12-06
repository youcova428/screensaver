package com.example.screensaver

import android.graphics.drawable.Drawable
import android.service.dreams.DreamService
import android.widget.ImageView
import java.io.IOException

class ScreenSaver : DreamService() {
    override fun onDreamingStarted() {
        super.onDreamingStarted()
        isFullscreen = true
        isScreenBright = false
        setContentView(R.layout.screen_saver)
        findViewById<ImageView>(R.id.screen_picture).setImageResource(R.drawable.pict_mvis)
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

}