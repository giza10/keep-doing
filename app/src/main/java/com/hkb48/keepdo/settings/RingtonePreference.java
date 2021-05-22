package com.hkb48.keepdo.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.preference.Preference;

class RingtonePreference extends Preference {

    public RingtonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        persistString((String) defaultValue);
    }

    public void updatePreference(String newValue) {
        if (callChangeListener(newValue)) {
            persistString(newValue);
        }
    }
}