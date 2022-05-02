package com.example.screensaver

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat


class SettingFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_main, rootKey)
    }
}