package com.hkb48.keepdo.settings

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

class GeneralSettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {
    private lateinit var mDoneIconPref: DoneIconPreference
    private lateinit var mDateChangeTimePref: ListPreference
    private lateinit var mWeekStartDayPref: ListPreference
    private lateinit var mEnableFutureDatePref: SwitchPreferenceCompat
    private lateinit var mRingtonePref: RingtonePreference
    private lateinit var mVibrateWhenPref: ListPreference
    private val mRingtonePickerLauncher: ActivityResultLauncher<Int> =
        registerForActivityResult(
            PickRingtone()
        ) { uri ->
            uri?.let {
                mRingtonePref.updatePreference(it.toString())
            }
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.general_settings, rootKey)
        val preferenceScreen = preferenceScreen
        mDoneIconPref = preferenceScreen.findPreference(KEY_GENERAL_DONE_ICON)!!
        mDateChangeTimePref = preferenceScreen.findPreference(KEY_GENERAL_DATE_CHANGE_TIME)!!
        val summary =
            getString(R.string.preferences_date_change_time_summary, mDateChangeTimePref.value)
        mDateChangeTimePref.summary = summary
        mWeekStartDayPref = preferenceScreen.findPreference(KEY_CALENDAR_WEEK_START_DAY)!!
        mWeekStartDayPref.summary = mWeekStartDayPref.entry
        mEnableFutureDatePref = preferenceScreen.findPreference(KEY_CALENDAR_ENABLE_FUTURE_DATE)!!
        val alertGroup = preferenceScreen.findPreference<PreferenceCategory>(KEY_ALERTS_CATEGORY)!!
        mRingtonePref = preferenceScreen.findPreference(KEY_ALERTS_RINGTONE)!!
        mVibrateWhenPref = preferenceScreen.findPreference(KEY_ALERTS_VIBRATE_WHEN)!!
        val notificationPref = preferenceScreen.findPreference<Preference>(
            KEY_ALERTS_NOTIFICATION
        )!!

        if (CompatUtil.isNotificationChannelSupported) {
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
            alertGroup.removePreference(mRingtonePref)
            alertGroup.removePreference(mVibrateWhenPref)
        } else {
            alertGroup.removePreference(notificationPref)
            @Suppress("DEPRECATION")
            val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
            if (!vibrator?.hasVibrator()!!) {
                alertGroup.removePreference(mVibrateWhenPref)
            } else {
                mVibrateWhenPref.summary = mVibrateWhenPref.entry
            }
            mRingtonePref.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    mRingtonePickerLauncher.launch(RingtoneManager.TYPE_ALARM)
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
        mDoneIconPref.onPreferenceChangeListener = listener
        mDateChangeTimePref.onPreferenceChangeListener = listener
        mWeekStartDayPref.onPreferenceChangeListener = listener
        mEnableFutureDatePref.onPreferenceChangeListener = listener
        mVibrateWhenPref.onPreferenceChangeListener = listener
        mRingtonePref.onPreferenceChangeListener = listener
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        var ret = false
        when {
            preference === mDoneIconPref -> {
                Settings.doneIconType = newValue as String
                ret = true
            }
            preference === mDateChangeTimePref -> {
                Settings.dateChangeTime = newValue as String
                mDateChangeTimePref.value = newValue
                val summary = getString(
                    R.string.preferences_date_change_time_summary,
                    mDateChangeTimePref.value
                )
                mDateChangeTimePref.summary = summary
            }
            preference === mWeekStartDayPref -> {
                Settings.weekStartDay = (newValue as String).toInt()
                mWeekStartDayPref.value = newValue
                mWeekStartDayPref.summary = mWeekStartDayPref.entry
            }
            preference === mEnableFutureDatePref -> {
                Settings.enableFutureDate = newValue as Boolean
                ret = true
            }
            preference === mVibrateWhenPref -> {
                Settings.alertsVibrateWhen = newValue as String
                mVibrateWhenPref.value = newValue
                mVibrateWhenPref.summary = mVibrateWhenPref.entry
            }
            preference === mRingtonePref -> {
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