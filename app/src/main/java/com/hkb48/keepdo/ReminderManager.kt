package com.hkb48.keepdo

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.text.MessageFormat
import java.util.*

object ReminderManager {
    fun setAlarmForAll(context: Context) {
        val dbAdapter = DatabaseAdapter.getInstance(context)
        val taskList = dbAdapter.taskList
        for (task in taskList) {
            setAlarm(context, task.taskID)
        }
    }

    fun setAlarm(context: Context, taskId: Long) {
        val dbAdapter = DatabaseAdapter.getInstance(context)
        val task = dbAdapter.getTask(taskId)
        if (task != null) {
            val reminder = task.reminder
            if (reminder.enabled) {
                val today = DateChangeTimeUtil.dateTime
                val isDoneToday = dbAdapter.getDoneStatus(taskId, today)
                val hourOfDay = reminder.hourOfDay
                val minute = reminder.minute
                val nextSchedule = getNextSchedule(
                    task.recurrence,
                    isDoneToday, hourOfDay, minute
                )
                if (nextSchedule != null) {
                    startAlarm(context, taskId, nextSchedule.timeInMillis)
                } else {
                    stopAlarm(context, taskId)
                }
            } else {
                stopAlarm(context, taskId)
            }
        }
    }

    fun cancelAlarm(context: Context, taskId: Long) {
        val task = DatabaseAdapter.getInstance(context).getTask(taskId)
        if (task?.reminder?.enabled == true) {
            stopAlarm(context, taskId)
        }
    }

    fun getRemainingUndoneTaskList(context: Context): List<Task> {
        val dbAdapter = DatabaseAdapter.getInstance(context)
        val remainingList: MutableList<Task> = ArrayList()
        val realTime = Calendar.getInstance()
        realTime.timeInMillis = System.currentTimeMillis()
        val today = DateChangeTimeUtil.getDateTimeCalendar(realTime).time
        val dayOfWeek = DateChangeTimeUtil.getDateTimeCalendar(realTime)[Calendar.DAY_OF_WEEK]

        // Add 1 minute to avoid that the next alarm is set to current time
        // again.
        realTime.add(Calendar.MINUTE, 1)
        for (task in dbAdapter.taskList) {
            val reminder = task.reminder
            if (reminder.enabled && task.recurrence.isValidDay(dayOfWeek)) {
                val hourOfDay = reminder.hourOfDay
                val minute = reminder.minute
                val reminderTime = Calendar.getInstance()
                reminderTime.timeInMillis = System.currentTimeMillis()
                reminderTime[Calendar.HOUR_OF_DAY] = hourOfDay
                reminderTime[Calendar.MINUTE] = minute

                // Check if today's reminder time is already exceeded
                if (hasReminderAlreadyExceeded(realTime, reminderTime)) {
                    if (!dbAdapter.getDoneStatus(task.taskID, today)) {
                        remainingList.add(task)
                    }
                }
            }
        }
        return remainingList
    }

    private fun getNextSchedule(
        recurrence: Recurrence,
        isDoneToday: Boolean, hourOfDay: Int, minute: Int
    ): Calendar? {
        val realTime = Calendar.getInstance()
        realTime.timeInMillis = System.currentTimeMillis()
        var dayOffset = 0
        var isSuccess = false
        val recurrenceFlags = booleanArrayOf(
            recurrence.sunday,
            recurrence.monday, recurrence.tuesday,
            recurrence.wednesday, recurrence.thursday,
            recurrence.friday, recurrence.saturday
        )
        var dayOfWeek = DateChangeTimeUtil.getDateTimeCalendar(realTime)[Calendar.DAY_OF_WEEK] - 1
        val reminderTime = Calendar.getInstance()
        reminderTime.timeInMillis = System.currentTimeMillis()
        reminderTime[Calendar.HOUR_OF_DAY] = hourOfDay
        reminderTime[Calendar.MINUTE] = minute
        val isRealTimeDateAdjusted = DateChangeTimeUtil.isDateAdjusted(realTime)
        val isReminderTimeDateAdjusted = DateChangeTimeUtil.isDateAdjusted(reminderTime)

        // Add 1 minute to avoid that the next alarm is set to current time
        // again.
        realTime.add(Calendar.MINUTE, 1)
        val todayAlreadyExceeded = hasReminderAlreadyExceeded(
            realTime, reminderTime
        )
        if (isRealTimeDateAdjusted) {
            if (isReminderTimeDateAdjusted) {
                if (isDoneToday || todayAlreadyExceeded) {
                    // Next alarm will be tomorrow onward
                    dayOffset++
                }
            }
        } else {
            if (isReminderTimeDateAdjusted) {
                // Next alarm will be tomorrow onward
                dayOffset++
                if (isDoneToday) {
                    // Next alarm will be day after tomorrow onward
                    dayOffset++
                }
            } else {
                if (isDoneToday || todayAlreadyExceeded) {
                    // Next alarm will be tomorrow onward
                    dayOffset++
                }
            }
        }
        dayOfWeek = (dayOfWeek + dayOffset) % 7
        for (counter in 0..6) {
            if (recurrenceFlags[(dayOfWeek + counter) % 7]) {
                dayOffset += counter
                isSuccess = true
                break
            }
        }
        if (!isSuccess) {
            return null
        }
        realTime.add(Calendar.DAY_OF_MONTH, dayOffset)
        realTime[Calendar.HOUR_OF_DAY] = hourOfDay
        realTime[Calendar.MINUTE] = minute
        realTime[Calendar.SECOND] = 0
        realTime[Calendar.MILLISECOND] = 0
        return realTime
    }

    private fun hasReminderAlreadyExceeded(
        realTime: Calendar,
        reminderTime: Calendar
    ): Boolean {
        val isRealTimeDateAdjusted = DateChangeTimeUtil.isDateAdjusted(realTime)
        val isReminderTimeDateAdjusted = DateChangeTimeUtil.isDateAdjusted(reminderTime)
        return if (isRealTimeDateAdjusted && !isReminderTimeDateAdjusted) {
            true
        } else if (!isRealTimeDateAdjusted && isReminderTimeDateAdjusted) {
            false
        } else {
            realTime.after(reminderTime)
        }
    }

    private fun startAlarm(context: Context, taskId: Long, timeInMillis: Long) {
        val alarmManager = context
            .getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        alarmManager?.set(AlarmManager.RTC_WAKEUP, timeInMillis, getPendingIntent(context, taskId))
        dumpLog(taskId, timeInMillis)
    }

    private fun stopAlarm(context: Context, taskId: Long) {
        val alarmManager = context
            .getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        alarmManager?.cancel(getPendingIntent(context, taskId))
    }

    private fun getPendingIntent(context: Context, taskId: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_REMINDER
            putExtra(AlarmReceiver.PARAM_TASK_ID, taskId)
        }
        return PendingIntent.getBroadcast(
            context, taskId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun dumpLog(taskId: Long, timeInMillis: Long) {
        if (BuildConfig.DEBUG) {
            val time = Calendar.getInstance()
            time.timeInMillis = timeInMillis
            val mf = MessageFormat("{0,date,yyyy/MM/dd HH:mm:ss}")
            val objs = arrayOf<Any>(time.time)
            val result = mf.format(objs)
            Log.v(TAG_KEEPDO, "ReminderManager: taskId=$taskId, time=$result")
        }
    }

    private const val TAG_KEEPDO = "#LOG_KEEPDO: "

}