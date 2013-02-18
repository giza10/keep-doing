package com.hkb48.keepdo;

import android.support.v4.app.Fragment;
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

		Intent intent = getActivity().getIntent();
        long taskId = intent.getLongExtra("TASK-ID", -1);
        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(this.getActivity());
        Task task = dbAdapter.getTask(taskId);

        RecurrenceView recurrenceView = (RecurrenceView) getActivity().findViewById(R.id.recurrenceView);
        recurrenceView.update(task.getRecurrence());
        TextView contextTextView = (TextView) getActivity().findViewById(R.id.taskDetailContextDescription);
        contextTextView.setText(task.getContext());
        TextView reminderTextView = (TextView) getActivity().findViewById(R.id.taskDetailReminderValue);
        Reminder reminder = task.getReminder();
        if (reminder.getEnabled()) {
        	int hourOfDay = reminder.getHourOfDay();
			int minute = reminder.getMinute();
			reminderTextView.setText(String.format("%1$02d", hourOfDay) + ":" + String.format("%1$02d", minute));
        } else {
        	reminderTextView.setText(R.string.no_reminder);
        }
	}
}
