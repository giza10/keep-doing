package com.hkb48.keepdo;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

class Settings {
    private static Settings sInstance = null;
    private static final List<OnChangedListener> sChangedListeners = new ArrayList<>(1);
    private static SharedPreferences sSharedPref;
    private static String sDoneIconType;
    private static String sDateChangeTime;
    private static String sWeekStartDay;
    private static String sAlertsRingTone;
    private static String sAlertsVibrateWhen;

    public interface OnChangedListener {
        void onDoneIconSettingChanged();
        void onDateChangeTimeSettingChanged();
        void onWeekStartDaySettingChanged();
    }

    static void registerOnChangedListener(OnChangedListener listener) {
        if (listener != null && !sChangedListeners.contains(listener)) {
            sChangedListeners.add(listener);
        }
    }

    static void unregisterOnChangedListener(OnChangedListener listener) {
        if (listener != null) {
            sChangedListeners.remove(listener);
        }
    }

    private Settings(SharedPreferences pref) {
        sSharedPref = pref;
    }

    static void initialize(Context context) {
        if (sInstance == null) {
            GeneralSettingsFragment.setDefaultValues(context);
            SharedPreferences pref = GeneralSettingsFragment.getSharedPreferences(context);
            sInstance = new Settings(pref);
            sDoneIconType = sSharedPref.getString(GeneralSettingsFragment.KEY_GENERAL_DONE_ICON, null);
            sDateChangeTime = sSharedPref.getString(GeneralSettingsFragment.KEY_GENERAL_DATE_CHANGE_TIME, null);
            sWeekStartDay = sSharedPref.getString(GeneralSettingsFragment.KEY_CALENDAR_WEEK_START_DAY, null);
            sAlertsRingTone = sSharedPref.getString(GeneralSettingsFragment.KEY_ALERTS_RINGTONE, null);
            sAlertsVibrateWhen = sSharedPref.getString(GeneralSettingsFragment.KEY_ALERTS_VIBRATE_WHEN, null);
        }
    }

    static void setDoneIcon(String v) {
        sDoneIconType = v;
        for (OnChangedListener listener : sChangedListeners) {
            listener.onDoneIconSettingChanged();
        }
    }

    static void setDateChangeTime(String v) {
        sDateChangeTime = v;
        for (OnChangedListener listener : sChangedListeners) {
            listener.onDateChangeTimeSettingChanged();
        }
    }

    static void setWeekStartDay(String v) {
        sWeekStartDay = v;
        for (OnChangedListener listener : sChangedListeners) {
            listener.onWeekStartDaySettingChanged();
        }
    }

    static void setAlertsRingTone(String v) {
        sAlertsRingTone = v;
    }

    static void setAlertsVibrateWhen(String v) {
        sAlertsVibrateWhen = v;
    }

    static int getDoneIconId() {
        int doneIconId = R.drawable.ic_done_1;
        if (sDoneIconType != null) {
            switch (sDoneIconType) {
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

    static int getNotDoneIconId() {
        int notDoneIconId = R.drawable.ic_not_done_1;
        if (sDoneIconType != null) {
            switch (sDoneIconType) {
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

    static String getDateChangeTime() {
        if (sDateChangeTime == null) {
            sDateChangeTime = "24:00";
        }
        return sDateChangeTime;
    }

    static int getWeekStartDay() {
        if (sWeekStartDay == null) {
            sWeekStartDay = "1";
        }
        int weekStartDay = Integer.parseInt(sWeekStartDay);
        if (Calendar.SUNDAY <= weekStartDay && weekStartDay <= Calendar.SATURDAY) {
            return weekStartDay;
        } else {
            return Calendar.SUNDAY;
        }
    }

    static String getAlertsRingTone() {
        // TODO Get the value from shared-preference directly because
        // GeneralSettingsFragment#onPreferenceChange isn't called when ringtone setting is updated.
        sAlertsRingTone = sSharedPref.getString(GeneralSettingsFragment.KEY_ALERTS_RINGTONE, null);

        return sAlertsRingTone;
    }

    static String getAlertsVibrateWhen() {
        return sAlertsVibrateWhen;
    }
}
