package com.hkb48.keepdo

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.hkb48.keepdo.settings.Settings.Companion.getWeekStartDay
import com.hkb48.keepdo.util.CompatUtil.getColor

class RecurrenceView(context: Context, attrs: AttributeSet?) : AppCompatTextView(
    context, attrs
) {
    private val mWeekName: Array<String>? = if (!isInEditMode) {
        resources.getStringArray(R.array.week_names)
    } else {
        null
    }

    fun update(recurrenceFlags: BooleanArray) {
        val separator = context.getString(R.string.recurrence_separator)
        val colorOffDay = getColor(context, R.color.recurrence_off_day)
        val weekStartDay = getWeekStartDay() - 1
        val ssb = SpannableStringBuilder()
        for (i in mWeekName!!.indices) {
            val index = (i + weekStartDay) % 7
            val start = ssb.length
            ssb.append(mWeekName[index])
            if (!recurrenceFlags[index]) {
                ssb.setSpan(
                    ForegroundColorSpan(colorOffDay), start, ssb.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            if (i != mWeekName.size - 1) {
                ssb.append(separator)
            }
        }
        text = ssb
    }

    fun update(recurrence: Recurrence) {
        val recurrenceFlags = BooleanArray(7)
        recurrenceFlags[0] = recurrence.sunday
        recurrenceFlags[1] = recurrence.monday
        recurrenceFlags[2] = recurrence.tuesday
        recurrenceFlags[3] = recurrence.wednesday
        recurrenceFlags[4] = recurrence.thursday
        recurrenceFlags[5] = recurrence.friday
        recurrenceFlags[6] = recurrence.saturday
        update(recurrenceFlags)
    }

}