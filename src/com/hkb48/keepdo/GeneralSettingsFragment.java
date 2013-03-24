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
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.util.Log;

public class GeneralSettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener {

    public static final String KEY_GENERAL_DONE_ICON = "preferences_done_icon";
    public static final String KEY_GENERAL_DATE_CHANGE_TIME = "preferences_date_change_time";

    public static final String KEY_ALERTS_CATEGORY = "preferences_alerts_category";
    public static final String KEY_ALERTS_RINGTONE = "preferences_alerts_ringtone";
    public static final String KEY_ALERTS_VIBRATE_WHEN = "preferences_alerts_vibrateWhen";

    private DoneIconPreference mDoneIconPref;
    private ListPreference mDateChangeTimePref;
    private RingtonePreference mRingtonePref;
    private ListPreference mVibrateWhen;

    public static SharedPreferences getSharedPreferences(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /** Set the default shared preferences in the proper context */
    public static void setDefaultValues(Context context) {
        PreferenceManager.setDefaultValues(context, R.xml.general_settings, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.general_settings);

        final Activity activity = getActivity();
        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        mDoneIconPref = (DoneIconPreference) preferenceScreen.findPreference(KEY_GENERAL_DONE_ICON);
        mDateChangeTimePref = (ListPreference) preferenceScreen.findPreference(KEY_GENERAL_DATE_CHANGE_TIME);
        String summary = getString(R.string.preferences_date_change_time_summary, mDateChangeTimePref.getValue());
        mDateChangeTimePref.setSummary(summary);
        mRingtonePref = (RingtonePreference) preferenceScreen.findPreference(KEY_ALERTS_RINGTONE);
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
        mDoneIconPref.setOnPreferenceChangeListener(this);
        mDateChangeTimePref.setOnPreferenceChangeListener(this);
        mRingtonePref.setOnPreferenceChangeListener(this);
        mVibrateWhen.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mDoneIconPref.setOnPreferenceChangeListener(null);
        mDateChangeTimePref.setOnPreferenceChangeListener(null);
        mRingtonePref.setOnPreferenceChangeListener(null);
        mVibrateWhen.setOnPreferenceChangeListener(null);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
    Log.v("KEEP-DO", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        boolean ret = false;
        if (preference == mDoneIconPref) {
            Settings.setDoneIcon((String) newValue);
            ret = true;
        } else if (preference == mDateChangeTimePref) {
            Settings.setDateChangeTime((String) newValue);
            mDateChangeTimePref.setValue((String) newValue);
            String summary = getString(R.string.preferences_date_change_time_summary, mDateChangeTimePref.getValue());
            mDateChangeTimePref.setSummary(summary);
        } else if (preference == mRingtonePref) {
            Settings.setAlertsRingTone((String) newValue);
            ret = true;
        } else if (preference == mVibrateWhen) {
            Settings.setAlertsVibrateWhen((String) newValue);
            mVibrateWhen.setValue((String) newValue);
            mVibrateWhen.setSummary(mVibrateWhen.getEntry());
        }
        return ret;
    }
}
