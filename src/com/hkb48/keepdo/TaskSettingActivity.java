package com.hkb48.keepdo;

import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TaskSettingActivity extends Activity {
    private boolean[] recurrenceFlags = {true,true,true,true,true,true,true};
    private String[] weeks;
    private Task task;
    private LinearLayout recurrenceChildLayout;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_setting_activity);

        EditText editText = (EditText) findViewById(R.id.editText1);

        Intent intent = getIntent();
        task = (Task) intent.getSerializableExtra("TASK-INFO");
        if (task == null) {
            setTitle(R.string.add_task);
        } else {
            setTitle(R.string.edit_task);
            editText.setText(task.getName());

            Recurrence recurrence = task.getRecurrence();
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
        recurrenceChildLayout = (LinearLayout) findViewById(R.id.recurrenceChildLayout);
        updateRecurrence();

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
                        updateRecurrence();
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
    }

    /**
     * Callback method for "Save" button
     * @param view
     */
    public void onSaveClicked(View view) {
        EditText edit = (EditText) findViewById(R.id.editText1);
        Recurrence recurrence = new Recurrence(recurrenceFlags[1], recurrenceFlags[2], recurrenceFlags[3], recurrenceFlags[4], recurrenceFlags[5], recurrenceFlags[6], recurrenceFlags[0]);
        if (task == null) {
            task = new Task(edit.getText().toString(), recurrence);
        } else {
            task.setName(edit.getText().toString());
            task.setRecurrence(recurrence);
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

    /**
     * Update the display of recurrence status on the recurrence setting
     */
    private void updateRecurrence() {
        String separator = getString(R.string.recurrence_separator);
        recurrenceChildLayout.removeAllViews();

        for (int i = 0; i < weeks.length; i++) {
            TextView week = new TextView(this);
            week.setText(weeks[i]);
            if( recurrenceFlags[i] == false ) {
                week.setTextColor(getResources().getColor(R.color.recurrence_off_day));
            }
            if( i != weeks.length - 1) {
                week.append(separator);
            }
            recurrenceChildLayout.addView(week);
        }
    }
}
