package com.hkb48.keepdo;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RecurrenceView extends LinearLayout {
    final Context context;
    final String[] weekName;
    float textSize = 16.0f;

    public RecurrenceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        weekName = getResources().getStringArray(R.array.week_names);
    }

    public void update(boolean[] recurrenceFlags) {
        String separator = context.getString(R.string.recurrence_separator);
        removeAllViewsInLayout();

        for (int i = 0; i < weekName.length; i++) {
            TextView week = new TextView(context);
            week.setText(weekName[i]);
            week.setTextSize(textSize);
            if( recurrenceFlags[i] == false ) {
                week.setTextColor(getResources().getColor(R.color.recurrence_off_day));
            }
            if( i != weekName.length - 1) {
                week.append(separator);
            }
            addView(week);
        }
    }

    public void update(Recurrence recurrence) {
        boolean[] recurrenceFlags = new boolean[7];
        recurrenceFlags[0] = recurrence.getSunday();
        recurrenceFlags[1] = recurrence.getMonday();
        recurrenceFlags[2] = recurrence.getTuesday();
        recurrenceFlags[3] = recurrence.getWednesday();
        recurrenceFlags[4] = recurrence.getThurday();
        recurrenceFlags[5] = recurrence.getFriday();
        recurrenceFlags[6] = recurrence.getSaturday();

        update(recurrenceFlags);
    }

    public void setTextSize(float size) {
        textSize = size;
    }
}
