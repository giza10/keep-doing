package com.hkb48.keepdo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class TaskSettingActivity extends Activity {

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
}
