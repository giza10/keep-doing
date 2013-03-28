package com.hkb48.keepdo;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
    private static Settings sInstance = null;
    private static final List<OnChangedListener> sChangedListeners = new ArrayList<OnChangedListener>(1);
    private static SharedPreferences sSharedPref;
    private static String sDoneIconType;
    private static String sDateChangeTime;
    private static String sAlertsRingTone;
    private static String sAlertsVibrateWhen;

    public interface OnChangedListener {
        public void onDoneIconSettingsChanged();
        public void onDateChangeTimeSettingsChanged();
    }

    public static void registerOnChangedListener(OnChangedListener listener) {
        if (listener != null && !sChangedListeners.contains(listener)) {
            sChangedListeners.add(listener);
        }
    }

    public static void unregisterOnChangedListener(OnChangedListener listener) {
        if (listener != null) {
            sChangedListeners.remove(listener);
        }
    }

    private Settings(SharedPreferences pref) {
        sSharedPref = pref;
    }

    public static void initialize(Context context) {
        if (sInstance == null) {
            GeneralSettingsFragment.setDefaultValues(context);
            SharedPreferences pref = GeneralSettingsFragment.getSharedPreferences(context);
            sInstance = new Settings(pref);
            sDoneIconType = sSharedPref.getString(GeneralSettingsFragment.KEY_GENERAL_DONE_ICON, null);
            sDateChangeTime = sSharedPref.getString(GeneralSettingsFragment.KEY_GENERAL_DATE_CHANGE_TIME, null);
            sAlertsRingTone = sSharedPref.getString(GeneralSettingsFragment.KEY_ALERTS_RINGTONE, null);
            sAlertsVibrateWhen = sSharedPref.getString(GeneralSettingsFragment.KEY_ALERTS_VIBRATE_WHEN, null);
        }
    }

    public static void setDoneIcon(String v) {
        sDoneIconType = v;
        for (OnChangedListener listener : sChangedListeners) {
            listener.onDoneIconSettingsChanged();
        }
    }

    public static void setDateChangeTime(String v) {
        sDateChangeTime = v;
        for (OnChangedListener listener : sChangedListeners) {
            listener.onDateChangeTimeSettingsChanged();
        }
    }

    public static void setAlertsRingTone(String v) {
        sAlertsRingTone = v;
    }

    public static void setAlertsVibrateWhen(String v) {
        sAlertsVibrateWhen = v;
    }

    public static int getDoneIconId() {
        int doneIconId = R.drawable.ic_done_1;
        if (sDoneIconType != null) {
            if (sDoneIconType.equals("type2")) {
                doneIconId = R.drawable.ic_done_2;
            } else if (sDoneIconType.equals("type3")) {
                doneIconId = R.drawable.ic_done_3;
            }
        }
        return doneIconId;
    }

    public static int getNotDoneIconId() {
        int notDoneIconId = R.drawable.ic_not_done_1;
        if (sDoneIconType != null) {
            if (sDoneIconType.equals("type2")) {
                notDoneIconId = R.drawable.ic_not_done_2;
            } else if (sDoneIconType.equals("type3")) {
                notDoneIconId = R.drawable.ic_not_done_3;
            }
        }
        return notDoneIconId;
    }

    public static String getDateChangeTime() {
        if (sDateChangeTime == null) {
            sDateChangeTime = new String("24:00");
        }
        return sDateChangeTime;
    }

    public static String getAlertsRingTone() {
        // TODO Get the value from shared-preference directly because
        // GeneralSettingsFragment#onPreferenceChange isn't called when ringtone setting is updated.
        sAlertsRingTone = sSharedPref.getString(GeneralSettingsFragment.KEY_ALERTS_RINGTONE, null);

        return sAlertsRingTone;
    }

    public static String getAlertsVibrateWhen() {
        return sAlertsVibrateWhen;
    }
}
