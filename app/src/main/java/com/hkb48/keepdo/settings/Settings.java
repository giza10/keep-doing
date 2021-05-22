package com.hkb48.keepdo.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.hkb48.keepdo.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public final class Settings {
    private static Settings sInstance = null;
    private final List<OnChangedListener> changedListeners = new ArrayList<>(1);
    private final SharedPreferences sharedPref;
    private String doneIconType;
    private String dateChangeTime;
    private String weekStartDay;
    private String alertsRingTone;
    private String alertsVibrateWhen;

    public interface OnChangedListener {
        void onDoneIconSettingChanged();

        void onDateChangeTimeSettingChanged();

        void onWeekStartDaySettingChanged();
    }

    public static void registerOnChangedListener(OnChangedListener listener) {
        if (listener != null && !sInstance.changedListeners.contains(listener)) {
            sInstance.changedListeners.add(listener);
        }
    }

    public static void unregisterOnChangedListener(OnChangedListener listener) {
        if (listener != null) {
            sInstance.changedListeners.remove(listener);
        }
    }

    private Settings(SharedPreferences pref) {
        sharedPref = pref;
    }

    public static void initialize(Context context) {
        if (sInstance == null) {
            GeneralSettingsFragment.setDefaultValues(context);
            SharedPreferences pref = GeneralSettingsFragment.getSharedPreferences(context);
            sInstance = new Settings(pref);
            sInstance.doneIconType = sInstance.sharedPref.getString(GeneralSettingsFragment.KEY_GENERAL_DONE_ICON, null);
            sInstance.dateChangeTime = sInstance.sharedPref.getString(GeneralSettingsFragment.KEY_GENERAL_DATE_CHANGE_TIME, null);
            sInstance.weekStartDay = sInstance.sharedPref.getString(GeneralSettingsFragment.KEY_CALENDAR_WEEK_START_DAY, null);
            sInstance.alertsRingTone = sInstance.sharedPref.getString(GeneralSettingsFragment.KEY_ALERTS_RINGTONE, null);
            sInstance.alertsVibrateWhen = sInstance.sharedPref.getString(GeneralSettingsFragment.KEY_ALERTS_VIBRATE_WHEN, null);
        }
    }

    static void setDoneIcon(String v) {
        sInstance.doneIconType = v;
        for (OnChangedListener listener : sInstance.changedListeners) {
            listener.onDoneIconSettingChanged();
        }
    }

    static void setDateChangeTime(String v) {
        sInstance.dateChangeTime = v;
        for (OnChangedListener listener : sInstance.changedListeners) {
            listener.onDateChangeTimeSettingChanged();
        }
    }

    static void setWeekStartDay(String v) {
        sInstance.weekStartDay = v;
        for (OnChangedListener listener : sInstance.changedListeners) {
            listener.onWeekStartDaySettingChanged();
        }
    }

    static void setAlertsRingTone(String v) {
        sInstance.alertsRingTone = v;
    }

    static void setAlertsVibrateWhen(String v) {
        sInstance.alertsVibrateWhen = v;
    }

    public static int getDoneIconId() {
        int doneIconId = R.drawable.ic_done_1;
        if (sInstance.doneIconType != null) {
            switch (sInstance.doneIconType) {
                case "type2":
                    doneIconId = R.drawable.ic_done_2;
                    break;
                case "type3":
                    doneIconId = R.drawable.ic_done_3;
                    break;
                case "type4":
                    doneIconId = R.drawable.ic_done_4;
                    break;
                default:
                    doneIconId = R.drawable.ic_done_1;
                    break;
            }
        }
        return doneIconId;
    }

    public static int getNotDoneIconId() {
        int notDoneIconId = R.drawable.ic_not_done_1;
        if (sInstance.doneIconType != null) {
            switch (sInstance.doneIconType) {
                case "type2":
                    notDoneIconId = R.drawable.ic_not_done_2;
                    break;
                case "type3":
                    notDoneIconId = R.drawable.ic_not_done_3;
                    break;
                case "type4":
                    notDoneIconId = R.drawable.ic_not_done_4;
                    break;
                default:
                    notDoneIconId = R.drawable.ic_not_done_1;
                    break;
            }
        }
        return notDoneIconId;
    }

    public static String getDateChangeTime() {
        if (sInstance.dateChangeTime == null) {
            sInstance.dateChangeTime = "24:00";
        }
        return sInstance.dateChangeTime;
    }

    public static int getWeekStartDay() {
        if (sInstance.weekStartDay == null) {
            sInstance.weekStartDay = "1";
        }
        int weekStartDay = Integer.parseInt(sInstance.weekStartDay);
        if (Calendar.SUNDAY <= weekStartDay && weekStartDay <= Calendar.SATURDAY) {
            return weekStartDay;
        } else {
            return Calendar.SUNDAY;
        }
    }

    public static String getAlertsRingTone() {
        return sInstance.alertsRingTone;
    }

    public static String getAlertsVibrateWhen() {
        return sInstance.alertsVibrateWhen;
    }
}
