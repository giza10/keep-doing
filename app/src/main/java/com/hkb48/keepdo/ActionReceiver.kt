package com.hkb48.keepdo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteException
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.db.entity.TaskCompletion
import com.hkb48.keepdo.util.DateChangeTimeUtil
import com.hkb48.keepdo.widget.TasksWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, Task.INVALID_TASKID)
        val today = DateChangeTimeUtil.dateTime

        CoroutineScope(Dispatchers.Main).launch {
            if (getDoneStatus(context, taskId, today).not()) {
                setDoneStatus(context, taskId, today)

                // Dismiss notification on wearable
                NotificationController.cancelReminder(context, taskId)

                // Dismiss notification on handheld
                NotificationController.cancelReminder(
                    context,
                    NotificationController.NOTIFICATION_ID_HANDHELD
                )

                // Update App widget
                TasksWidgetProvider.notifyDatasetChanged(context)

                // Update reminder alarm
                RemindAlarmInitReceiver.updateReminder(context, taskId)
            }
        }
    }

    private suspend fun getDoneStatus(context: Context, taskId: Int, date: Date): Boolean {
        val applicationContext = context.applicationContext
        return if (applicationContext is KeepdoApplication) {
            applicationContext.getDatabase().taskCompletionDao().getByDate(taskId, date).count() > 0
        } else {
            true
        }
    }

    private suspend fun setDoneStatus(context: Context, taskId: Int, date: Date) {
        val applicationContext = context.applicationContext
        if (applicationContext is KeepdoApplication) {
            try {
                val taskCompletion = TaskCompletion(0, taskId, date)
                applicationContext.getDatabase().taskCompletionDao().insert(taskCompletion)
            } catch (e: SQLiteException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private val PACKAGE_NAME = ActionReceiver::class.java.getPackage()!!.name
        val EXTRA_TASK_ID = "$PACKAGE_NAME.intent_extra_task_id"
    }

}