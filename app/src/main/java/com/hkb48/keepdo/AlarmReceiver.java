package com.hkb48.keepdo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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

        NotificationController.showReminder(context, taskId);

        ReminderManager.getInstance().setNextAlert(context);
    }

    private void dispatchDateChangedEvent(Context context) {
        DateChangeTimeManager.getInstance(context).dateChanged();
    }
}
