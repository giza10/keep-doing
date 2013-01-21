package com.hkb48.keepdo;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReminderManager {
    private static final String TAG_KEEPDO = "#LOG_KEEPDO: ";
    private static ReminderManager sInstance;
    private static Map<Long, Task> sTaskMap = new HashMap<Long, Task>();

    private ReminderManager() {}

    public static ReminderManager getInstance() {
        if (sInstance == null) {
            sInstance = new ReminderManager();
        }
        return sInstance;
    }

    public void register(Context context, Task task, boolean isDoneToday) {
        long taskId = task.getTaskID();
        sTaskMap.put(taskId, task);
        Reminder reminder = task.getReminder();
        Calendar nextSchedule = getNextSchedule(task.getRecurrence(), isDoneToday, reminder.getHourOfDay(), reminder.getMinute());
        if (nextSchedule != null) {
            startAlarm(context, taskId, nextSchedule.getTimeInMillis());
        }
    }

    public void unregister(Context context, long taskId) {
        stopAlarm(context, taskId);
        sTaskMap.remove(taskId);
    }

    public void setNextAlert(final Context context) {
        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(context);
        List<Task> taskList = dbAdapter.getTaskList();
        long minTime = Long.MAX_VALUE;;
        long taskId = -1;
        for (Task task : taskList) {
            Reminder reminder = task.getReminder();
            if (reminder.getEnabled()) {
                boolean isDoneToday = dbAdapter.getDoneStatus(task.getTaskID(), new Date());
                int hourOfDay = reminder.getHourOfDay();
                int minute = reminder.getMinute();
                Calendar nextSchedule = getNextSchedule(task.getRecurrence(), isDoneToday, hourOfDay, minute);
                if (nextSchedule != null) {
                    if (minTime > nextSchedule.getTimeInMillis()) {
                        minTime = nextSchedule.getTimeInMillis();
                        taskId = task.getTaskID();
                    }
                }
            }
        }

        if (taskId != -1) {
            startAlarm(context, taskId, minTime);
        }
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
            ((time.get(Calendar.HOUR_OF_DAY) == hourOfDay) && time.get(Calendar.MINUTE) >= minute)) {
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

    private void startAlarm(Context context, long taskId, long timeInMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,
                timeInMillis,
                getPendingIntent(context, taskId));
        Log.v(TAG_KEEPDO + "ReminderManager#startAlarm()", "taskId = " + taskId);
    }

    private void stopAlarm(Context context, long taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getPendingIntent(context, taskId));
    }

    private PendingIntent getPendingIntent(Context context, long taskId) {
        Intent intent = new Intent(context, RemindAlarmReceiver.class);
        intent.putExtra("TASK-ID", taskId);
        PendingIntent pendingIntent = 
                PendingIntent.getBroadcast(context, 0, intent, 0);
        return pendingIntent;
    }
}
