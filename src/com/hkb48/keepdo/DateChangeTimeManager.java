package com.hkb48.keepdo;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DateChangeTimeManager {
    private static final String TAG_KEEPDO = "#LOG_KEEPDO: ";

    private static DateChangeTimeManager sInstance;
    private final Context mContext;
    private final List<OnDateChangedListener> mChangedListeners = new ArrayList<OnDateChangedListener>();

    private Settings.OnChangedListener mSettingsChangedListener = new Settings.OnChangedListener() {
        public void onDoneIconSettingsChanged() {}

        public void onDateChangeTimeSettingsChanged() {
            startAlarm();
            ReminderManager.getInstance().setNextAlert(mContext);
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
    	DateChangeTimeUtil.DateChangeTime dateChangeTime = DateChangeTimeUtil.getDateChangeTime();
        Calendar nextAlarmTime = DateChangeTimeUtil.getDateTimeCalendar();
        nextAlarmTime.add(Calendar.DATE, 1);
        nextAlarmTime.set(Calendar.HOUR_OF_DAY, dateChangeTime.hourOfDay);
        nextAlarmTime.set(Calendar.MINUTE, dateChangeTime.minute);
        nextAlarmTime.set(Calendar.SECOND, 0);
        nextAlarmTime.set(Calendar.MILLISECOND, 0);

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, nextAlarmTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, getPendingIntent(mContext));
        dumpLog(nextAlarmTime.getTimeInMillis());
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



    private void dumpLog(long timeInMillis) {
        if (BuildConfig.DEBUG) {
            Calendar time = Calendar.getInstance();
            time.setTimeInMillis(timeInMillis);
            MessageFormat mf = new MessageFormat("{0,date,yyyy/MM/dd HH:mm:ss}");
            Object[] objs = {time.getTime()};
            String result = mf.format(objs);
            Log.v(TAG_KEEPDO + "DateChangeTimeManager", "time:" + result);
        }
    }
}
