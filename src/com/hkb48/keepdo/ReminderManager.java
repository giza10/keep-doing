package com.hkb48.keepdo;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class ReminderManager {
    private Context mContext;
    private AlarmManager mAlarmManager;
    Map<Long, Task> mTaskMap = new HashMap<Long, Task>();

    public ReminderManager(Context context) {
        mContext = context;
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void register(Task task) {
        long taskId = task.getTaskID();
        mTaskMap.put(taskId, task);
        Reminder reminder = task.getReminder();
        startAlarm(taskId, reminder.getHourOfDay(), reminder.getMinute());
    }

    public void unregister(long taskId) {
        stopAlarm(taskId);
        mTaskMap.remove(taskId);
    }

    private void startAlarm(long taskId, int hourOfDay, int minute) {
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(System.currentTimeMillis());
        time.set(Calendar.HOUR_OF_DAY, hourOfDay);
        time.set(Calendar.MINUTE, minute);
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);

        mAlarmManager.set(AlarmManager.RTC_WAKEUP,
                time.getTimeInMillis(),
                getPendingIntent(taskId));
    }

    private void stopAlarm(long taskId) {
        mAlarmManager.cancel(getPendingIntent(taskId));
    }

    private PendingIntent getPendingIntent(long taskId) {
        Task task = mTaskMap.get(taskId);
        Intent intent = new Intent(mContext, RemindAlarmReceiver.class);
        intent.putExtra("TASK-INFO", task);
        PendingIntent pendingIntent = 
                PendingIntent.getBroadcast(mContext, 0, intent, 0);
        return pendingIntent;
    }
}