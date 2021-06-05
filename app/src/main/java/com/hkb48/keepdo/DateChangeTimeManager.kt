package com.hkb48.keepdo

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.hkb48.keepdo.DateChangeTimeUtil.dateChangeTime
import com.hkb48.keepdo.DateChangeTimeUtil.dateTimeCalendar
import com.hkb48.keepdo.settings.Settings.Companion.registerOnChangedListener
import com.hkb48.keepdo.settings.Settings.Companion.unregisterOnChangedListener
import com.hkb48.keepdo.settings.Settings.OnChangedListener
import com.hkb48.keepdo.widget.TasksWidgetProvider.Companion.notifyDatasetChanged
import java.text.MessageFormat
import java.util.*

class DateChangeTimeManager private constructor(private val mContext: Context) {
    private val mChangedListeners: MutableList<OnDateChangedListener> = ArrayList()
    private val mSettingsChangedListener: OnChangedListener = object : OnChangedListener {
        override fun onDoneIconSettingChanged() {}
        override fun onDateChangeTimeSettingChanged() {
            startAlarm()
            ReminderManager.instance.setAlarmForAll(mContext)
            mContext.contentResolver.notifyChange(KeepdoProvider.DateChangeTime.CONTENT_URI, null)
            notifyDatasetChanged(mContext)
        }

        override fun onWeekStartDaySettingChanged() {}
    }

    fun registerOnDateChangedListener(listener: OnDateChangedListener?) {
        if (listener != null && !mChangedListeners.contains(listener)) {
            mChangedListeners.add(listener)
            if (mChangedListeners.size == 1) {
                registerOnChangedListener(mSettingsChangedListener)
                startAlarm()
            }
        }
    }

    fun unregisterOnDateChangedListener(listener: OnDateChangedListener?) {
        if (listener != null) {
            mChangedListeners.remove(listener)
            if (mChangedListeners.size < 1) {
                unregisterOnChangedListener(mSettingsChangedListener)
                stopAlarm()
            }
        }
    }

    fun dateChanged() {
        for (listener in mChangedListeners) {
            listener.onDateChanged()
        }
    }

    private fun startAlarm() {
        val dateChangeTime = dateChangeTime
        val nextAlarmTime = dateTimeCalendar
        nextAlarmTime.add(Calendar.DATE, 1)
        nextAlarmTime[Calendar.HOUR_OF_DAY] = dateChangeTime.hourOfDay
        nextAlarmTime[Calendar.MINUTE] = dateChangeTime.minute
        nextAlarmTime[Calendar.SECOND] = 0
        nextAlarmTime[Calendar.MILLISECOND] = 0
        val alarmManager = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        alarmManager?.setRepeating(
            AlarmManager.RTC,
            nextAlarmTime.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            getPendingIntent(mContext)
        )
        dumpLog(nextAlarmTime.timeInMillis)
    }

    private fun stopAlarm() {
        val alarmManager = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        alarmManager?.cancel(getPendingIntent(mContext))
    }

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = AlarmReceiver.ACTION_DATE_CHANGED
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun dumpLog(timeInMillis: Long) {
        if (BuildConfig.DEBUG) {
            val time = Calendar.getInstance()
            time.timeInMillis = timeInMillis
            val mf = MessageFormat("{0,date,yyyy/MM/dd HH:mm:ss}")
            val objs = arrayOf<Any>(time.time)
            val result = mf.format(objs)
            Log.v(TAG_KEEPDO, "DateChangeTimeManager: time=$result")
        }
    }

    interface OnDateChangedListener {
        fun onDateChanged()
    }

    companion object {
        private const val TAG_KEEPDO = "#LOG_KEEPDO: "
        private var sInstance: DateChangeTimeManager? = null

        @JvmStatic
        fun getInstance(context: Context): DateChangeTimeManager {
            if (sInstance == null) {
                sInstance = DateChangeTimeManager(context)
            }
            return sInstance as DateChangeTimeManager
        }
    }
}