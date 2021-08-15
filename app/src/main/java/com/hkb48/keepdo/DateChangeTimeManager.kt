package com.hkb48.keepdo

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.hkb48.keepdo.ui.settings.Settings
import com.hkb48.keepdo.util.DateChangeTimeUtil
import com.hkb48.keepdo.widget.TasksWidgetProvider
import java.text.MessageFormat
import java.util.*
import javax.inject.Inject

class DateChangeTimeManager @Inject constructor(
    private val context: Context,
    private val reminderManager: ReminderManager
) {
    private val changedListeners: MutableList<OnDateChangedListener> = ArrayList()
    private val settingsChangedListener: Settings.OnChangedListener =
        object : Settings.OnChangedListener {
            override fun onDoneIconSettingChanged() {}
            override fun onDateChangeTimeSettingChanged() {
                if (changedListeners.size > 0) {
                    startAlarm()
                }
                reminderManager.setAlarmForAll()
                TasksWidgetProvider.notifyDatasetChanged(context)
            }

            override fun onWeekStartDaySettingChanged() {}
        }

    fun registerOnDateChangedListener(listener: OnDateChangedListener?) {
        if (listener != null && !changedListeners.contains(listener)) {
            changedListeners.add(listener)
            if (changedListeners.size == 1) {
                Settings.registerOnChangedListener(settingsChangedListener)
                startAlarm()
            }
        }
    }

    fun unregisterOnDateChangedListener(listener: OnDateChangedListener?) {
        if (listener != null) {
            changedListeners.remove(listener)
            if (changedListeners.size < 1) {
                Settings.unregisterOnChangedListener(settingsChangedListener)
                stopAlarm()
            }
        }
    }

    fun dateChanged() {
        for (listener in changedListeners) {
            listener.onDateChanged()
        }
    }

    private fun startAlarm() {
        val dateChangeTime = DateChangeTimeUtil.dateChangeTime
        val nextAlarmTime = DateChangeTimeUtil.dateTimeCalendar
        nextAlarmTime.add(Calendar.DATE, 1)
        nextAlarmTime[Calendar.HOUR_OF_DAY] = dateChangeTime.hourOfDay
        nextAlarmTime[Calendar.MINUTE] = dateChangeTime.minute
        nextAlarmTime[Calendar.SECOND] = 0
        nextAlarmTime[Calendar.MILLISECOND] = 0
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        alarmManager?.setRepeating(
            AlarmManager.RTC,
            nextAlarmTime.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            getPendingIntent(context)
        )
        dumpLog(nextAlarmTime.timeInMillis)
    }

    private fun stopAlarm() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        alarmManager?.cancel(getPendingIntent(context))
    }

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_DATE_CHANGED
        }
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
    }
}