package com.hkb48.keepdo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class RemindAlarmReceiver extends BroadcastReceiver {
    private static final String TAG_KEEPDO = "#LOG_KEEPDO: ";

    @Override
    public void onReceive(Context context, Intent intent) {
        long taskId = intent.getLongExtra("TASK-ID", -1);
        Task task = DatabaseAdapter.getInstance(context).getTask(taskId);
        String taskName = null;
        if (task != null) {
            taskName = task.getName();
            Log.v(TAG_KEEPDO + "RemindAlarmReceiver#onReceive()", "taskId:" + taskId + ", taskName:" + taskName);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext());
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setTicker(taskName);
        builder.setContentTitle("ç°ì˙Ç‚ÇÈÉ^ÉXÉNÇ™Ç†ÇËÇ‹Ç∑Éà");
        builder.setContentText(taskName);
        builder.setDefaults(Notification.DEFAULT_LIGHTS);
        Intent newIntent = new Intent(context, TasksActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, newIntent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        notificationManager.notify(1, builder.build());

        ReminderManager.getInstance().setNextAlert(context);
    }
}
