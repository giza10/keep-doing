package com.hkb48.keepdo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.util.DateChangeTimeUtil
import com.hkb48.keepdo.widget.TasksWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ActionReceiver : BroadcastReceiver() {
    @Inject
    lateinit var repository: TaskRepository

    @Inject
    lateinit var notificationController: NotificationController

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, Task.INVALID_TASKID)
        val today = DateChangeTimeUtil.dateTime

        CoroutineScope(Dispatchers.Main).launch {
            if (getDoneStatus(taskId, today).not()) {
                setDoneStatus(taskId, today)

                // Dismiss notification on wearable
                notificationController.cancelReminder(taskId)

                // Dismiss notification on handheld
                notificationController.cancelReminder(
                    NotificationController.NOTIFICATION_ID_HANDHELD
                )

                // Update App widget
                TasksWidgetProvider.notifyDatasetChanged(context)

                // Update reminder alarm
                RemindAlarmInitReceiver.updateReminder(context, taskId)
            }
        }
    }

    private suspend fun getDoneStatus(taskId: Int, date: Date): Boolean {
        return repository.getDoneStatus(taskId, date)
    }

    private suspend fun setDoneStatus(taskId: Int, date: Date) {
        repository.setDoneStatus(taskId, date, true)
    }

    companion object {
        private val PACKAGE_NAME = ActionReceiver::class.java.getPackage()!!.name
        val EXTRA_TASK_ID = "$PACKAGE_NAME.intent_extra_task_id"
    }

}