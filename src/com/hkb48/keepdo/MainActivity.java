package com.hkb48.keepdo;

import java.text.ParseException;
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
import android.util.Log;

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

        // Looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Recurrence recurrence = new Recurrence(Boolean.valueOf(cursor.getString(2)), Boolean.valueOf(cursor.getString(3)),
                        Boolean.valueOf(cursor.getString(4)),Boolean.valueOf(cursor.getString(5)), Boolean.valueOf(cursor.getString(6)), Boolean.valueOf(cursor.getString(7)),Boolean.valueOf(cursor.getString(8)));
                Task task = new Task(cursor.getString(1),recurrence);
                Long taskID = Long.parseLong(cursor.getString(0));
                boolean checked = isChecked(taskID, new Date());
                task.setTaskID(taskID);
                task.setChecked(checked);
                tasks.add(task);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

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

			if (rowID > 0) {
				contentValues.clear();
				contentValues.put(TaskCompletions.TASK_NAME_ID, rowID);
				mDatabaseHelper.getWritableDatabase().insert(TaskCompletions.TASK_COMPLETION_TABLE_NAME, null, contentValues);
				mDatabaseHelper.close();
			}
			mDatabaseHelper.close();

		} catch (SQLiteException e) {
			Log.e("_KEEPDOLOG: ", e.getMessage());
		}

		return rowID;
	}

    protected void editTask(Long taskID, String taskName, Recurrence recurrence) {
        if ((taskName ==null) || (taskName.isEmpty()) || (recurrence == null)) {
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(TasksToday.TASK_NAME, taskName);
        contentValues.put(TasksToday.FREQUENCY_MON, String.valueOf(recurrence.getMonday()));
        contentValues.put(TasksToday.FREQUENCY_TUE, String.valueOf(recurrence.getTuesday()));
        contentValues.put(TasksToday.FREQUENCY_WEN, String.valueOf(recurrence.getWednesday()));
        contentValues.put(TasksToday.FREQUENCY_THR, String.valueOf(recurrence.getThurday()));
        contentValues.put(TasksToday.FREQUENCY_FRI, String.valueOf(recurrence.getFriday()));
        contentValues.put(TasksToday.FREQUENCY_SAT, String.valueOf(recurrence.getSaturday()));
        contentValues.put(TasksToday.FREQUENCY_SUN, String.valueOf(recurrence.getSunday()));
        String whereClause = TasksToday._ID + "=?";
        String whereArgs[] = {taskID.toString()};

        try {
            mDatabaseHelper.getWritableDatabase().update(TasksToday.TASKS_TABLE_NAME, contentValues, whereClause, whereArgs);
        } catch (SQLiteException e) {
            Log.e("_KEEPDOLOG: ", e.getMessage());
        } finally {
            mDatabaseHelper.close();
        }
    }

    protected void deleteTask(Long taskID) {
        String whereClause = TasksToday._ID + "=?";
        String whereArgs[] = {taskID.toString()};

        mDatabaseHelper.getWritableDatabase().delete(TasksToday.TASKS_TABLE_NAME, whereClause, whereArgs);
        mDatabaseHelper.close();
    }

    //TODO: ROID v.s _ID to be validated.
	protected void setDoneStatus(Long taskID, Date date, Boolean doneSwitch) {
        if (taskID ==null) {
            return;
        }

        ContentValues contentValues = new ContentValues();
        String whereClause = TaskCompletions.TASK_NAME_ID + "=?";
        String whereArgs[] = {taskID.toString()};

        if (doneSwitch == true) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            if (date == null) {
                contentValues.put(TaskCompletions.TASK_COMPLETION_DATE, dateFormat.format(new Date())); //Insert 'now' as the date
            } else {
                contentValues.put(TaskCompletions.TASK_COMPLETION_DATE, dateFormat.format(date));
            }           

            mDatabaseHelper.getWritableDatabase().update(TaskCompletions.TASK_COMPLETION_TABLE_NAME, contentValues, whereClause, whereArgs);
        } else {
            contentValues.putNull(TaskCompletions.TASK_COMPLETION_DATE);
            mDatabaseHelper.getWritableDatabase().delete(TaskCompletions.TASK_COMPLETION_TABLE_NAME, whereClause, whereArgs);
        }

         mDatabaseHelper.close();
    }

    protected Task getTask(Long taskID) {
        Task task = null;
        String selectQuery = "SELECT * FROM " + TasksToday.TASKS_TABLE_NAME;
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                if (Long.parseLong(cursor.getString(0)) == taskID) {
                    Recurrence recurrence = new Recurrence(Boolean.valueOf(cursor.getString(2)), Boolean.valueOf(cursor.getString(3)),
                            Boolean.valueOf(cursor.getString(4)),Boolean.valueOf(cursor.getString(5)), Boolean.valueOf(cursor.getString(6)), Boolean.valueOf(cursor.getString(7)),Boolean.valueOf(cursor.getString(8)));
                    task = new Task(cursor.getString(1),recurrence);
                    task.setTaskID(taskID);
                    break;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        return task;
    }

    protected ArrayList<Date> getHistory(Long taskID, Date month) {
        ArrayList<Date> dateList = new ArrayList<Date>();
        String selectQuery = "SELECT * FROM " + TaskCompletions.TASK_COMPLETION_TABLE_NAME;
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        SimpleDateFormat sdf_ymd = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf_ym = new SimpleDateFormat("yyyy-MM");

        // Looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                if ((Long.parseLong(cursor.getString(1)) == taskID) &&
                    (cursor.getString(2) != null)) {
                    Date date = null;
                    try {
                        date = sdf_ymd.parse(cursor.getString(2));
                    } catch (ParseException e) {
                        Log.e("_KEEPDOLOG: ", e.getMessage());
                    }
                    if (date != null) {
                        if (sdf_ym.format(date).equals(sdf_ym.format(month))) {
                            dateList.add(date);
                        }
                    }
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        return dateList;
    }

    private boolean isChecked(Long taskID, Date day) {
        boolean isChecked = false;
        String selectQuery = "SELECT * FROM " + TaskCompletions.TASK_COMPLETION_TABLE_NAME;
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        SimpleDateFormat sdf_ymd = new SimpleDateFormat("yyyy-MM-dd");

        if (cursor.moveToFirst()) {
            do {
                if ((Long.parseLong(cursor.getString(1)) == taskID) &&
                    (cursor.getString(2) != null)) {
                    Date date = null;
                    try {
                        date = sdf_ymd.parse(cursor.getString(2));
                    } catch (ParseException e) {
                        Log.e("_KEEPDOLOG: ", e.getMessage());
                    }
                    if (date != null) {
                        if (sdf_ymd.format(date).equals(sdf_ymd.format(day))) {
                            isChecked = true;
                            break;
                        }
                    }
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
   
        return isChecked;
    }
}