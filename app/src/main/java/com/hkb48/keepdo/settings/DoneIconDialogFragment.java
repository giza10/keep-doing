package com.hkb48.keepdo.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceDialogFragmentCompat;

import com.hkb48.keepdo.R;

public class DoneIconDialogFragment extends PreferenceDialogFragmentCompat {

    public static DoneIconDialogFragment newInstance(String key) {
        DoneIconDialogFragment fragment = new DoneIconDialogFragment();
        Bundle bundle = new Bundle(1);
        bundle.putString(PreferenceDialogFragmentCompat.ARG_KEY, key);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
    }

    @Override
    protected View onCreateDialogView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.done_icon_selector, null);

        DoneIconDialogFragment.OnClickListener listener = new DoneIconDialogFragment.OnClickListener();
        view.findViewById(R.id.done_icon_1).setOnClickListener(listener);
        view.findViewById(R.id.done_icon_2).setOnClickListener(listener);
        view.findViewById(R.id.done_icon_3).setOnClickListener(listener);
        view.findViewById(R.id.done_icon_4).setOnClickListener(listener);
        return view;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        // Do nothing
    }

    private class OnClickListener implements View.OnClickListener {
        public void onClick(View v) {
            requireDialog().dismiss();

            String newValue;
            int id = v.getId();
            if (id == R.id.done_icon_2) {
                newValue = "type2";
            } else if (id == R.id.done_icon_3) {
                newValue = "type3";
            } else if (id == R.id.done_icon_4) {
                newValue = "type4";
            } else {
                newValue = "type1";
            }

            DoneIconPreference preference = (DoneIconPreference) getPreference();
            preference.updatePreference(newValue);
        }
    }
}