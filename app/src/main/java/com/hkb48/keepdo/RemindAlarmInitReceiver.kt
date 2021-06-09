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
            ReminderManager.instance.setAlarmForAll(context)
        }
    }

    companion object {
        private const val ACTION_UPDATE_REMINDER = "com.hkb48.keepdo.action.UPDATE_REMINDER"
        fun updateReminder(context: Context) {
            val intent = Intent(context, RemindAlarmInitReceiver::class.java)
            intent.action = ACTION_UPDATE_REMINDER
            context.sendBroadcast(intent)
        }
    }
}