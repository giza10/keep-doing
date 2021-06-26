package com.hkb48.keepdo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class RemindAlarmInitReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_UPDATE_REMINDER,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED
            -> ReminderManager.setAlarmForAll(context)
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

        fun updateReminder(context: Context) {
            context.sendBroadcast(
                Intent(context, RemindAlarmInitReceiver::class.java).apply {
                    action = ACTION_UPDATE_REMINDER
                }
            )
        }
    }
}