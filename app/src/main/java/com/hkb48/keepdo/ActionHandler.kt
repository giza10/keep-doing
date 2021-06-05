package com.hkb48.keepdo

import android.app.IntentService
import android.content.ContentValues
import android.content.Intent
import com.hkb48.keepdo.KeepdoProvider.TaskCompletion
import com.hkb48.keepdo.widget.TasksWidgetProvider.Companion.notifyDatasetChanged

class ActionHandler : IntentService(SERVICE_NAME) {
    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val taskId = intent.getLongExtra(INTENT_EXTRA_TASK_ID, 0)
            val contentValues = ContentValues()
            contentValues.put(TaskCompletion.TASK_NAME_ID, taskId)
            contentValues.put(TaskCompletion.TASK_COMPLETION_DATE, todayDate)
            contentResolver.insert(TaskCompletion.CONTENT_URI, contentValues)

            // Dismiss notification on wearable
            NotificationController.cancelReminder(this, taskId)

            // Dismiss notification on handheld
            NotificationController.cancelReminder(
                this,
                NotificationController.NOTIFICATION_ID_HANDHELD.toLong()
            )

            // Update App widget
            notifyDatasetChanged(applicationContext)
        }
    }

    private val todayDate: String
        get() {
            val dbAdapter = DatabaseAdapter.getInstance(applicationContext)
            return dbAdapter.todayDate
        }

    companion object {
        private const val SERVICE_NAME = "ActionHandler"
        private val PACKAGE_NAME = ActionHandler::class.java.getPackage()!!.name

        @JvmField
        val INTENT_EXTRA_TASK_ID = "$PACKAGE_NAME.intent_extra_task_id"
    }
}