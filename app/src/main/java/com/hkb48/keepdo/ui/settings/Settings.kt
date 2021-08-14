package com.hkb48.keepdo.ui.settings

import android.content.Context
import com.hkb48.keepdo.R
import java.util.*

object Settings {
    private val changedListeners: MutableList<OnChangedListener> = ArrayList(1)
    var doneIconType: String? = null
        set(v) {
            field = v
            for (listener in changedListeners) {
                listener.onDoneIconSettingChanged()
            }
        }
    var dateChangeTime: String? = null
        get() = field ?: "24:00"
        set(v) {
            field = v
            for (listener in changedListeners) {
                listener.onDateChangeTimeSettingChanged()
            }
        }
    var weekStartDay: Int? = null
        get() = field ?: Calendar.SUNDAY
        set(v) {
            field = v
            for (listener in changedListeners) {
                listener.onWeekStartDaySettingChanged()
            }
        }

    var enableFutureDate = false
    var alertsRingTone: String? = null
    var alertsVibrateWhen: String? = null

    interface OnChangedListener {
        fun onDoneIconSettingChanged()
        fun onDateChangeTimeSettingChanged()
        fun onWeekStartDaySettingChanged()
    }

    fun registerOnChangedListener(listener: OnChangedListener?) {
        listener?.let {
            if (!changedListeners.contains(it)) {
                changedListeners.add(it)
            }
        }
    }

    fun unregisterOnChangedListener(listener: OnChangedListener?) {
        listener?.let {
            changedListeners.remove(it)
        }
    }

    fun initialize(context: Context) {
        SettingsFragment.setDefaultValues(context)
        val sharedPref = SettingsFragment.getSharedPreferences(context)
        doneIconType = sharedPref.getString(
            SettingsFragment.KEY_GENERAL_DONE_ICON,
            null
        )
        dateChangeTime = sharedPref.getString(
            SettingsFragment.KEY_GENERAL_DATE_CHANGE_TIME,
            null
        )
        weekStartDay = sharedPref.getString(
            SettingsFragment.KEY_CALENDAR_WEEK_START_DAY,
            null
        )?.toInt()
        enableFutureDate = sharedPref.getBoolean(
            SettingsFragment.KEY_CALENDAR_ENABLE_FUTURE_DATE,
            false
        )
        alertsRingTone = sharedPref.getString(
            SettingsFragment.KEY_ALERTS_RINGTONE,
            null
        )
        alertsVibrateWhen = sharedPref.getString(
            SettingsFragment.KEY_ALERTS_VIBRATE_WHEN,
            null
        )
    }

    val doneIconId: Int
        get() {
            return when (doneIconType) {
                "type2" -> R.drawable.ic_done_2
                "type3" -> R.drawable.ic_done_3
                "type4" -> R.drawable.ic_done_4
                else -> R.drawable.ic_done_1
            }
        }

    val notDoneIconId: Int
        get() {
            return when (doneIconType) {
                "type2" -> R.drawable.ic_not_done_2
                "type3" -> R.drawable.ic_not_done_3
                "type4" -> R.drawable.ic_not_done_4
                else -> R.drawable.ic_not_done_1
            }
        }
}