package com.hkb48.keepdo;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

public class DoneIconPreference extends DialogPreference {

    public DoneIconPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue,
            Object defaultValue) {
        if (! restorePersistedValue) {
            persistString((String) defaultValue);
        }
    }

    @Override
    protected View onCreateDialogView() {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.done_icon_selector, null);

        view.findViewById(R.id.done_icon_1).setOnClickListener(new OnClickListener());
        view.findViewById(R.id.done_icon_2).setOnClickListener(new OnClickListener());
        view.findViewById(R.id.done_icon_3).setOnClickListener(new OnClickListener());
        view.findViewById(R.id.done_icon_4).setOnClickListener(new OnClickListener());
        return view;
    }

    private class OnClickListener implements View.OnClickListener {
        public void onClick(View v) {
            getDialog().dismiss();

            String newValue;
            switch (v.getId()) {
            case R.id.done_icon_2:
                newValue = "type2";
                break;
            case R.id.done_icon_3:
                newValue = "type3";
                break;
            case R.id.done_icon_4:
                newValue = "type4";
                break;
            case R.id.done_icon_1:
            default:
                newValue = "type1";
                break;
            }
            if (callChangeListener(newValue)) {
                persistString(newValue);
            }
        }
    }
}
