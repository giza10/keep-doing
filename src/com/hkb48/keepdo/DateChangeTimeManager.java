package com.hkb48.keepdo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class DateChangeTimeManager {
    private static DateChangeTimeManager sInstance;
    private final Context mContext;
    private final List<OnDateChangedListener> mChangedListeners = new ArrayList<OnDateChangedListener>(1);

    private Settings.OnChangedListener mSettingsChangedListener = new Settings.OnChangedListener() {
        public void onSettingsChanged() {
            startAlarm();
        }
    };

    private DateChangeTimeManager(Context context) {
        mContext = context;
    }

    public static DateChangeTimeManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DateChangeTimeManager(context);
        }
        return sInstance;
    }

    public interface OnDateChangedListener {
        public void onDateChanged();
    }

    public void registerOnDateChangedListener(OnDateChangedListener listener) {
        if (listener != null && !mChangedListeners.contains(listener)) {
        	mChangedListeners.add(listener);
            if (mChangedListeners.size() == 1) {
                Settings.registerOnChangedListener(mSettingsChangedListener);
                startAlarm();
            }
        }
    }

    public void unregisterOnDateChangedListener(OnDateChangedListener listener) {
        if (listener != null) {
        	mChangedListeners.remove(listener);
            if (mChangedListeners.size() < 1) {
                Settings.unregisterOnChangedListener(mSettingsChangedListener);
                stopAlarm();
            }
        }
    }

    public void dateChanged() {
        for (OnDateChangedListener listener : mChangedListeners) {
            listener.onDateChanged();
        }
    }

    private void startAlarm() {
        String dateChangeTime = Settings.getDateChangeTime();
        String[] time_para = dateChangeTime.split(":");
        int hour = Integer.parseInt(time_para[0]);
        int minutes = Integer.parseInt(time_para[1]);
        Calendar nextAlarmTime = getDateCalendar();
        nextAlarmTime.add(Calendar.DATE, 1);
        nextAlarmTime.set(Calendar.HOUR_OF_DAY, hour - 24);
        nextAlarmTime.set(Calendar.MINUTE, minutes);

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, nextAlarmTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, getPendingIntent(mContext));
    }

    private void stopAlarm() {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getPendingIntent(mContext));
    }

    private PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setType("DateChangeTime");
        PendingIntent pendingIntent = 
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    public static Date getDate() {
        return getDateCalendar().getTime();
    }

    public static Calendar getDateCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
        int nowMinutes = calendar.get(Calendar.MINUTE);

        String dateChangeTime = Settings.getDateChangeTime();
        String[] time_para = dateChangeTime.split(":");
        int hour = Integer.parseInt(time_para[0]);
        int minutes = Integer.parseInt(time_para[1]);

        nowHour += 24;
        if ((nowHour * 60 + nowMinutes) < (hour * 60 + minutes)) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}
