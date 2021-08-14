package com.hkb48.keepdo.util

import com.hkb48.keepdo.ui.settings.Settings
import java.util.*

object DateChangeTimeUtil {
    val dateTime: Date
        get() = dateTimeCalendar.time

    val date: Date
        get() {
            val calendar = dateTimeCalendar
            calendar[Calendar.HOUR_OF_DAY] = 0
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0
            return calendar.time
        }

    val dateTimeCalendar: Calendar
        get() {
            val realTime = Calendar.getInstance()
            realTime.timeInMillis = System.currentTimeMillis()
            return getDateTimeCalendar(realTime)
        }

    fun getDateTimeCalendar(realTime: Calendar): Calendar {
        val dateChangeTime = dateChangeTime
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = realTime.timeInMillis
        calendar.add(Calendar.HOUR_OF_DAY, -dateChangeTime.hourOfDay)
        calendar.add(Calendar.MINUTE, -dateChangeTime.minute)
        return calendar
    }

    val dateChangeTime: DateChangeTime
        get() {
            val dateChangeTimeStr = Settings.dateChangeTime
            val timeHourMin = dateChangeTimeStr!!.split(":").toTypedArray()
            val dateChangeTime = DateChangeTime()
            dateChangeTime.hourOfDay = timeHourMin[0].toInt() - 24
            dateChangeTime.minute = timeHourMin[1].toInt()
            return dateChangeTime
        }

    fun isDateAdjusted(realTime: Calendar): Boolean {
        val dateChangeTime = getDateTimeCalendar(realTime)
        return realTime[Calendar.DAY_OF_MONTH] > dateChangeTime[Calendar.DAY_OF_MONTH]
    }

    class DateChangeTime {
        var hourOfDay = 0
        var minute = 0
    }
}