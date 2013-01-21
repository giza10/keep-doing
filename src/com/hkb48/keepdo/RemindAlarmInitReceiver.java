package com.hkb48.keepdo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RemindAlarmInitReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.v("KEEP_DO", "RemindAlarmInitReceiver");

        ReminderManager reminderManager = ReminderManager.getInstance();
        reminderManager.setNextAlert(context);

        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            ;
        }
    }
}
