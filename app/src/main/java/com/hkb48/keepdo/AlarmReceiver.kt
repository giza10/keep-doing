package com.hkb48.keepdo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.hkb48.keepdo.db.entity.Task

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_REMINDER -> {
                dispatchReminderEvent(context, intent)
            }
            ACTION_DATE_CHANGED -> {
                dispatchDateChangedEvent(context)
            }
            else -> {
                Log.e(TAG_KEEPDO, "AlarmReceiver#onReceive(): Unknown intent type=" + intent.action)
            }
        }
    }

    private fun dispatchReminderEvent(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, Task.INVALID_TASKID)
        NotificationController.showReminder(context, taskId)
        ReminderManager.setAlarm(context, taskId)
    }

    private fun dispatchDateChangedEvent(context: Context) {
        val applicationContext = context.applicationContext
        if (applicationContext is KeepdoApplication) {
            applicationContext.mDateChangeTimeManager.dateChanged()
        }
    }

    companion object {
        private val PACKAGE_NAME = AlarmReceiver::class.java.getPackage()!!.name
        val ACTION_REMINDER = "$PACKAGE_NAME.action.REMINDER"
        val ACTION_DATE_CHANGED = "$PACKAGE_NAME.action.DATE_CHANGED"
        const val EXTRA_TASK_ID = "TASK-ID"
        private const val TAG_KEEPDO = "#LOG_KEEPDO: "
    }
}