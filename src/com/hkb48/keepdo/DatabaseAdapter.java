package com.hkb48.keepdo;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.hkb48.keepdo.Database.TaskCompletions;
import com.hkb48.keepdo.Database.TasksToday;

public class DatabaseAdapter {
    private static final String TAG_KEEPDO = "#LOG_KEEPDO: ";
    private static final String SDF_PATTERN_YMD = "yyyy-MM-dd"; 
    private static final String SDF_PATTERN_YM = "yyyy-MM";
    private static final String SELECT_FORM = "select * from ";
    private static final String SELECT_ARG_FORM = " where ";

    private static DatabaseAdapter INSTANCE = null;
    private DatabaseHelper mDatabaseHelper = null;
    private SQLiteDatabase mDatabase = null;

    private DatabaseAdapter(Context context) {
        mDatabaseHelper = new DatabaseHelper(context.getApplicationContext());
        setDatabase();
    }

    public static DatabaseAdapter getInstance(Context context) {
    	if (INSTANCE == null) {
    		INSTANCE = new DatabaseAdapter(context);
    	}

    	return INSTANCE;
    }
   
    private SQLiteDatabase openDatabase() {
    	try {
    		mDatabase = mDatabaseHelper.getWritableDatabase();
    	} catch (SQLiteException sqle) {
    		throw sqle;
    	}
 
    	return mDatabase;
    }

    private void closeDatabase() {
    	if (mDatabase != null) {
    		mDatabase.close();
    	}
    }
    
    private void setDatabase() {
        try {
            mDatabaseHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        } catch(SQLException sqle){
            throw sqle;
        }
    }

    public void close() {
        mDatabaseHelper.close();
    }
    
    public List<Task> getTaskList() {
        List<Task> tasks = new ArrayList<Task>();
        String selectQuery = SELECT_FORM + TasksToday.TASKS_TABLE_NAME;

        Cursor cursor = openDatabase().rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String taskName = cursor.getString(1);
                Recurrence recurrence = new Recurrence(Boolean.valueOf(cursor.getString(2)), Boolean.valueOf(cursor.getString(3)),
                        Boolean.valueOf(cursor.getString(4)),Boolean.valueOf(cursor.getString(5)), Boolean.valueOf(cursor.getString(6)), Boolean.valueOf(cursor.getString(7)),Boolean.valueOf(cursor.getString(8)));
                Reminder reminder = new Reminder(Boolean.valueOf(cursor.getString(9)), Integer.valueOf(cursor.getString(10)), Integer.valueOf(cursor.getString(11)));
                Long taskID = Long.parseLong(cursor.getString(0));
                Task task = new Task(taskName, recurrence);
                task.setTaskID(taskID);
                task.setReminder(reminder);
                tasks.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        closeDatabase();

        return tasks;
    }

    public long addTask(Task task) {
        long rowID = Task.INVALID_TASKID;
        String taskName = task.getName();
        Recurrence recurrence = task.getRecurrence();
        Reminder reminder = task.getReminder();

        if ((taskName == null) || (taskName.isEmpty()) || (recurrence == null) || (reminder == null)) {
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
            contentValues.put(TasksToday.REMINDER_ENABLED, String.valueOf(reminder.getEnabled()));
            contentValues.put(TasksToday.REMINDER_TIME_HOUR, String.valueOf(reminder.getHourOfDay()));
            contentValues.put(TasksToday.REMINDER_TIME_MINUTE, String.valueOf(reminder.getMinute()));

            
            rowID = openDatabase().insertOrThrow(TasksToday.TASKS_TABLE_NAME, null, contentValues);
            closeDatabase();
            
        } catch (SQLiteException e) {
            Log.e(TAG_KEEPDO, e.getMessage());
        }

        return rowID;
    }

