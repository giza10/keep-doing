package com.hkb48.keepdo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class GeneralSettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener {

    private static final String SHARED_PREFS_NAME = "com.hkb48.keepdo_preferences";

    public static final String KEY_GENERAL_DONE_ICON = "preferences_done_icon";

    public static final String KEY_ALERTS_CATEGORY = "preferences_alerts_category";
    public static final String KEY_ALERTS_RINGTONE = "preferences_alerts_ringtone";
    public static final String KEY_ALERTS_VIBRATE_WHEN = "preferences_alerts_vibrateWhen";

    private ListPreference mVibrateWhen;

    public static SharedPreferences getSharedPreferences(final Context context) {
        return context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.general_settings);

        final Activity activity = getActivity();
        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        mVibrateWhen = (ListPreference) preferenceScreen.findPreference(KEY_ALERTS_VIBRATE_WHEN);
        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) {
            PreferenceCategory alertGroup = (PreferenceCategory) preferenceScreen.findPreference(KEY_ALERTS_CATEGORY);
            alertGroup.removePreference(mVibrateWhen);
        } else {
            mVibrateWhen.setSummary(mVibrateWhen.getEntry());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mVibrateWhen.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mVibrateWhen.setOnPreferenceChangeListener(null);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mVibrateWhen) {
            mVibrateWhen.setValue((String) newValue);
            mVibrateWhen.setSummary(mVibrateWhen.getEntry());
        }
        return false;
    }
}
