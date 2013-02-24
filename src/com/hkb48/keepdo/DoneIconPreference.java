package com.hkb48.keepdo;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

public class DoneIconPreference extends DialogPreference {

    public DoneIconPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateDialogView() {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.done_icon_selector, null);

        view.findViewById(R.id.done_icon_1).setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                getDialog().dismiss();
                persistString("type1");
            }
        });

        view.findViewById(R.id.done_icon_2).setOnClickListener(new OnClickListener() {
            public void onClick(final View v) {
                getDialog().dismiss();
                persistString("type2");
            }
        });
        return view;
    }
}
