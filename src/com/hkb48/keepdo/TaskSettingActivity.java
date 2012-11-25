package com.hkb48.keepdo;

import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TaskSettingActivity extends Activity {
    private boolean[] recurrenceFlags = {true,true,true,true,true,true,true};
    private String[] weeks;
    private LinearLayout recurrenceChildLayout;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_setting_activity);

        Intent intent = getIntent();
        if(intent.getAction().equals("com.hkb48.keepdo.NEW_TASK")) {
        	setTitle(R.string.add_task);
        } else {
        	setTitle(R.string.edit_task);
        }

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
    	finish();
    }

    /**
     * Callback method for "Cancel" button
     * @param view
     */
    public void onCancelClicked(View view) {
    	finish();
    }

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
