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

public class GeneralSettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener {

    public static final String KEY_GENERAL_DONE_ICON = "preferences_done_icon";
    public static final String KEY_GENERAL_DATE_CHANGE_TIME = "preferences_date_change_time";

    public static final String KEY_CALENDAR_WEEK_START_DAY = "preferences_calendar_week_start_day";

    public static final String KEY_ALERTS_CATEGORY = "preferences_alerts_category";
    public static final String KEY_ALERTS_RINGTONE = "preferences_alerts_ringtone";
    public static final String KEY_ALERTS_VIBRATE_WHEN = "preferences_alerts_vibrateWhen";

    private DoneIconPreference mDoneIconPref;
    private ListPreference mDateChangeTimePref;
    private ListPreference mWeekStartDayPref;
    private RingtonePreference mRingtonePref;
    private ListPreference mVibrateWhenPref;

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

        mWeekStartDayPref = (ListPreference) preferenceScreen.findPreference(KEY_CALENDAR_WEEK_START_DAY);
        mWeekStartDayPref.setSummary(mWeekStartDayPref.getEntry());

        mRingtonePref = (RingtonePreference) preferenceScreen.findPreference(KEY_ALERTS_RINGTONE);

        mVibrateWhenPref = (ListPreference) preferenceScreen.findPreference(KEY_ALERTS_VIBRATE_WHEN);
        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) {
            PreferenceCategory alertGroup = (PreferenceCategory) preferenceScreen.findPreference(KEY_ALERTS_CATEGORY);
            alertGroup.removePreference(mVibrateWhenPref);
        } else {
            mVibrateWhenPref.setSummary(mVibrateWhenPref.getEntry());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setListeners(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        setListeners(null);
    }

    private void setListeners(OnPreferenceChangeListener listener) {
        mDoneIconPref.setOnPreferenceChangeListener(listener);
        mDateChangeTimePref.setOnPreferenceChangeListener(listener);
        mWeekStartDayPref.setOnPreferenceChangeListener(listener);
        mRingtonePref.setOnPreferenceChangeListener(listener);
        mVibrateWhenPref.setOnPreferenceChangeListener(listener);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean ret = false;
        if (preference == mDoneIconPref) {
            Settings.setDoneIcon((String) newValue);
            ret = true;
        } else if (preference == mDateChangeTimePref) {
            Settings.setDateChangeTime((String) newValue);
            mDateChangeTimePref.setValue((String) newValue);
            String summary = getString(R.string.preferences_date_change_time_summary, mDateChangeTimePref.getValue());
            mDateChangeTimePref.setSummary(summary);
        } else if (preference == mWeekStartDayPref) {
            Settings.setWeekStartDay((String) newValue);
            mWeekStartDayPref.setValue((String) newValue);
            mWeekStartDayPref.setSummary(mWeekStartDayPref.getEntry());
        } else if (preference == mRingtonePref) {
            Settings.setAlertsRingTone((String) newValue);
            ret = true;
        } else if (preference == mVibrateWhenPref) {
            Settings.setAlertsVibrateWhen((String) newValue);
            mVibrateWhenPref.setValue((String) newValue);
            mVibrateWhenPref.setSummary(mVibrateWhenPref.getEntry());
        }
        return ret;
    }
}
