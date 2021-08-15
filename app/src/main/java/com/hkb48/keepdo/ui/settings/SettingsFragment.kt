package com.hkb48.keepdo.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Vibrator
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.preference.*
import com.hkb48.keepdo.NotificationController
import com.hkb48.keepdo.R
import com.hkb48.keepdo.util.CompatUtil

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
    private lateinit var doneIconPref: DoneIconPreference
    private lateinit var dateChangeTimePref: ListPreference
    private lateinit var weekStartDayPref: ListPreference
    private lateinit var enableFutureDatePref: SwitchPreferenceCompat
    private lateinit var ringtonePref: RingtonePreference
    private lateinit var vibrateWhenPref: ListPreference
    private val ringtonePickerLauncher: ActivityResultLauncher<Int> =
        registerForActivityResult(
            PickRingtone()
        ) { uri ->
            uri?.let {
                ringtonePref.updatePreference(it.toString())
            }
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.general_settings, rootKey)
        val preferenceScreen = preferenceScreen
        doneIconPref = preferenceScreen.findPreference(KEY_GENERAL_DONE_ICON)!!
        dateChangeTimePref = preferenceScreen.findPreference(KEY_GENERAL_DATE_CHANGE_TIME)!!
        val summary =
            getString(R.string.preferences_date_change_time_summary, dateChangeTimePref.value)
        dateChangeTimePref.summary = summary
        weekStartDayPref = preferenceScreen.findPreference(KEY_CALENDAR_WEEK_START_DAY)!!
        weekStartDayPref.summary = weekStartDayPref.entry
        enableFutureDatePref = preferenceScreen.findPreference(KEY_CALENDAR_ENABLE_FUTURE_DATE)!!
        val alertGroup = preferenceScreen.findPreference<PreferenceCategory>(KEY_ALERTS_CATEGORY)!!
        ringtonePref = preferenceScreen.findPreference(KEY_ALERTS_RINGTONE)!!
        vibrateWhenPref = preferenceScreen.findPreference(KEY_ALERTS_VIBRATE_WHEN)!!
        val notificationPref = preferenceScreen.findPreference<Preference>(
            KEY_ALERTS_NOTIFICATION
        )!!

        if (CompatUtil.isNotificationChannelSupported()) {
            notificationPref.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    startActivity(Intent(android.provider.Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                        putExtra(
                            android.provider.Settings.EXTRA_CHANNEL_ID,
                            NotificationController.notificationChannelId
                        )
                        putExtra(
                            android.provider.Settings.EXTRA_APP_PACKAGE,
                            requireContext().packageName
                        )
                    })
                    true
                }
            alertGroup.removePreference(ringtonePref)
            alertGroup.removePreference(vibrateWhenPref)
        } else {
            alertGroup.removePreference(notificationPref)
            @Suppress("DEPRECATION")
            val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
            if (!vibrator?.hasVibrator()!!) {
                alertGroup.removePreference(vibrateWhenPref)
            } else {
                vibrateWhenPref.summary = vibrateWhenPref.entry
            }
            ringtonePref.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    ringtonePickerLauncher.launch(RingtoneManager.TYPE_ALARM)
                    true
                }
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is DoneIconPreference) {
            val dialogFragment = DoneIconDialogFragment.newInstance(preference.getKey())
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(parentFragmentManager, null)
        } else super.onDisplayPreferenceDialog(preference)
    }

    override fun onStart() {
        super.onStart()
        setListeners(this)
    }

    override fun onStop() {
        super.onStop()
        setListeners(null)
    }

    private fun setListeners(listener: Preference.OnPreferenceChangeListener?) {
        doneIconPref.onPreferenceChangeListener = listener
        dateChangeTimePref.onPreferenceChangeListener = listener
        weekStartDayPref.onPreferenceChangeListener = listener
        enableFutureDatePref.onPreferenceChangeListener = listener
        vibrateWhenPref.onPreferenceChangeListener = listener
        ringtonePref.onPreferenceChangeListener = listener
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        var ret = false
        when {
            preference === doneIconPref -> {
                Settings.doneIconType = newValue as String
                ret = true
            }
            preference === dateChangeTimePref -> {
                Settings.dateChangeTime = newValue as String
                dateChangeTimePref.value = newValue
                val summary = getString(
                    R.string.preferences_date_change_time_summary,
                    dateChangeTimePref.value
                )
                dateChangeTimePref.summary = summary
            }
            preference === weekStartDayPref -> {
                Settings.weekStartDay = (newValue as String).toInt()
                weekStartDayPref.value = newValue
                weekStartDayPref.summary = weekStartDayPref.entry
            }
            preference === enableFutureDatePref -> {
                Settings.enableFutureDate = newValue as Boolean
                ret = true
            }
            preference === vibrateWhenPref -> {
                Settings.alertsVibrateWhen = newValue as String
                vibrateWhenPref.value = newValue
                vibrateWhenPref.summary = vibrateWhenPref.entry
            }
            preference === ringtonePref -> {
                Settings.alertsRingTone = newValue as String
                ret = true
            }
        }
        return ret
    }

    class PickRingtone : ActivityResultContract<Int, Uri?>() {
        override fun createIntent(context: Context, ringtoneType: Int) =
            Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, ringtoneType)
                Settings.alertsRingTone?.let {
                    putExtra(
                        RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                        Uri.parse(it)
                    )
                }
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            }

        override fun parseResult(resultCode: Int, result: Intent?): Uri? {
            if (resultCode != Activity.RESULT_OK) {
                return null
            }
            return result?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        }
    }

    companion object {
        const val KEY_GENERAL_DONE_ICON = "preferences_done_icon"
        const val KEY_GENERAL_DATE_CHANGE_TIME = "preferences_date_change_time"
        const val KEY_CALENDAR_WEEK_START_DAY = "preferences_calendar_week_start_day"
        const val KEY_CALENDAR_ENABLE_FUTURE_DATE = "preferences_calendar_enable_future_date"
        const val KEY_ALERTS_RINGTONE = "preferences_alerts_ringtone"
        const val KEY_ALERTS_VIBRATE_WHEN = "preferences_alerts_vibrateWhen"
        private const val KEY_ALERTS_CATEGORY = "preferences_alerts_category"
        private const val KEY_ALERTS_NOTIFICATION = "preferences_notification"

        fun getSharedPreferences(context: Context?): SharedPreferences {
            return PreferenceManager.getDefaultSharedPreferences(context)
        }

        /**
         * Set the default shared preferences in the proper context
         */
        fun setDefaultValues(context: Context?) {
            PreferenceManager.setDefaultValues(context, R.xml.general_settings, false)
        }
    }
}