package com.hkb48.keepdo;

import java.util.Arrays;
import java.util.Calendar;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

public class TaskSettingActivity extends Activity {
    private boolean[] recurrenceFlags = {true,true,true,true,true,true,true};
    private String[] weeks;
    private Task task;
    private RecurrenceView recurrenceView;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_setting_activity);

        EditText editText = (EditText) findViewById(R.id.editText1);
        Recurrence recurrence;

        Intent intent = getIntent();
        task = (Task) intent.getSerializableExtra("TASK-INFO");
        if (task == null) {
            setTitle(R.string.add_task);
            recurrence = new Recurrence(true, true, true, true, true, true, true);
        } else {
            setTitle(R.string.edit_task);
            String taskName = task.getName();
            if (taskName != null) {
                editText.setText(taskName);
                editText.setSelection(taskName.length());
            }

            recurrence = task.getRecurrence();
            recurrenceFlags[0] = recurrence.getSunday();
            recurrenceFlags[1] = recurrence.getMonday();
            recurrenceFlags[2] = recurrence.getTuesday();
            recurrenceFlags[3] = recurrence.getWednesday();
            recurrenceFlags[4] = recurrence.getThurday();
            recurrenceFlags[5] = recurrence.getFriday();
            recurrenceFlags[6] = recurrence.getSaturday();

            ((Button) findViewById(R.id.okButton)).setEnabled(true);
        }

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

        weeks = getResources().getStringArray(R.array.week_names);
        recurrenceView = (RecurrenceView) findViewById(R.id.recurrenceView);
        recurrenceView.update(recurrence);

        findViewById(R.id.recurrenceLayout).setOnClickListener(new OnClickListener() {
            boolean tmpRecurrenceFlags[];

            public void onClick(View v) {
                tmpRecurrenceFlags = Arrays.copyOf(recurrenceFlags, recurrenceFlags.length);
                new AlertDialog.Builder(TaskSettingActivity.this)
                .setTitle(getString(R.string.recurrence))
                .setMultiChoiceItems(weeks, recurrenceFlags,
                        new DialogInterface.OnMultiChoiceClickListener(){
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        recurrenceFlags[which] = isChecked;
                    }
                })
                .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    recurrenceView.update(recurrenceFlags);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        recurrenceFlags = tmpRecurrenceFlags;
                    }
                })
                .show();
            }
        });

//        final TextView reminderTime = (TextView) findViewById(R.id.textView3);
//        final Calendar calendar = Calendar.getInstance();
//        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
//        final int minute = calendar.get(Calendar.MINUTE);
//        final TimePickerDialog timePickerDialog = new TimePickerDialog(
//            this,
//            new TimePickerDialog.OnTimeSetListener() {
//                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                    reminderTime.setText(hourOfDay + ":" + minute);
//                }
//            }, hour, minute, true);
//
//        reminderTime.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//                timePickerDialog.show();
//            }
//        });
    }

    /**
     * Callback method for "Save" button
     * @param view
     */
    public void onSaveClicked(View view) {
        EditText edit = (EditText) findViewById(R.id.editText1);
        Recurrence recurrence = new Recurrence(recurrenceFlags[1], recurrenceFlags[2], recurrenceFlags[3], recurrenceFlags[4], recurrenceFlags[5], recurrenceFlags[6], recurrenceFlags[0]);
        Reminder reminder = new Reminder(false, 1, 23);
        if (task == null) {
            task = new Task(edit.getText().toString(), recurrence);
            task.setReminder(reminder);
        } else {
            task.setName(edit.getText().toString());
            task.setRecurrence(recurrence);
            task.setReminder(reminder);
        }
        Intent data = new Intent();
        data.putExtra("TASK-INFO", task);
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
