package com.hkb48.keepdo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RemindAlarmInitReceiver extends BroadcastReceiver {
    private static final String ACTION_UPDATE_REMINDER = "com.hkb48.keepdo.action.UPDATE_REMINDER";

    @Override
    public void onReceive(Context context, Intent intent) {
        Settings.initialize(context.getApplicationContext());
        ReminderManager.getInstance().setNextAlert(context);
    }

    public static void updateReminder(final Context context) {
        final Intent intent = new Intent(context, RemindAlarmInitReceiver.class);
        intent.setAction(ACTION_UPDATE_REMINDER);
        context.sendBroadcast(intent);
    }
}
