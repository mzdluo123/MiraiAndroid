package io.github.mzdluo123.mirai.android.ui

import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import io.github.mzdluo123.mirai.android.R


class SettingFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = "setting"
        setPreferencesFromResource(R.xml.setting_screen, rootKey)
        val logBuffer = findPreference<EditTextPreference>("log_buffer_preference")
        logBuffer?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        logBuffer?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
        }

        val refreshCount = findPreference<EditTextPreference>("status_refresh_count")
        refreshCount?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        refreshCount?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
        }
    }


}