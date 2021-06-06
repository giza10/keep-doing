package com.hkb48.keepdo.settings

import android.content.Context
import android.content.SharedPreferences
import com.hkb48.keepdo.R
import java.util.*

class Settings private constructor(private val sharedPref: SharedPreferences) {
    private val changedListeners: MutableList<OnChangedListener> = ArrayList(1)
    private var doneIconType: String? = null
    private var dateChangeTime: String? = null
    private var weekStartDay: String? = null
    private var enableFutureDate = false
    private var alertsRingTone: String? = null
    private var alertsVibrateWhen: String? = null

    interface OnChangedListener {
        fun onDoneIconSettingChanged()
        fun onDateChangeTimeSettingChanged()
        fun onWeekStartDaySettingChanged()
    }

    companion object {
        private lateinit var sInstance: Settings

        @JvmStatic
        fun registerOnChangedListener(listener: OnChangedListener?) {
            if (listener != null && !sInstance.changedListeners.contains(listener)) {
                sInstance.changedListeners.add(listener)
            }
        }

        @JvmStatic
        fun unregisterOnChangedListener(listener: OnChangedListener?) {
            if (listener != null) {
                sInstance.changedListeners.remove(listener)
            }
        }

        @JvmStatic
        fun initialize(context: Context) {
            if (!::sInstance.isInitialized) {
                GeneralSettingsFragment.setDefaultValues(context)
                val pref = GeneralSettingsFragment.getSharedPreferences(context)
                sInstance = Settings(pref)
                sInstance.doneIconType = sInstance.sharedPref.getString(
                    GeneralSettingsFragment.KEY_GENERAL_DONE_ICON,
                    null
                )
                sInstance.dateChangeTime = sInstance.sharedPref.getString(
                    GeneralSettingsFragment.KEY_GENERAL_DATE_CHANGE_TIME,
                    null
                )
                sInstance.weekStartDay = sInstance.sharedPref.getString(
                    GeneralSettingsFragment.KEY_CALENDAR_WEEK_START_DAY,
                    null
                )
                sInstance.enableFutureDate = sInstance.sharedPref.getBoolean(
                    GeneralSettingsFragment.KEY_CALENDAR_ENABLE_FUTURE_DATE,
                    false
                )
                sInstance.alertsRingTone = sInstance.sharedPref.getString(
                    GeneralSettingsFragment.KEY_ALERTS_RINGTONE,
                    null
                )
                sInstance.alertsVibrateWhen = sInstance.sharedPref.getString(
                    GeneralSettingsFragment.KEY_ALERTS_VIBRATE_WHEN,
                    null
                )
            }
        }

        fun setDoneIcon(v: String?) {
            sInstance.doneIconType = v
            for (listener in sInstance.changedListeners) {
                listener.onDoneIconSettingChanged()
            }
        }

        @JvmStatic
        val doneIconId: Int
            get() {
                var doneIconId = R.drawable.ic_done_1
                if (sInstance.doneIconType != null) {
                    doneIconId = when (sInstance.doneIconType) {
                        "type2" -> R.drawable.ic_done_2
                        "type3" -> R.drawable.ic_done_3
                        "type4" -> R.drawable.ic_done_4
                        else -> R.drawable.ic_done_1
                    }
                }
                return doneIconId
            }

        @JvmStatic
        val notDoneIconId: Int
            get() {
                var notDoneIconId = R.drawable.ic_not_done_1
                if (sInstance.doneIconType != null) {
                    notDoneIconId = when (sInstance.doneIconType) {
                        "type2" -> R.drawable.ic_not_done_2
                        "type3" -> R.drawable.ic_not_done_3
                        "type4" -> R.drawable.ic_not_done_4
                        else -> R.drawable.ic_not_done_1
                    }
                }
                return notDoneIconId
            }

        @JvmStatic
        fun getDateChangeTime(): String? {
            if (sInstance.dateChangeTime == null) {
                sInstance.dateChangeTime = "24:00"
            }
            return sInstance.dateChangeTime
        }

        fun setDateChangeTime(v: String?) {
            sInstance.dateChangeTime = v
            for (listener in sInstance.changedListeners) {
                listener.onDateChangeTimeSettingChanged()
            }
        }

        @JvmStatic
        fun getWeekStartDay(): Int {
            if (sInstance.weekStartDay == null) {
                sInstance.weekStartDay = "1"
            }
            val weekStartDay = sInstance.weekStartDay!!.toInt()
            return if (Calendar.SUNDAY <= weekStartDay && weekStartDay <= Calendar.SATURDAY) {
                weekStartDay
            } else {
                Calendar.SUNDAY
            }
        }

        fun setWeekStartDay(v: String?) {
            sInstance.weekStartDay = v
            for (listener in sInstance.changedListeners) {
                listener.onWeekStartDaySettingChanged()
            }
        }

        fun getEnableFutureDate(): Boolean {
            return sInstance.enableFutureDate
        }

        fun setEnableFutureDate(v: Boolean) {
            sInstance.enableFutureDate = v
        }

        @JvmStatic
        fun getAlertsRingTone(): String? {
            return sInstance.alertsRingTone
        }

        fun setAlertsRingTone(v: String?) {
            sInstance.alertsRingTone = v
        }

        @JvmStatic
        fun getAlertsVibrateWhen(): String? {
            return sInstance.alertsVibrateWhen
        }

        fun setAlertsVibrateWhen(v: String?) {
            sInstance.alertsVibrateWhen = v
        }
    }
}