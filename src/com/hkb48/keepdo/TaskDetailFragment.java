package com.hkb48.keepdo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * TODO: document your custom view class.
 */
class TaskDetailFragment extends Fragment {

    public TaskDetailFragment() {
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.task_detail_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Activity activity = getActivity();
        Intent intent = activity.getIntent();
        long taskId = intent.getLongExtra("TASK-ID", -1);
        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(activity);
        Task task = dbAdapter.getTask(taskId);

        // Recurrence
        RecurrenceView recurrenceView = (RecurrenceView) activity.findViewById(R.id.recurrenceView);
        recurrenceView.update(task.getRecurrence());

        // Reminder
        TextView reminderTextView = (TextView) activity.findViewById(R.id.taskDetailReminderValue);
        Reminder reminder = task.getReminder();
        if (reminder.getEnabled()) {
            String hourOfDayStr = String.format("%1$02d", reminder.getHourOfDay());
            String minuteStr = String.format("%1$02d", reminder.getMinute());
            String remindAtStr = getString(R.string.remind_at);
            reminderTextView.setText(remindAtStr + " " + hourOfDayStr + ":" + minuteStr);
        } else {
            reminderTextView.setText(R.string.no_reminder);
        }

        // Context
        TextView contextTitleTextView = (TextView) activity.findViewById(R.id.taskDetailContext);
        TextView contextTextView = (TextView) activity.findViewById(R.id.taskDetailContextDescription);
        String contextStr = task.getContext();
        if (contextStr == null || contextStr.isEmpty()) {
            View contextLayout = activity.findViewById(R.id.taskDetailContextContainer);
            contextLayout.setVisibility(View.GONE);
            contextTitleTextView.setVisibility(View.INVISIBLE);
            contextTextView.setVisibility(View.INVISIBLE);
        } else {
            contextTitleTextView.setVisibility(View.VISIBLE);
            contextTextView.setVisibility(View.VISIBLE);
            contextTextView.setText(contextStr);
        }

        // Total number of done
        TextView numOfDoneTextView = (TextView) activity.findViewById(R.id.taskDetailNumOfDoneValue);
        numOfDoneTextView.setText(getString(R.string.number_of_times, dbAdapter.getNumberOfDone(taskId)));

        // Current combo / Max combo
        TextView comboTextView = (TextView) activity.findViewById(R.id.taskDetailComboValue);
        ComboCount combo = dbAdapter.getComboCount(taskId);
        if (combo != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(getString(R.string.number_of_times, combo.currentCount));
            sb.append(" / ");
            sb.append(getString(R.string.number_of_times, combo.maxCount));
            comboTextView.setText(sb.toString());
        }

        // First date that done is set
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN);
        TextView firstDoneDateTextView = (TextView) activity.findViewById(R.id.taskDetailFirstDoneDateValue);
        Date firstDoneDate = dbAdapter.getFirstDoneDate(taskId);
        if (firstDoneDate != null) {
            firstDoneDateTextView.setText(dateFormat.format(firstDoneDate));
        } else {
            View firstDoneDateLayout = activity.findViewById(R.id.taskDetailFirstDoneDateContainer);
            firstDoneDateLayout.setVisibility(View.GONE);
        }

        // Last date that done is set
        TextView lastDoneDateTextView = (TextView) activity.findViewById(R.id.taskDetailLastDoneDateValue);
        Date lastDoneDate = dbAdapter.getLastDoneDate(taskId);
        if (lastDoneDate != null) {
            lastDoneDateTextView.setText(dateFormat.format(lastDoneDate));
        } else {
            View lastDoneDateLayout = activity.findViewById(R.id.taskDetailLastDoneDateContainer);
            lastDoneDateLayout.setVisibility(View.GONE);
        }
    }
}
