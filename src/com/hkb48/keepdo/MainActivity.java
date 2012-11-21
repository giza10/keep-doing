package com.hkb48.keepdo;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.Menu;

import com.hkb48.keepdo.Database.TaskCompletions;
import com.hkb48.keepdo.Database.TasksToday;

public class MainActivity extends Activity {

	// Our application database
	protected DatabaseHelper mDatabaseHelper = null; 

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		mDatabaseHelper = new DatabaseHelper(this.getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mDatabaseHelper != null)
		{
			mDatabaseHelper.close();
		}
	}
	
	protected void fetchTasks() {

	}

	protected void addTask(String taskName, Integer frequency) {

		if ((taskName ==null) || (taskName.isEmpty()) || (frequency == null)) {
			return;
		}

		try {
			ContentValues cvTask = new ContentValues(2);
			cvTask.put(TasksToday.TASK_NAME, taskName);
			cvTask.put(TasksToday.TASK_FREQUENCY, frequency.toString());
			long rowID = mDatabaseHelper.getWritableDatabase().insert(TasksToday.TASK_NAME, null, cvTask);

			ContentValues cvComplete = new ContentValues();
			cvComplete.put(TaskCompletions.TASK_NAME_ID, rowID);
			mDatabaseHelper.getWritableDatabase().insert(TaskCompletions.TASK_COMPLETION_TABLE_NAME, null, cvComplete);

			mDatabaseHelper.close();
		} catch (SQLiteException e) {
			//@ToDo.
		}
	}

	protected void checkDone(Long taskID, Boolean doneSwitch) {
		if (taskID ==null) {
			return;
		}

		try {
			ContentValues cv = new ContentValues();
			if (doneSwitch == true) {				
			    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			    cv.put(TaskCompletions.TASK_COMPLETION_DATE, dateFormat.format(new Date())); //Insert 'now' as the date
			} else {
				cv.putNull(null);
			}
		    mDatabaseHelper.getWritableDatabase().update(TaskCompletions.TASK_COMPLETION_TABLE_NAME, cv, "id=?", new String[] {taskID.toString()});
		    mDatabaseHelper.getWritableDatabase().close();
		} catch (SQLiteException e) {
			//@Todo.
		}
	}
}