    protected void editTask(Task task) {
        Long taskID = task.getTaskID();
        String taskName = task.getName();
        Recurrence recurrence = task.getRecurrence();
        Reminder reminder = task.getReminder();

        if ((taskName == null) || (taskName.isEmpty()) || (recurrence == null) || (reminder == null)) {
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
        contentValues.put(TasksToday.REMINDER_ENABLED, String.valueOf(reminder.getEnabled()));
        contentValues.put(TasksToday.REMINDER_TIME_HOUR, String.valueOf(reminder.getHourOfDay()));
        contentValues.put(TasksToday.REMINDER_TIME_MINUTE, String.valueOf(reminder.getMinute()));
        String whereClause = TasksToday._ID + "=?";
        String whereArgs[] = {taskID.toString()};

        try {
        	openDatabase().update(TasksToday.TASKS_TABLE_NAME, contentValues, whereClause, whereArgs);
        	closeDatabase();
        } catch (SQLiteException e) {
            Log.e(TAG_KEEPDO, e.getMessage());
        }
    }

    protected void deleteTask(Long taskID) {
        // Delete task from TASKS_TABLE_NAME
    	String whereClause = TasksToday._ID + "=?";
        String whereArgs[] = {taskID.toString()};
        openDatabase().delete(TasksToday.TASKS_TABLE_NAME, whereClause, whereArgs);
        closeDatabase();

        // Delete records of deleted task from TASK_COMPLETION_TABLE_NAME
        whereClause = TaskCompletions.TASK_NAME_ID + "=?";
        openDatabase().delete(TaskCompletions.TASK_COMPLETION_TABLE_NAME, whereClause, whereArgs);
        closeDatabase();
    }

    //TODO: ROID v.s _ID to be validated.
	protected void setDoneStatus(Long taskID, Date date, Boolean doneSwitch) {
        if (taskID == null) {
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(SDF_PATTERN_YMD);

        if (doneSwitch == true) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(TaskCompletions.TASK_NAME_ID, taskID);
            contentValues.put(TaskCompletions.TASK_COMPLETION_DATE, dateFormat.format(date));

            try {
            	openDatabase().insertOrThrow(TaskCompletions.TASK_COMPLETION_TABLE_NAME, null, contentValues);
            } catch (SQLiteException e) {
                Log.e(TAG_KEEPDO, e.getMessage());
            } finally {
            	closeDatabase();
            }

        } else {
            String whereClause = TaskCompletions.TASK_NAME_ID + "=? and " + TaskCompletions.TASK_COMPLETION_DATE + "=?";
            String whereArgs[] = {taskID.toString(), dateFormat.format(date)};
            openDatabase().delete(TaskCompletions.TASK_COMPLETION_TABLE_NAME, whereClause, whereArgs);
            closeDatabase();
        }
    }

    protected boolean getDoneStatus(Long taskID, Date day) {
        boolean isDone = false;
        String selectQuery = SELECT_FORM + TaskCompletions.TASK_COMPLETION_TABLE_NAME + SELECT_ARG_FORM + TaskCompletions.TASK_NAME_ID + "=?";
        Cursor cursor = openDatabase().rawQuery(selectQuery, new String[] {String.valueOf(taskID)});
        SimpleDateFormat sdf_ymd = new SimpleDateFormat(SDF_PATTERN_YMD);

        if (cursor.moveToFirst()) {
            do {
                String dateString = cursor.getString(2);
                if (dateString != null) {
                    Date date = null;
                    try {
                        date = sdf_ymd.parse(dateString);
                    } catch (ParseException e) {
                        Log.e(TAG_KEEPDO, e.getMessage());
                    }
                    if (date != null) {
                        if (sdf_ymd.format(date).equals(sdf_ymd.format(day))) {
                            isDone = true;
                            break;
                        }
                    }
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        closeDatabase();

        return isDone;
    }

    protected Task getTask(Long taskID) {
        Task task = null;
        String selectQuery = SELECT_FORM + TasksToday.TASKS_TABLE_NAME + SELECT_ARG_FORM + TasksToday._ID + "=?";

        Cursor cursor = openDatabase().rawQuery(selectQuery, new String[] {String.valueOf(taskID)});
        if (cursor != null){
            cursor.moveToFirst();
            String taskName = cursor.getString(1);
            Recurrence recurrence = new Recurrence(Boolean.valueOf(cursor.getString(2)), Boolean.valueOf(cursor.getString(3)),
                    Boolean.valueOf(cursor.getString(4)),Boolean.valueOf(cursor.getString(5)), Boolean.valueOf(cursor.getString(6)), Boolean.valueOf(cursor.getString(7)),Boolean.valueOf(cursor.getString(8)));
            Reminder reminder = new Reminder(Boolean.valueOf(cursor.getString(9)), Integer.valueOf(cursor.getString(10)), Integer.valueOf(cursor.getString(11)));
            task = new Task(taskName, recurrence);
            task.setReminder(reminder);
            task.setTaskID(taskID);
        }

        cursor.close();
        closeDatabase();

        return task;
    }

    protected ArrayList<Date> getHistory(Long taskID, Date month) {
        ArrayList<Date> dateList = new ArrayList<Date>();
        String selectQuery = SELECT_FORM + TaskCompletions.TASK_COMPLETION_TABLE_NAME + SELECT_ARG_FORM + TaskCompletions.TASK_NAME_ID + "=?";;
        
        Cursor cursor = openDatabase().rawQuery(selectQuery, new String[] {String.valueOf(taskID)});
        SimpleDateFormat sdf_ymd = new SimpleDateFormat(SDF_PATTERN_YMD);
        SimpleDateFormat sdf_ym = new SimpleDateFormat(SDF_PATTERN_YM);

        if (cursor.moveToFirst()) {
            do {
                String dateString = cursor.getString(2);
                if (dateString != null) {
                    Date date = null;
                    try {
                        date = sdf_ymd.parse(dateString);
                    } catch (ParseException e) {
                        Log.e(TAG_KEEPDO, e.getMessage());
                    }
                    if (date != null) {
                        if (sdf_ym.format(date).equals(sdf_ym.format(month))) {
                            dateList.add(date);
                        }
                    }
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        closeDatabase();
        
        return dateList;
    }
}