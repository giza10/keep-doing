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
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.ui.TasksActivity
import com.hkb48.keepdo.ui.settings.Settings
import com.hkb48.keepdo.util.CompatUtil
import javax.inject.Inject

class NotificationController @Inject constructor(
    private val context: Context,
    private val reminderManager: ReminderManager
) {
    suspend fun showReminder(task: Task) {
        val taskName = task.name

        val builder = NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setTicker(taskName)
            .setDefaults(Notification.DEFAULT_LIGHTS)
        Settings.alertsRingTone?.let {
            builder.setSound(Uri.parse(it))
        }
        val vibrateWhen = Settings.alertsVibrateWhen
        val vibrateAlways = vibrateWhen == "always"
        val vibrateSilent = vibrateWhen == "silent"
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        val nowSilent = audioManager?.ringerMode == AudioManager.RINGER_MODE_VIBRATE

        // Possibly generate a vibration
        if (vibrateAlways || vibrateSilent && nowSilent) {
            builder.setDefaults(Notification.DEFAULT_VIBRATE)
        }
        val launchIntent = Intent(context, TasksActivity::class.java)
        val launchPendingIntent = PendingIntent.getActivity(
            context, 0, launchIntent,
            CompatUtil.appendImmutableFlag(0)
        )
        builder.setContentIntent(launchPendingIntent)
        builder.setAutoCancel(true)

        // Notification for hand-held device
        val largeIcon = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.ic_launcher
        )
        builder.setLargeIcon(largeIcon)
        builder.setGroup(GROUP_KEY_REMINDERS)
        builder.setGroupSummary(true)
        val remainingTaskList = reminderManager.getRemainingUndoneTaskList()
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
            val taskId = task._id
            val actionIntent = Intent(context, ActionReceiver::class.java).apply {
                putExtra(ActionReceiver.EXTRA_TASK_ID, taskId)
            }
            val actionPendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                actionIntent,
                CompatUtil.appendImmutableFlag(PendingIntent.FLAG_CANCEL_CURRENT)
            )
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel() {
        if (CompatUtil.isNotificationChannelSupported().not()) {
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
        val vibrateWhen = Settings.alertsVibrateWhen
        val vibrateAlways = vibrateWhen == "always"
        val vibrateSilent = vibrateWhen == "silent"
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        val nowSilent = audioManager?.ringerMode == AudioManager.RINGER_MODE_VIBRATE

        // Possibly generate a vibration
        if (vibrateAlways || vibrateSilent && nowSilent) {
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        }
        Settings.alertsRingTone?.let {
            channel.setSound(
                Uri.parse(it), AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
            )
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.createNotificationChannel(channel)
    }

    fun cancelReminder() {
        NotificationManagerCompat.from(context).cancelAll()
    }

    fun cancelReminder(id: Int) {
        NotificationManagerCompat.from(context).cancel(id)
    }

    companion object {
        const val NOTIFICATION_ID_HANDHELD = -1
        private const val GROUP_KEY_REMINDERS = "group_key_reminders"

        @get:RequiresApi(Build.VERSION_CODES.O)
        val notificationChannelId = "Channel_ID"
    }
}