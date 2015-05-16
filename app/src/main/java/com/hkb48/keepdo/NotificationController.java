package com.hkb48.keepdo;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.List;

class NotificationController {
    private static final String TAG_KEEPDO = "#LOG_KEEPDO: ";
    private static final String GROUP_KEY_REMINDERS = "group_key_reminders";
    public static final int NOTIFICATION_ID_HANDHELD = -1;

    public static void showReminder(final Context context, long taskId) {
        Task task = DatabaseAdapter.getInstance(context).getTask(taskId);
        String taskName = null;
        if (task != null) {
            taskName = task.getName();
            if (BuildConfig.DEBUG) {
                Log.v(TAG_KEEPDO, "taskId:" + taskId + ", taskName:" + taskName);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
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
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        boolean nowSilent = audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE;

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
        PendingIntent actionPendingIntent = PendingIntent.getService(context, (int)taskId, actionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        
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

        // Notification for wearable
        Notification wearableNotification = new NotificationCompat.Builder(context)
            .setContentTitle(context.getString(R.string.notification_title_one_task))
            .setContentText(taskName)
            .setContentIntent(launchPendingIntent)
            .setSmallIcon(R.drawable.ic_launcher)
            .addAction(R.drawable.ic_notification_check, context.getText(R.string.check_done), actionPendingIntent)
            .setGroup(GROUP_KEY_REMINDERS)
            .build();

        NotificationManagerCompat.from(context).notify((int)taskId, wearableNotification);
    }

    public static void cancelReminder(final Context context) {
        NotificationManagerCompat.from(context).cancelAll();
    }

    public static void cancelReminder(final Context context, long id) {
        int notificationId = (int) id;
        NotificationManagerCompat.from(context).cancel(notificationId);
    }
}
