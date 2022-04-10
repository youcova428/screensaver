package com.example.screensaver

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PrefUtils {

    companion object {
        private var singleton: PrefUtils? = null
        private lateinit var preferences: SharedPreferences
        lateinit var editor: SharedPreferences.Editor

        fun with(context: Context): PrefUtils {
            if (singleton == null)
                singleton = Build(context, null, -1).build()
            return singleton as PrefUtils
        }

        fun with(context: Context, name: String, mode: Int): PrefUtils {
            if (singleton == null)
                singleton = Build(context, name, mode).build()
            return singleton as PrefUtils
        }
    }

    constructor(context: Context) {
        preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        editor = preferences.edit()
    }

    constructor(context: Context, name: String, mode: Int) {
        preferences = context.getSharedPreferences(name, mode)
        editor = preferences.edit()
    }

    fun saveBoolean(key: String, boolean: Boolean) {
        editor.putBoolean(key, boolean).apply()
    }

    fun saveUriSet(key: String, uriSet: Set<Image>) {
        val json = Gson().toJson(uriSet)
        editor.putString(key, json).apply()
    }

    // スクリーンセイバー設定画像の情報保存
    fun saveScreenImageInfo(key: String, setImage: Set<String>) {
        val json = Gson().toJson(setImage)
        editor.putString(key, json)
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return preferences.getBoolean(key, defValue)
    }

    fun getUriArray(key: String): MutableList<Image> {
        val emptyList = Gson().toJson(mutableListOf<Image>())
        val json = preferences.getString(key, emptyList)
        Log.d("tag", json!!)
        val type = object : TypeToken<MutableList<Image>>() {}.type
        return Gson().fromJson(json, type)
    }

    // スクリーンセイバーの設定画像の情報取得
    fun getScreenImageInfo(key: String): List<String>? {
        val list = Gson().toJson(listOf<String>())
        val json = preferences.getString(key, list)
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun remove(key: String) {
        editor.remove(key).apply()
    }

    fun clear() {
        editor.clear().apply()
    }

    fun getEditor(): SharedPreferences.Editor {
        return editor
    }


    private class Build(val context: Context, val name: String?, val mode: Int) {
        fun build(): PrefUtils {
            if (mode == -1 || name == null) {
                return PrefUtils(context)
            }
            return PrefUtils(context, name, mode)
        }
    }
}