package com.hkb48.keepdo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;

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
	protected void onDestroy() {
		super.onDestroy();
		if(mDatabaseHelper != null)
		{
			mDatabaseHelper.close();
		}
	}

	protected List<Task> getTaskList() {
		List<Task> tasks = new ArrayList<Task>();
		
		String selectQuery = "SELECT  * FROM " + TasksToday.TASKS_TABLE_NAME;
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
	    	do {
	    		Recurrence recurrence = new Recurrence(Boolean.valueOf(cursor.getString(1)), Boolean.valueOf(cursor.getString(2)),
	    				Boolean.valueOf(cursor.getString(3)),Boolean.valueOf(cursor.getString(4)), Boolean.valueOf(cursor.getString(5)), Boolean.valueOf(cursor.getString(6)),Boolean.valueOf(cursor.getString(7)));
	    		Task task = new Task(cursor.getString(0),recurrence);
	    		task.setTaskID(Integer.parseInt(cursor.getString(0)));
	    		tasks.add(task);
	    		//TODO: add Recurrency
    		} while (cursor.moveToNext());

	    	cursor.close();
	    }

	    // return contact list
        return tasks;
	}

	protected long addTask(String taskName, Recurrence recurrence) {
		long rowID = 0;
		
		if ((taskName ==null) || (taskName.isEmpty()) || (recurrence == null)) {
			return rowID;
		}

		try {
			ContentValues contentValues = new ContentValues();
			contentValues.put(TasksToday.TASK_NAME, taskName);
			contentValues.put(TasksToday.FREQUENCY_MON, String.valueOf(recurrence.getMonday()));
			contentValues.put(TasksToday.FREQUENCY_TUE, String.valueOf(recurrence.getTuesday()));
			contentValues.put(TasksToday.FREQUENCY_WEN, String.valueOf(recurrence.getWednesday()));
			contentValues.put(TasksToday.FREQUENCY_THR, String.valueOf(recurrence.getThurday()));
			contentValues.put(TasksToday.FREQUENCY_FRI, String.valueOf(recurrence.getFriday()));
			contentValues.put(TasksToday.FREQUENCY_SAT, String.valueOf(recurrence.getSaturday()));
			contentValues.put(TasksToday.FREQUENCY_SUN, String.valueOf(recurrence.getSunday()));
			
			rowID = mDatabaseHelper.getWritableDatabase().insertOrThrow(TasksToday.TASKS_TABLE_NAME, null, contentValues);

			contentValues.clear();
			contentValues.put(TaskCompletions.TASK_NAME_ID, rowID);
			mDatabaseHelper.getWritableDatabase().insert(TaskCompletions.TASK_COMPLETION_TABLE_NAME, null, contentValues);

			mDatabaseHelper.close();
		} catch (SQLiteException e) {
			//@ToDo.
		}

		return rowID;
	}

	protected void setDoneStatus(Long taskID, Date date, Boolean doneSwitch) {
		if (taskID ==null) {
			return;
		}

		try {
			ContentValues contentValues = new ContentValues();
			if (doneSwitch == true) {
			    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			    if (date == null) {
			    	contentValues.put(TaskCompletions.TASK_COMPLETION_DATE, dateFormat.format(new Date())); //Insert 'now' as the date
			    } else {
			    	contentValues.put(TaskCompletions.TASK_COMPLETION_DATE, dateFormat.format(date)); //Insert 'now' as the date			    	
			    }
			} else {
				contentValues.putNull(null);
			}
		    mDatabaseHelper.getWritableDatabase().update(TaskCompletions.TASK_COMPLETION_TABLE_NAME, contentValues, "id=?", new String[] {taskID.toString()});
		    mDatabaseHelper.getWritableDatabase().close();
		} catch (SQLiteException e) {
			//@Todo.
		}
	}

    protected Task getTask(Long taskID) {
        // TODO
        return new Task("Task xxx", new Recurrence(false, false, false, false, false, false, false));
    }    
}