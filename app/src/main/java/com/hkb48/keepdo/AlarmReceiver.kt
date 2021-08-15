package com.hkb48.keepdo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.hkb48.keepdo.db.entity.Task
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    @Inject
    lateinit var repository: TaskRepository

    @Inject
    lateinit var reminderManager: ReminderManager

    @Inject
    lateinit var dateChangeTimeManager: DateChangeTimeManager

    @Inject
    lateinit var notificationController: NotificationController

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_REMINDER -> {
                dispatchReminderEvent(intent)
            }
            ACTION_DATE_CHANGED -> {
                dispatchDateChangedEvent()
            }
            else -> {
                Log.e(TAG_KEEPDO, "AlarmReceiver#onReceive(): Unknown intent type=" + intent.action)
            }
        }
    }

    private fun dispatchReminderEvent(intent: Intent) {
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, Task.INVALID_TASKID)
        CoroutineScope(Dispatchers.IO).launch {
            repository.getTask(taskId)?.also { task ->
                notificationController.showReminder(task)
            }
        }
        reminderManager.setAlarm(taskId)
    }

    private fun dispatchDateChangedEvent() {
        dateChangeTimeManager.dateChanged()
    }

    companion object {
        private val PACKAGE_NAME = AlarmReceiver::class.java.getPackage()!!.name
        val ACTION_REMINDER = "$PACKAGE_NAME.action.REMINDER"
        val ACTION_DATE_CHANGED = "$PACKAGE_NAME.action.DATE_CHANGED"
        const val EXTRA_TASK_ID = "TASK-ID"
        private const val TAG_KEEPDO = "#LOG_KEEPDO: "
    }
}