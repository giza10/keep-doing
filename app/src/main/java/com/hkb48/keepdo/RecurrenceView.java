package com.hkb48.keepdo;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.hkb48.keepdo.com.hkb48.keepdo.util.CompatUtil;

public class RecurrenceView extends AppCompatTextView {
    private final String[] mWeekName;

    public RecurrenceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            mWeekName = getResources().getStringArray(R.array.week_names);
        } else {
            mWeekName = null;
        }
    }

    void update(boolean[] recurrenceFlags) {
        final String separator = getContext().getString(R.string.recurrence_separator);
        final int colorOffDay = CompatUtil.getColor(getContext(), R.color.recurrence_off_day);
        final int weekStartDay = Settings.getWeekStartDay() - 1;
        SpannableStringBuilder ssb = new SpannableStringBuilder();

        for (int i = 0; i < mWeekName.length; i++) {
            int index = (i + weekStartDay) % 7;
            int start = ssb.length();
            ssb.append(mWeekName[index]);
            if (!recurrenceFlags[index]) {
                ssb.setSpan(new ForegroundColorSpan(colorOffDay), start, ssb.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (i != (mWeekName.length - 1)) {
                ssb.append(separator);
            }
        }
        setText(ssb);
    }

    void update(Recurrence recurrence) {
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
}
