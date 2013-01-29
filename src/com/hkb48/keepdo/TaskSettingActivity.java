package com.hkb48.keepdo;

import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;

public class TaskSettingActivity extends Activity {
    private boolean[] mRecurrenceFlags = {true,true,true,true,true,true,true};
    private Task mTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.task_setting_activity);

        EditText editText = (EditText) findViewById(R.id.editTextTaskName);
        Recurrence recurrence;

        Intent intent = getIntent();
        mTask = (Task) intent.getSerializableExtra("TASK-INFO");
        if (mTask == null) {
            setTitle(R.string.add_task);
            recurrence = new Recurrence(true, true, true, true, true, true, true);
            mTask = new Task(null, recurrence);
            mTask.setReminder(new Reminder());
        } else {
            setTitle(R.string.edit_task);
            String taskName = mTask.getName();
            if (taskName != null) {
                editText.setText(taskName);
                editText.setSelection(taskName.length());
            }

            recurrence = mTask.getRecurrence();
            mRecurrenceFlags[0] = recurrence.getSunday();
            mRecurrenceFlags[1] = recurrence.getMonday();
            mRecurrenceFlags[2] = recurrence.getTuesday();
            mRecurrenceFlags[3] = recurrence.getWednesday();
            mRecurrenceFlags[4] = recurrence.getThurday();
            mRecurrenceFlags[5] = recurrence.getFriday();
            mRecurrenceFlags[6] = recurrence.getSaturday();

            ((Button) findViewById(R.id.okButton)).setEnabled(true);
        }

        addTaskName(editText);
        addRecurrence(recurrence);
        addReminder();
    }

    private void addTaskName(EditText editText) {
        editText.addTextChangedListener( new TextWatcher() {
            public void afterTextChanged(Editable s) {
                 Button okButton = (Button) findViewById(R.id.okButton);
                if (s.length() > 0) {
                    okButton.setEnabled(true);
                } else {
                    okButton.setEnabled(false);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
            }
        } );
    }

    private void addRecurrence(Recurrence recurrence) {
        final String[] weekNames = getResources().getStringArray(R.array.week_names);
        final RecurrenceView recurrenceView = (RecurrenceView) findViewById(R.id.recurrenceView);
        recurrenceView.update(recurrence);

        findViewById(R.id.recurrenceLayout).setOnClickListener(new OnClickListener() {
            boolean tmpRecurrenceFlags[];

            public void onClick(View v) {
                tmpRecurrenceFlags = Arrays.copyOf(mRecurrenceFlags, mRecurrenceFlags.length);
                new AlertDialog.Builder(TaskSettingActivity.this)
                .setTitle(getString(R.string.recurrence))
                .setMultiChoiceItems(weekNames, mRecurrenceFlags,
                        new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        mRecurrenceFlags[which] = isChecked;
                    }
                })
                .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        recurrenceView.update(mRecurrenceFlags);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mRecurrenceFlags = tmpRecurrenceFlags;
                    }
                })
                .show();
            }
        });
    }

    private void addReminder() {
        final Reminder reminder = mTask.getReminder();
        final Button reminderTime = (Button) findViewById(R.id.buttonReminderTime);
        final ImageButton cancelButton = (ImageButton) findViewById(R.id.reminder_remove);

        final int hourOfDay = reminder.getHourOfDay();
        final int minute = reminder.getMinute();
        boolean isChecked = reminder.getEnabled();
        if (isChecked) {
            reminderTime.setText(hourOfDay + ":" + minute);
            cancelButton.setVisibility(View.VISIBLE);
        } else {
            reminderTime.setText(R.string.no_reminder);
            cancelButton.setVisibility(View.INVISIBLE);
        }

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                reminder.setEnabled(false);
                cancelButton.setVisibility(View.INVISIBLE);
                reminderTime.setText(R.string.no_reminder);
            }
        });

        final TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    reminderTime.setText(hourOfDay + ":" + minute);
                    cancelButton.setVisibility(View.VISIBLE);
                    Reminder reminder = mTask.getReminder();
                    reminder.setHourOfDay(hourOfDay);
                    reminder.setMinute(minute);
                    mTask.setReminder(reminder);
                }
            }, hourOfDay, minute, true);

        reminderTime.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                timePickerDialog.show();
            }
        });
    }

    /**
     * Callback method for "Save" button
     * @param view
     */
    public void onSaveClicked(View view) {
        EditText edit = (EditText) findViewById(R.id.editTextTaskName);
        Recurrence recurrence = new Recurrence(
                mRecurrenceFlags[1],
                mRecurrenceFlags[2],
                mRecurrenceFlags[3],
                mRecurrenceFlags[4],
                mRecurrenceFlags[5],
                mRecurrenceFlags[6],
                mRecurrenceFlags[0]);
        mTask.setName(edit.getText().toString());
        mTask.setRecurrence(recurrence);

        Intent data = new Intent();
        data.putExtra("TASK-INFO", mTask);
        setResult(RESULT_OK, data);
    	finish();
    }

    /**
     * Callback method for "Cancel" button
     * @param view
     */
    public void onCancelClicked(View view) {
        finish();
    }
}
