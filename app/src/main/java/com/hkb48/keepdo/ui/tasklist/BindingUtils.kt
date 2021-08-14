package com.hkb48.keepdo.ui.tasklist

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.hkb48.keepdo.R
import com.hkb48.keepdo.Recurrence
import com.hkb48.keepdo.RecurrenceView
import com.hkb48.keepdo.Reminder
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.ui.settings.Settings
import com.hkb48.keepdo.util.CompatUtil
import java.util.*

@BindingAdapter("doneIcon")
fun ImageView.setDoneIcon(daysSinceLastDone: Int?) {
    val doneToday = (daysSinceLastDone == 0)
    setImageResource(
        if (doneToday) Settings.doneIconId
        else Settings.notDoneIconId
    )
}

@BindingAdapter("recurrence")
fun RecurrenceView.setRecurrence(task: Task) {
    update(Recurrence.getFromTask(task))
}

@BindingAdapter("description")
fun TextView.setDescription(description: String?) {
    text = description
    visibility = if (description != null && description.isNotEmpty()) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

@BindingAdapter("alarmText")
fun TextView.setAlarmText(task: Task) {
    text = if (task.reminderEnabled) {
        val reminder = Reminder(true, task.reminderTime ?: 0)
        String.format(
            Locale.getDefault(),
            "%1$02d",
            reminder.hourOfDay
        ) + ":" + String.format(
            Locale.getDefault(), "%1$02d", reminder.minute
        )
    } else {
        String()
    }
}

@BindingAdapter("lastDoneDateOrCombo")
fun TextView.setLastDoneDateOrCombo(item: TaskListItem) {
    val comboCount = item.comboCount
    if (comboCount > 1) {
        text = context.getString(R.string.tasklist_combo, comboCount)
        setTextColor(CompatUtil.getColor(context, R.color.tasklist_combo))
    } else {
        text = when (val daysSinceLastDone = item.daysSinceLastDone) {
            null -> context.getString(R.string.tasklist_lastdonedate_notyet)
            0 -> context.getString(R.string.tasklist_lastdonedate_today)
            1 -> context.getString(R.string.tasklist_lastdonedate_yesterday)
            else -> context.getString(R.string.tasklist_lastdonedate_diffdays, daysSinceLastDone)
        }
        setTextColor(CompatUtil.getColor(context, R.color.tasklist_last_donedate))
    }
}
