package com.hkb48.keepdo;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RecurrenceView extends LinearLayout {
    private static final float DEFAULT_TEXT_SIZE = 16.0f;

    private final Context mContext;
    private final String[] mWeekName;
    private float mTextSize;

    public RecurrenceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mWeekName = getResources().getStringArray(R.array.week_names);
        mTextSize = DEFAULT_TEXT_SIZE;
    }

    public void update(boolean[] recurrenceFlags) {
        String separator = mContext.getString(R.string.recurrence_separator);
        int calorOffDay = getResources().getColor(R.color.recurrence_off_day);
        removeAllViewsInLayout();
        int WeekStartDay = Settings.getWeekStartDay() - 1;

        for (int i = 0; i < mWeekName.length; i++) {
        	int index = (i + WeekStartDay) % 7;
            TextView week = new TextView(mContext);
            week.setText(mWeekName[index]);
            week.setTextSize(mTextSize);
            if (recurrenceFlags[index] == false) {
                week.setTextColor(calorOffDay);
            }
            if (i != (mWeekName.length - 1)) {
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
        mTextSize = size;
    }
}
