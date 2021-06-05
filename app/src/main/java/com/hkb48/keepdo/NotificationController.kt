package com.hkb48.keepdo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.hkb48.keepdo.settings.Settings.Companion.getAlertsRingTone
import com.hkb48.keepdo.settings.Settings.Companion.getAlertsVibrateWhen
import com.hkb48.keepdo.settings.Settings.Companion.initialize
import com.hkb48.keepdo.util.CompatUtil.isNotificationChannelSupported

object NotificationController {
    const val NOTIFICATION_ID_HANDHELD = -1
    private const val TAG_KEEPDO = "#LOG_KEEPDO: "
    private const val GROUP_KEY_REMINDERS = "group_key_reminders"

    @get:RequiresApi(Build.VERSION_CODES.O)
    val notificationChannelId = "Channel_ID"
    fun showReminder(context: Context, taskId: Long) {
        val task = DatabaseAdapter.getInstance(context).getTask(taskId)
        var taskName: String? = null
        if (task != null) {
            taskName = task.name
            if (BuildConfig.DEBUG) {
                Log.v(TAG_KEEPDO, "taskId:$taskId, taskName:$taskName")
            }
        }
        val builder = NotificationCompat.Builder(context, notificationChannelId)
        builder.setSmallIcon(R.drawable.ic_notification)
        builder.setTicker(taskName)
        builder.setDefaults(Notification.DEFAULT_LIGHTS)
        initialize(context.applicationContext)
        val reminderRingtone = getAlertsRingTone()
        if (reminderRingtone != null) {
            builder.setSound(Uri.parse(reminderRingtone))
        }
        val vibrateWhen = getAlertsVibrateWhen()
        val vibrateAlways = vibrateWhen == "always"
        val vibrateSilent = vibrateWhen == "silent"
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        val nowSilent = audioManager?.ringerMode == AudioManager.RINGER_MODE_VIBRATE

        // Possibly generate a vibration
        if (vibrateAlways || vibrateSilent && nowSilent) {
            builder.setDefaults(Notification.DEFAULT_VIBRATE)
        }
        val launchIntent = Intent(context, TasksActivity::class.java)
        val launchPendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0)
        builder.setContentIntent(launchPendingIntent)
        builder.setAutoCancel(true)
        val actionIntent = Intent(context, ActionHandler::class.java)
        actionIntent.putExtra(ActionHandler.INTENT_EXTRA_TASK_ID, taskId)
        val actionPendingIntent = PendingIntent.getService(
            context,
            taskId.toInt(),
            actionIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        // Notification for hand-held device
        val largeIcon = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.ic_launcher
        )
        builder.setLargeIcon(largeIcon)
        builder.setGroup(GROUP_KEY_REMINDERS)
        builder.setGroupSummary(true)
        val reminderManager = ReminderManager.instance
        val remainingTaskList = reminderManager.getRemainingUndoneTaskList(context)
        val numOfRemainingTasks = remainingTaskList.size
        if (numOfRemainingTasks > 1) {
            val inboxStyle = NotificationCompat.InboxStyle()
            inboxStyle.setBigContentTitle(
                context.getString(
                    R.string.notification_title_multi_tasks,
                    numOfRemainingTasks
                )
            )
            for (remainingTask in remainingTaskList) {
                inboxStyle.addLine(remainingTask.name)
            }
            builder.setContentTitle(
                context.getString(
                    R.string.notification_title_multi_tasks,
                    numOfRemainingTasks
                )
            )
            builder.setStyle(inboxStyle)
        } else {
            builder.setContentTitle(context.getString(R.string.notification_title_one_task))
            builder.setContentText(taskName)
            builder.addAction(
                R.drawable.ic_notification_check,
                context.getText(R.string.check_done),
                actionPendingIntent
            )
        }
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_HANDHELD, builder.build())
    }

    @JvmStatic
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(context: Context) {
        if (!isNotificationChannelSupported) {
            return
        }

        // The user-visible name of the channel.
        val name: CharSequence = context.getString(R.string.channel_name)

        // The user-visible description of the channel.
        val description = context.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(notificationChannelId, name, importance)
        // Configure the notification channel.
        channel.description = description
        channel.enableLights(true)
        // Sets the notification light color for notifications posted to this
        // channel, if the device supports this feature.
        channel.lightColor = Color.WHITE
        initialize(context.applicationContext)
        val vibrateWhen = getAlertsVibrateWhen()
        val vibrateAlways = vibrateWhen == "always"
        val vibrateSilent = vibrateWhen == "silent"
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        val nowSilent = audioManager?.ringerMode == AudioManager.RINGER_MODE_VIBRATE

        // Possibly generate a vibration
        if (vibrateAlways || vibrateSilent && nowSilent) {
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        }
        val reminderRingtone = getAlertsRingTone()
        if (reminderRingtone != null) {
            channel.setSound(
                Uri.parse(reminderRingtone), AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
            )
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.createNotificationChannel(channel)
    }

    fun cancelReminder(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }

    fun cancelReminder(context: Context, id: Long) {
        val notificationId = id.toInt()
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}