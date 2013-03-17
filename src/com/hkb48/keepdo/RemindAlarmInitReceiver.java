package com.hkb48.keepdo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RemindAlarmInitReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Settings.initialize(context.getApplicationContext());
        ReminderManager.getInstance().setNextAlert(context);
    }
}
