package com.hkb48.keepdo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.hkb48.keepdo.db.entity.Task
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RemindAlarmInitReceiver : BroadcastReceiver() {
    @Inject
    lateinit var reminderManager: ReminderManager

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_UPDATE_REMINDER -> {
                reminderManager.setAlarm(
                    intent.getIntExtra(EXTRA_TASK_ID, Task.INVALID_TASKID)
                )
            }
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED
            -> reminderManager.setAlarmForAll()
            else -> {
                Log.e(
                    TAG_KEEPDO,
                    "RemindAlarmInitReceiver#onReceive(): Unknown intent type=" + intent.action
                )
            }
        }
    }

    companion object {
        private val PACKAGE_NAME = RemindAlarmInitReceiver::class.java.getPackage()!!.name
        private val ACTION_UPDATE_REMINDER = "$PACKAGE_NAME.action.UPDATE_REMINDER"
        private const val TAG_KEEPDO = "#LOG_KEEPDO: "
        val EXTRA_TASK_ID = "$PACKAGE_NAME.intent_extra_task_id"

        fun updateReminder(context: Context, taskId: Int) {
            context.sendBroadcast(
                Intent(context, RemindAlarmInitReceiver::class.java).apply {
                    action = ACTION_UPDATE_REMINDER
                    putExtra(EXTRA_TASK_ID, taskId)
                }
            )
        }
    }
}