package com.hkb48.keepdo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hkb48.keepdo.widget.TasksWidgetProvider

class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, TaskInfo.INVALID_TASKID)
        DatabaseAdapter.getInstance(context)
            .setDoneStatus(taskId, DateChangeTimeUtil.dateTime, true)

        // Dismiss notification on wearable
        NotificationController.cancelReminder(context, taskId)

        // Dismiss notification on handheld
        NotificationController.cancelReminder(
            context,
            NotificationController.NOTIFICATION_ID_HANDHELD
        )

        // Update App widget
        TasksWidgetProvider.notifyDatasetChanged(context)
    }

    companion object {
        private val PACKAGE_NAME = ActionReceiver::class.java.getPackage()!!.name
        val EXTRA_TASK_ID = "$PACKAGE_NAME.intent_extra_task_id"
    }

}