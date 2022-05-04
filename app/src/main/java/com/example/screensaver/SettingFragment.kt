package com.example.screensaver

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference


class SettingFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_main, rootKey)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // review ダークモード対応
        val v = super.onCreateView(inflater, container, savedInstanceState)
        v.setBackgroundResource(R.color.black)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = PrefUtils.with(requireContext())

        // interactive項目 押下時処理
        val interactivePref =
            findPreference<SwitchPreference>(getString(R.string.pref_key_interactive))
        interactivePref?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val checkedBool = interactivePref?.isChecked
            sharedPref.getEditor().apply() {
                checkedBool?.let { bool ->
                    putBoolean(
                        getString(R.string.pref_key_interactive),
                        bool
                    )
                }
                apply()
            }
            Log.d(
                "tag",
                "変化後${getString(R.string.pref_key_interactive)} : ${
                    sharedPref.getBoolean(
                        getString(R.string.pref_key_interactive), false
                    )
                }"
            )
            true
        }

        // FullScreen項目 押下時処理
        val fullScreenPref =
            findPreference<SwitchPreference>(getString(R.string.pref_key_fullscreen))
        fullScreenPref?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val checkedBool = fullScreenPref?.isChecked
            sharedPref.getEditor().apply() {
                checkedBool?.let { bool ->
                    putBoolean(
                        getString(R.string.pref_key_fullscreen),
                        bool
                    )
                }
                apply()
            }
            Log.d(
                "tag",
                "変化後${getString(R.string.pref_key_fullscreen)} : ${
                    sharedPref.getBoolean(
                        getString(R.string.pref_key_fullscreen),
                        false
                    )
                }"
            )
            true
        }

        // ScreenBright項目 押下時処理
        val screenBrightPref =
            findPreference<SwitchPreference>(getString(R.string.pref_key_screen_bright))
        screenBrightPref?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val checkedBool = screenBrightPref?.isChecked
            sharedPref.getEditor().apply() {
                checkedBool?.let { bool ->
                    putBoolean(
                        getString(R.string.pref_key_screen_bright),
                        bool
                    )
                }
                apply()
            }
            Log.d(
                "tag",
                "変化後${getString(R.string.pref_key_screen_bright)} : ${
                    sharedPref.getBoolean(
                        getString(R.string.pref_key_screen_bright),
                        false
                    )
                }"
            )
            true
        }
    }
}