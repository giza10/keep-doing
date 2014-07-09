package com.hkb48.keepdo;

import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class NotificationHelper {
    private static final String TAG_KEEPDO = "#LOG_KEEPDO: ";

    public static void showReminder(final Context context, long taskId) {
        Task task = DatabaseAdapter.getInstance(context).getTask(taskId);
        String taskName = null;
        if (task != null) {
            taskName = task.getName();
            if (BuildConfig.DEBUG) {
                Log.v(TAG_KEEPDO + "RemindAlarmReceiver#onReceive()", "taskId:" + taskId + ", taskName:" + taskName);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext());
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setTicker(taskName);

        ReminderManager reminderManager = ReminderManager.getInstance();
        List<Task> remainingTaskList = reminderManager.getRemainingUndoneTaskList(context);
        int numOfReminingTasks = remainingTaskList.size();

        if (numOfReminingTasks > 1) {
            builder.setContentTitle(context.getString(R.string.notification_title_multi_tasks, numOfReminingTasks));
            StringBuilder stringBuilder = new StringBuilder();
            for (Task remainingTask : remainingTaskList) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(remainingTask.getName());
            }
            builder.setContentText(stringBuilder.toString());
        } else {
            builder.setContentTitle(context.getString(R.string.notification_title_one_task));
            builder.setContentText(taskName);
            Intent actionIntent = new Intent(context, KeepdoActionService.class);
            actionIntent.putExtra(KeepdoActionService.INTENT_EXTRA_MESSAGE_ID, taskId);
            PendingIntent pendingintent = PendingIntent.getService(context, (int)taskId, actionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            builder.addAction(R.drawable.ic_not_done_2, context.getText(R.string.check_done), pendingintent);
        }
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
        Intent newIntent = new Intent(context, TasksActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, newIntent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        notificationManager.notify(R.string.app_name, builder.build());
    }

    public static void cancelReminder(final Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(R.string.app_name);
    }
}
