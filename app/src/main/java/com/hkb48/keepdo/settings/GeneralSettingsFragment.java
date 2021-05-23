package com.hkb48.keepdo.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.hkb48.keepdo.NotificationController;
import com.hkb48.keepdo.R;
import com.hkb48.keepdo.util.CompatUtil;

public class GeneralSettingsFragment extends PreferenceFragmentCompat
        implements OnPreferenceChangeListener {

    public static final String KEY_GENERAL_DONE_ICON = "preferences_done_icon";
    public static final String KEY_GENERAL_DATE_CHANGE_TIME = "preferences_date_change_time";

    public static final String KEY_CALENDAR_WEEK_START_DAY = "preferences_calendar_week_start_day";
    public static final String KEY_CALENDAR_ENABLE_FUTURE_DATE = "preferences_calendar_enable_future_date";
    public static final String KEY_ALERTS_RINGTONE = "preferences_alerts_ringtone";
    public static final String KEY_ALERTS_VIBRATE_WHEN = "preferences_alerts_vibrateWhen";
    private static final String KEY_ALERTS_CATEGORY = "preferences_alerts_category";
    private static final String KEY_ALERTS_NOTIFICATION = "preferences_notification";
    private static final int RINGTONE_REQUEST_CODE = 1;

    private DoneIconPreference mDoneIconPref;
    private ListPreference mDateChangeTimePref;
    private ListPreference mWeekStartDayPref;
    private SwitchPreferenceCompat mEnableFutureDatePref;
    private RingtonePreference mRingtonePref;
    private ListPreference mVibrateWhenPref;

    public static SharedPreferences getSharedPreferences(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Set the default shared preferences in the proper context
     */
    public static void setDefaultValues(Context context) {
        PreferenceManager.setDefaultValues(context, R.xml.general_settings, false);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.general_settings, rootKey);

        final Activity activity = requireActivity();
        final PreferenceScreen preferenceScreen = getPreferenceScreen();

        mDoneIconPref = preferenceScreen.findPreference(KEY_GENERAL_DONE_ICON);

        mDateChangeTimePref = preferenceScreen.findPreference(KEY_GENERAL_DATE_CHANGE_TIME);
        assert mDateChangeTimePref != null;
        String summary = getString(R.string.preferences_date_change_time_summary, mDateChangeTimePref.getValue());
        mDateChangeTimePref.setSummary(summary);

        mWeekStartDayPref = preferenceScreen.findPreference(KEY_CALENDAR_WEEK_START_DAY);
        assert mWeekStartDayPref != null;
        mWeekStartDayPref.setSummary(mWeekStartDayPref.getEntry());

        mEnableFutureDatePref = preferenceScreen.findPreference(KEY_CALENDAR_ENABLE_FUTURE_DATE);
        assert mEnableFutureDatePref != null;

        final PreferenceCategory alertGroup = preferenceScreen.findPreference(KEY_ALERTS_CATEGORY);
        mRingtonePref = preferenceScreen.findPreference(KEY_ALERTS_RINGTONE);
        mVibrateWhenPref = preferenceScreen.findPreference(KEY_ALERTS_VIBRATE_WHEN);
        Preference notificationPref = preferenceScreen.findPreference(KEY_ALERTS_NOTIFICATION);

        assert alertGroup != null;
        assert mRingtonePref != null;
        assert notificationPref != null;
        if (CompatUtil.isNotificationChannelSupported()) {
            notificationPref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(android.provider.Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                intent.putExtra(android.provider.Settings.EXTRA_CHANNEL_ID, NotificationController.getNotificationChannelId());
                intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
                startActivity(intent);
                return true;
            });

            alertGroup.removePreference(mRingtonePref);
            alertGroup.removePreference(mVibrateWhenPref);
        } else {
            alertGroup.removePreference(notificationPref);

            Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator == null || !vibrator.hasVibrator()) {
                alertGroup.removePreference(mVibrateWhenPref);
            } else {
                mVibrateWhenPref.setSummary(mVibrateWhenPref.getEntry());
            }

            mRingtonePref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, mRingtonePref.getTitle());
                String currentRingtone = Settings.getAlertsRingTone();
                if (currentRingtone != null) {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(currentRingtone));
                }
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                startActivityForResult(intent, RINGTONE_REQUEST_CODE);
                return true;
            });
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof DoneIconPreference) {
            DoneIconDialogFragment dialogFragment =
                    DoneIconDialogFragment.newInstance(preference.getKey());
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getParentFragmentManager(), null);
        } else super.onDisplayPreferenceDialog(preference);
    }

    @Override
    public void onStart() {
        super.onStart();
        setListeners(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        setListeners(null);
    }

    private void setListeners(OnPreferenceChangeListener listener) {
        mDoneIconPref.setOnPreferenceChangeListener(listener);
        mDateChangeTimePref.setOnPreferenceChangeListener(listener);
        mWeekStartDayPref.setOnPreferenceChangeListener(listener);
        mEnableFutureDatePref.setOnPreferenceChangeListener(listener);
        mVibrateWhenPref.setOnPreferenceChangeListener(listener);
        mRingtonePref.setOnPreferenceChangeListener(listener);
        android.util.Log.e("test", "setListeners" + mRingtonePref.getOnPreferenceChangeListener());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        android.util.Log.e("test", "onPreferenceChange");
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
        } else if (preference == mEnableFutureDatePref) {
            Settings.setEnableFutureDate((boolean) newValue);
            ret = true;
        } else if (preference == mVibrateWhenPref) {
            Settings.setAlertsVibrateWhen((String) newValue);
            mVibrateWhenPref.setValue((String) newValue);
            mVibrateWhenPref.setSummary(mVibrateWhenPref.getEntry());
        } else if (preference == mRingtonePref) {
            Settings.setAlertsRingTone((String) newValue);
            ret = true;
        }
        return ret;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RINGTONE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri toneUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (toneUri != null) {
                    mRingtonePref.updatePreference(toneUri.toString());
                }
            }
        }
    }
}
