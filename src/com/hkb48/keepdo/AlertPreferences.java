package com.hkb48.keepdo;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class AlertPreferences extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.alert_preferences);
    }
}
