package com.hkb48.keepdo;

import android.content.SharedPreferences;

public class Settings {
    private static Settings sInstance = null;
    private static SharedPreferences sSharedPref;
    private static String sDoneIconType;
    private static String sAlertsRingTone;
    private static String sAlertsVibrateWhen;

    private Settings(SharedPreferences pref) {
        sSharedPref = pref;
    }

    public static Settings getInstance(SharedPreferences pref) {
        if (sInstance == null) {
            sInstance = new Settings(pref);
            sDoneIconType = sSharedPref.getString(GeneralSettingsFragment.KEY_GENERAL_DONE_ICON, null);
            sAlertsRingTone = sSharedPref.getString(GeneralSettingsFragment.KEY_ALERTS_RINGTONE, null);
            sAlertsVibrateWhen = sSharedPref.getString(GeneralSettingsFragment.KEY_ALERTS_VIBRATE_WHEN, null);
        }

        return sInstance;
    }

    public static void setDoneIcon(String v) {
        sDoneIconType = v;
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
