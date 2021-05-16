package com.hkb48.keepdo;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hkb48.keepdo.widget.TasksWidgetProvider;

import java.util.Arrays;

public class TaskSettingActivity extends AppCompatActivity {
    public static final String EXTRA_TASK_INFO = "TASK-INFO";
    private static final int MODE_NEW_TASK = 0;
    private static final int MODE_EDIT_TASK = 1;

    private boolean[] mRecurrenceFlags = {true, true, true, true, true, true,
            true};
    private Task mTask;
    private int mMode;
    private TextView mTitleText;
    private Button mSaveButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_task_setting);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            toolbar.setNavigationIcon(R.drawable.ic_close);
            LayoutInflater inflater = LayoutInflater.from(this);
            View v = inflater.inflate(R.layout.actionbar_task_setting, null);
            mTitleText = v.findViewById(R.id.title_text);
            mSaveButton = v.findViewById(R.id.button_save);
            mSaveButton.setOnClickListener(view -> onSaveClicked());
            actionBar.setCustomView(v);
        }

        EditText editTextTaskName = findViewById(R.id.editTextTaskName);
        enableInputEmoji(editTextTaskName);

        EditText editTextDescription = findViewById(R.id.editTextDescription);
        enableInputEmoji(editTextDescription);

        Recurrence recurrence;
        Intent intent = getIntent();
        mTask = (Task) intent.getSerializableExtra("TASK-INFO");
        if (mTask == null) {
            mMode = MODE_NEW_TASK;
            if (mTitleText != null) mTitleText.setText(R.string.add_task);
            recurrence = new Recurrence(true, true, true, true, true, true,
                    true);
            mTask = new Task(null, null, recurrence);
            mTask.setReminder(new Reminder());
        } else {
            mMode = MODE_EDIT_TASK;
            if (mTitleText != null) mTitleText.setText(R.string.edit_task);
            String taskName = mTask.getName();
            if (taskName != null) {
                editTextTaskName.setText(taskName);
                editTextTaskName.setSelection(taskName.length());
            }

            recurrence = mTask.getRecurrence();
            mRecurrenceFlags[0] = recurrence.getSunday();
            mRecurrenceFlags[1] = recurrence.getMonday();
            mRecurrenceFlags[2] = recurrence.getTuesday();
            mRecurrenceFlags[3] = recurrence.getWednesday();
            mRecurrenceFlags[4] = recurrence.getThurday();
            mRecurrenceFlags[5] = recurrence.getFriday();
            mRecurrenceFlags[6] = recurrence.getSaturday();

            String description = mTask.getContext();
            if (description != null) {
                editTextDescription.setText(description);
                editTextDescription.setSelection(description.length());
            }
        }

        addTaskName(editTextTaskName);
        addRecurrence(recurrence);
        addReminder();
        mSaveButton.setEnabled(canSave());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = true;
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            ret = super.onOptionsItemSelected(item);
        }
        return ret;
    }

    private void addTaskName(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (mSaveButton != null) {
                    mSaveButton.setEnabled(s.length() > 0);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }
        });
    }

    private void addRecurrence(Recurrence recurrence) {
        final String[] weekNames = getResources().getStringArray(
                R.array.week_names);
        final RecurrenceView recurrenceView = findViewById(R.id.recurrenceView);
        recurrenceView.update(recurrence);

        findViewById(R.id.recurrenceView).setOnClickListener(
                new OnClickListener() {
                    boolean[] tmpRecurrenceFlags;

                    public void onClick(final View v) {
                        tmpRecurrenceFlags = Arrays.copyOf(mRecurrenceFlags,
                                mRecurrenceFlags.length);
                        new AlertDialog.Builder(TaskSettingActivity.this)
                                .setTitle(getString(R.string.recurrence))
                                .setMultiChoiceItems(
                                        weekNames,
                                        mRecurrenceFlags,
                                        (dialog, which, isChecked) -> mRecurrenceFlags[which] = isChecked)
                                .setPositiveButton(
                                        getString(R.string.dialog_ok),
                                        (dialog, whichButton) -> recurrenceView
                                                .update(mRecurrenceFlags))
                                .setNegativeButton(
                                        getString(R.string.dialog_cancel),
                                        (dialog, whichButton) -> mRecurrenceFlags = tmpRecurrenceFlags).show();
                    }
                });
    }

    private void addReminder() {
        final Reminder reminder = mTask.getReminder();
        final Button reminderTime = findViewById(R.id.buttonReminderTime);
        final ImageButton cancelButton = findViewById(R.id.reminder_remove);

        final int hourOfDay = reminder.getHourOfDay();
        final int minute = reminder.getMinute();
        boolean isChecked = reminder.getEnabled();
        if (isChecked) {
            reminderTime.setText(getString(R.string.reminder_time, hourOfDay, minute));
            cancelButton.setVisibility(View.VISIBLE);
        } else {
            reminderTime.setText(R.string.no_reminder);
            cancelButton.setVisibility(View.INVISIBLE);
        }

        cancelButton.setOnClickListener(v -> {
            cancelButton.setVisibility(View.INVISIBLE);
            reminderTime.setText(R.string.no_reminder);
            reminder.setEnabled(false);
            mTask.setReminder(reminder);
        });

        final TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay1, minute1) -> {
                    reminderTime.setText(getString(R.string.reminder_time, hourOfDay1, minute1));
                    cancelButton.setVisibility(View.VISIBLE);
                    reminder.setEnabled(true);
                    reminder.setHourOfDay(hourOfDay1);
                    reminder.setMinute(minute1);
                    mTask.setReminder(reminder);
                }, hourOfDay, minute, true);

        reminderTime.setOnClickListener(v -> timePickerDialog.show());
    }

    private void onSaveClicked() {
        EditText editTextTaskName = findViewById(R.id.editTextTaskName);
        EditText editTextDescription = findViewById(R.id.editTextDescription);
        Recurrence recurrence = new Recurrence(mRecurrenceFlags[1],
                mRecurrenceFlags[2], mRecurrenceFlags[3], mRecurrenceFlags[4],
                mRecurrenceFlags[5], mRecurrenceFlags[6], mRecurrenceFlags[0]);
        mTask.setName(editTextTaskName.getText().toString());
        mTask.setRecurrence(recurrence);
        mTask.setContext(editTextDescription.getText().toString());

        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(this);
        long taskId;
        if (mMode == MODE_NEW_TASK) {
            mTask.setOrder(dbAdapter.getMaxSortOrderId() + 1);
            taskId = dbAdapter.addTask(mTask);
        } else {
            dbAdapter.editTask(mTask);
            taskId = mTask.getTaskID();
        }
        ReminderManager.getInstance().setAlarm(this, taskId);
        TasksWidgetProvider.notifyDatasetChanged(this);
        finish();
    }

    private boolean canSave() {
        EditText editTextTaskName = findViewById(R.id.editTextTaskName);
        return (editTextTaskName.getText().length() > 0);
    }

    private void enableInputEmoji(EditText editText) {
        Bundle bundle = editText.getInputExtras(true);
        if (bundle != null) {
            bundle.putBoolean("allowEmoji", true);
        }
    }
}
