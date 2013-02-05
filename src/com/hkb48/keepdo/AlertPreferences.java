package com.hkb48.keepdo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class AlertPreferences extends PreferenceFragment {

    private static final String SHARED_PREFS_NAME = "com.hkb48.keepdo_preferences";

    public static final String KEY_ALERTS_RINGTONE = "preferences_alerts_ringtone";

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.alert_preferences);
    }
}
