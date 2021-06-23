package com.hkb48.keepdo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RemindAlarmInitReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (ACTION_UPDATE_REMINDER == action ||
            Intent.ACTION_BOOT_COMPLETED.equals(
                action,
                ignoreCase = true
            ) || Intent.ACTION_TIME_CHANGED == action || Intent.ACTION_TIMEZONE_CHANGED == action || Intent.ACTION_LOCALE_CHANGED == action
        ) {
            ReminderManager.setAlarmForAll(context)
        }
    }

    companion object {
        private val PACKAGE_NAME = RemindAlarmInitReceiver::class.java.getPackage()!!.name
        private val ACTION_UPDATE_REMINDER = "$PACKAGE_NAME.action.UPDATE_REMINDER"
        fun updateReminder(context: Context) {
            context.sendBroadcast(
                Intent(context, RemindAlarmInitReceiver::class.java).apply {
                    action = ACTION_UPDATE_REMINDER
                }
            )
        }
    }
}