package com.hkb48.keepdo;

import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * TODO: document your custom view class.
 */
public class TaskDetailFragment extends Fragment {

    public TaskDetailFragment() {
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
            String minuteStr    = String.format("%1$02d", reminder.getMinute());
            String remindAtStr  = activity.getString(R.string.remind_at);
            reminderTextView.setText(remindAtStr + " " + hourOfDayStr + ":" + minuteStr);
        } else {
            reminderTextView.setText(R.string.no_reminder);
        }

        // Context
        TextView contextTitleTextView = (TextView) activity.findViewById(R.id.taskDetailContext);
        TextView contextTextView = (TextView) activity.findViewById(R.id.taskDetailContextDescription);
        String contextStr = task.getContext();
        if (contextStr == null || contextStr.isEmpty()) {
            contextTitleTextView.setVisibility(View.INVISIBLE);
            contextTextView.setVisibility(View.INVISIBLE);
        } else {
            contextTitleTextView.setVisibility(View.VISIBLE);
            contextTextView.setVisibility(View.VISIBLE);
            contextTextView.setText(contextStr);
        }

        // Total number of done
        TextView numOfDoneTextView = (TextView) activity.findViewById(R.id.taskDetailNumOfDoneValue);
        numOfDoneTextView.setText(Integer.toString(dbAdapter.getNumberOfDone(taskId)));
	}
}
