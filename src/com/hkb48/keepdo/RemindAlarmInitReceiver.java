package com.hkb48.keepdo;

import java.util.Date;
import java.util.List;

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

        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(context);

        List<Task> taskList = dbAdapter.getTaskList();
        for (Task task : taskList) {
            if (task.getReminder().getEnabled()) {
                boolean isDoneToday = dbAdapter.getDoneStatus(task.getTaskID(), new Date());
                reminderManager.register(context, task, isDoneToday);
            }
        }

        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            ;
        }
    }
}
