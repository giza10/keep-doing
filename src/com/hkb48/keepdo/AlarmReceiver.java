package com.hkb48.keepdo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG_KEEPDO = "#LOG_KEEPDO: ";
    public static final String ACTION_REMINDER = "com.hkb48.keepdo.action.REMINDER";
    public static final String ACTION_DATE_CHANGED = "com.hkb48.keepdo.action.DATE_CHANGED";
    public static final String PARAM_TASK_ID = "TASK-ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getType().equals(ACTION_REMINDER)) {
            dispatchReminderEvent(context, intent);
        } else if (intent.getType().equals(ACTION_DATE_CHANGED)) {
            dispatchDateChangedEvent(context);
        } else {
            Log.e(TAG_KEEPDO + "RemindAlarmReceiver#onReceive()", "Unknown intent type");
        }
    }

    private void dispatchReminderEvent(Context context, Intent intent) {
        long taskId = intent.getLongExtra(PARAM_TASK_ID, -1);
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
            StringBuilder stringBugger = new StringBuilder();
            for (Task remainingTask : remainingTaskList) {
                if (stringBugger.length() > 0) {
                    stringBugger.append(", ");
                }
                stringBugger.append(remainingTask.getName());
            }
            builder.setContentText(stringBugger.toString());
        } else {
            builder.setContentTitle(context.getString(R.string.notification_title_one_task));
            builder.setContentText(taskName);
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

        reminderManager.setNextAlert(context);
    }

    private void dispatchDateChangedEvent(Context context) {
        DateChangeTimeManager.getInstance(context).dateChanged();
    }
}
