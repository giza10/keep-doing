package com.hkb48.keepdo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.hkb48.keepdo.settings.Settings;
import com.hkb48.keepdo.util.CompatUtil;

import java.util.List;

public class NotificationController {
    static final int NOTIFICATION_ID_HANDHELD = -1;
    private static final String TAG_KEEPDO = "#LOG_KEEPDO: ";
    private static final String GROUP_KEY_REMINDERS = "group_key_reminders";
    private static final String CHANNEL_ID = "Channel_ID";

    static void showReminder(final Context context, long taskId) {
        Task task = DatabaseAdapter.getInstance(context).getTask(taskId);
        String taskName = null;
        if (task != null) {
            taskName = task.getName();
            if (BuildConfig.DEBUG) {
                Log.v(TAG_KEEPDO, "taskId:" + taskId + ", taskName:" + taskName);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setTicker(taskName);
        builder.setDefaults(Notification.DEFAULT_LIGHTS);

        Settings.initialize(context.getApplicationContext());
        String reminderRingtone = Settings.getAlertsRingTone();
        if (reminderRingtone != null) {
            builder.setSound(Uri.parse(reminderRingtone));
        }
        String vibrateWhen = Settings.getAlertsVibrateWhen();
        boolean vibrateAlways = vibrateWhen.equals("always");
        boolean vibrateSilent = vibrateWhen.equals("silent");
        boolean nowSilent = false;
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            nowSilent = audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE;
        }

        // Possibly generate a vibration
        if (vibrateAlways || (vibrateSilent && nowSilent)) {
            builder.setDefaults(Notification.DEFAULT_VIBRATE);
        }
        Intent launchIntent = new Intent(context, TasksActivity.class);
        PendingIntent launchPendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);
        builder.setContentIntent(launchPendingIntent);
        builder.setAutoCancel(true);

        Intent actionIntent = new Intent(context, ActionHandler.class);
        actionIntent.putExtra(ActionHandler.INTENT_EXTRA_TASK_ID, taskId);
        PendingIntent actionPendingIntent = PendingIntent.getService(context, (int) taskId, actionIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Notification for hand-held device
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_launcher);
        builder.setLargeIcon(largeIcon);
        builder.setGroup(GROUP_KEY_REMINDERS);
        builder.setGroupSummary(true);

        ReminderManager reminderManager = ReminderManager.getInstance();
        List<Task> remainingTaskList = reminderManager.getRemainingUndoneTaskList(context);
        int numOfReminingTasks = remainingTaskList.size();

        if (numOfReminingTasks > 1) {
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.setBigContentTitle(context.getString(R.string.notification_title_multi_tasks, numOfReminingTasks));
            for (Task remainingTask : remainingTaskList) {
                inboxStyle.addLine(remainingTask.getName());
            }

            builder.setContentTitle(context.getString(R.string.notification_title_multi_tasks, numOfReminingTasks));
            builder.setStyle(inboxStyle);
        } else {
            builder.setContentTitle(context.getString(R.string.notification_title_one_task));
            builder.setContentText(taskName);
            builder.addAction(R.drawable.ic_notification_check, context.getText(R.string.check_done), actionPendingIntent);
        }

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_HANDHELD, builder.build());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    static void createNotificationChannel(final Context context) {
        if (!CompatUtil.isNotificationChannelSupported()) {
            return;
        }

        // The user-visible name of the channel.
        CharSequence name = context.getString(R.string.channel_name);

        // The user-visible description of the channel.
        String description = context.getString(R.string.channel_description);

        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        // Sets the notification light color for notifications posted to this
        // channel, if the device supports this feature.
        mChannel.setLightColor(Color.WHITE);
        Settings.initialize(context.getApplicationContext());
        String vibrateWhen = Settings.getAlertsVibrateWhen();
        boolean vibrateAlways = vibrateWhen.equals("always");
        boolean vibrateSilent = vibrateWhen.equals("silent");
        boolean nowSilent = false;
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            nowSilent = audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE;
        }

        // Possibly generate a vibration
        if (vibrateAlways || (vibrateSilent && nowSilent)) {
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        }

        String reminderRingtone = Settings.getAlertsRingTone();
        if (reminderRingtone != null) {
            mChannel.setSound(Uri.parse(reminderRingtone), new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(mChannel);
        }
    }

    static void cancelReminder(final Context context) {
        NotificationManagerCompat.from(context).cancelAll();
    }

    static void cancelReminder(final Context context, long id) {
        int notificationId = (int) id;
        NotificationManagerCompat.from(context).cancel(notificationId);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public static String getNotificationChannelId() {
        return CHANNEL_ID;
    }
}
