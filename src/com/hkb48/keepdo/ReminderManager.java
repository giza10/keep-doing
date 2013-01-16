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
    private static Map<Long, Task> sTaskMap = new HashMap<Long, Task>();

    public ReminderManager(Context context) {
        mContext = context;
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void register(Task task, boolean isDoneToday) {
        long taskId = task.getTaskID();
        sTaskMap.put(taskId, task);
        Reminder reminder = task.getReminder();
        Calendar nextSchedule = getNextSchedule(task.getRecurrence(), isDoneToday, reminder.getHourOfDay(), reminder.getMinute());
        if (nextSchedule != null) {
            startAlarm(taskId, nextSchedule);
        }
    }

    public void unregister(long taskId) {
        stopAlarm(taskId);
        sTaskMap.remove(taskId);
    }

    private Calendar getNextSchedule(Recurrence recurrence, boolean isDoneToday, int hourOfDay, int minute) {
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(System.currentTimeMillis());
        boolean todayAlreadyExceeded = false;
        int dayOffset = 0;
        boolean isSuccess = false;

        boolean[] recurrenceFlags = {
                recurrence.getSunday(),
                recurrence.getMonday(),
                recurrence.getTuesday(),
                recurrence.getWednesday(),
                recurrence.getThurday(),
                recurrence.getFriday(),
                recurrence.getSaturday()};
        int dayOfWeek = time.get(Calendar.DAY_OF_WEEK) - 1;

        // Check if today's reminder time is already exceeded
        if ((time.get(Calendar.HOUR_OF_DAY) > hourOfDay) ||
            ((time.get(Calendar.HOUR_OF_DAY) == hourOfDay) && time.get(Calendar.MINUTE) > minute)) {
            todayAlreadyExceeded = true;
        }
        if (isDoneToday || todayAlreadyExceeded) {
            dayOfWeek = (dayOfWeek + 1) % 7;
            dayOffset++;
        }

        for (int counter=0; counter<7; counter++) {
            if (recurrenceFlags[(dayOfWeek + counter) % 7] == true) {
                dayOffset = dayOffset + counter;
                isSuccess = true;
                break;
            }
        }
        if (! isSuccess) {
            return null;
        }

        time.add(Calendar.DAY_OF_MONTH, dayOffset);
        time.set(Calendar.HOUR_OF_DAY, hourOfDay);
        time.set(Calendar.MINUTE, minute);
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        return time;
    }

    private void startAlarm(long taskId, Calendar time) {
        mAlarmManager.set(AlarmManager.RTC_WAKEUP,
                time.getTimeInMillis(),
                getPendingIntent(taskId));
    }

    private void stopAlarm(long taskId) {
        mAlarmManager.cancel(getPendingIntent(taskId));
    }

    private PendingIntent getPendingIntent(long taskId) {
        Task task = sTaskMap.get(taskId);
        Intent intent = new Intent(mContext, RemindAlarmReceiver.class);
        intent.putExtra("TASK-INFO", task);
        PendingIntent pendingIntent = 
                PendingIntent.getBroadcast(mContext, 0, intent, 0);
        return pendingIntent;
    }
}
