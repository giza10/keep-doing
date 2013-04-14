package com.hkb48.keepdo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.hkb48.keepdo.Database.TaskCompletion;
import com.hkb48.keepdo.Database.TasksToday;

public class DatabaseAdapter {
    private static final String TAG = "#KEEPDO_DB_ADAPTER: ";
    private static final String SDF_PATTERN_YMD = "yyyy-MM-dd"; 
    private static final String SDF_PATTERN_YM = "yyyy-MM";
    private static final String SELECT_FORM = "select * from ";
    private static final String SELECT_ARG_FORM = " where ";

    private static DatabaseAdapter INSTANCE = null;
    private DatabaseHelper mDatabaseHelper = null;
    private SQLiteDatabase mDatabase = null;

    private DatabaseAdapter(Context context) {
        mDatabaseHelper = new DatabaseHelper(context.getApplicationContext());
    }

    public static synchronized DatabaseAdapter getInstance(Context context) {
    	if (INSTANCE == null) {
    		INSTANCE = new DatabaseAdapter(context);
    	}

    	return INSTANCE;
    }

    private SQLiteDatabase openDatabase() {
    	synchronized (this) {
	    	try {
	    		mDatabase = mDatabaseHelper.getWritableDatabase();
	    	} catch (SQLiteException sqle) {
	    		throw sqle;
	    	}
    	}
 
    	return mDatabase;
    }

    private void closeDatabase() {
    	synchronized (this) {
			if (mDatabase != null) {
				mDatabase.close();
				mDatabase = null;
			}
    	}
    }

    public synchronized void close() {
		mDatabaseHelper.close();
    }

    public List<Task> getTaskList() {
        List<Task> tasks = new ArrayList<Task>();
        String selectQuery = SELECT_FORM + TasksToday.TABLE_NAME;

        Cursor cursor = openDatabase().rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                tasks.add(getTask(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        closeDatabase();

        return tasks;
    }

    public long addTask(Task task) {
        long rowID = Task.INVALID_TASKID;
        String taskName = task.getName();
        String taskContext = task.getContext();
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
            contentValues.put(TasksToday.TASK_CONTEXT, taskContext);
            contentValues.put(TasksToday.REMINDER_ENABLED, String.valueOf(reminder.getEnabled()));
            contentValues.put(TasksToday.REMINDER_TIME, String.valueOf(reminder.getTimeInMillis()));

            rowID = openDatabase().insertOrThrow(TasksToday.TABLE_NAME, null, contentValues);
            closeDatabase();
            
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        }

        return rowID;
    }

    protected void editTask(Task task) {
        Long taskID = task.getTaskID();
        String taskName = task.getName();
        String taskContext = task.getContext();
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
        contentValues.put(TasksToday.TASK_CONTEXT, taskContext);
        contentValues.put(TasksToday.REMINDER_ENABLED, String.valueOf(reminder.getEnabled()));
        contentValues.put(TasksToday.REMINDER_TIME, String.valueOf(reminder.getTimeInMillis()));
        String whereClause = TasksToday._ID + "=?";
        String whereArgs[] = {taskID.toString()};

        try {
        	openDatabase().update(TasksToday.TABLE_NAME, contentValues, whereClause, whereArgs);
        	closeDatabase();
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    protected void deleteTask(Long taskID) {
        // Delete task from TASKS_TABLE_NAME
    	String whereClause = TasksToday._ID + "=?";
        String whereArgs[] = {taskID.toString()};
        openDatabase().delete(TasksToday.TABLE_NAME, whereClause, whereArgs);
        closeDatabase();

        // Delete records of deleted task from TASK_COMPLETION_TABLE_NAME
        whereClause = TaskCompletion.TASK_NAME_ID + "=?";
        openDatabase().delete(TaskCompletion.TABLE_NAME, whereClause, whereArgs);
        closeDatabase();
    }

    //TODO: ROID --> _ID to be validated.
	protected void setDoneStatus(Long taskID, Date date, Boolean doneSwitch) {
        if (taskID == null) {
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(SDF_PATTERN_YMD, Locale.JAPAN);

        if (doneSwitch == true) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(TaskCompletion.TASK_NAME_ID, taskID);
            contentValues.put(TaskCompletion.TASK_COMPLETION_DATE, dateFormat.format(date));

            try {
            	openDatabase().insertOrThrow(TaskCompletion.TABLE_NAME, null, contentValues);
            } catch (SQLiteException e) {
                Log.e(TAG, e.getMessage());
            } finally {
            	closeDatabase();
            }

        } else {
            String whereClause = TaskCompletion.TASK_NAME_ID + "=? and " + TaskCompletion.TASK_COMPLETION_DATE + "=?";
            String whereArgs[] = {taskID.toString(), dateFormat.format(date)};
            openDatabase().delete(TaskCompletion.TABLE_NAME, whereClause, whereArgs);
            closeDatabase();
        }
    }

    protected boolean getDoneStatus(Long taskID, Date day) {
        boolean isDone = false;
        String selectQuery = SELECT_FORM + TaskCompletion.TABLE_NAME + SELECT_ARG_FORM + TaskCompletion.TASK_NAME_ID + "=?";
        Cursor cursor = openDatabase().rawQuery(selectQuery, new String[] {String.valueOf(taskID)});
        SimpleDateFormat sdf_ymd = new SimpleDateFormat(SDF_PATTERN_YMD, Locale.JAPAN);

        if (cursor.moveToFirst()) {
            do {
                String dateString = cursor.getString(cursor.getColumnIndex(TaskCompletion.TASK_COMPLETION_DATE));
                if (dateString != null) {
                    Date date = null;
                    try {
                        date = sdf_ymd.parse(dateString);
                    } catch (ParseException e) {
                        Log.e(TAG, e.getMessage());
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

    public int getNumberOfDone(Long taskID) {
        int numberOfDone = 0;
        String selectQuery = SELECT_FORM + TaskCompletion.TABLE_NAME + SELECT_ARG_FORM + TaskCompletion.TASK_NAME_ID + "=?";
        Cursor cursor = openDatabase().rawQuery(selectQuery, new String[] {String.valueOf(taskID)});
        if (cursor != null) {
            numberOfDone = cursor.getCount();
            cursor.close();
        }
        closeDatabase();
        return numberOfDone;
    }

    protected Task getTask(Long taskID) {
        Task task = null;
        String selectQuery = SELECT_FORM + TasksToday.TABLE_NAME + SELECT_ARG_FORM + TasksToday._ID + "=?";

        Cursor cursor = openDatabase().rawQuery(selectQuery, new String[] {String.valueOf(taskID)});
        if (cursor != null){
            cursor.moveToFirst();
            task = getTask(cursor);
            cursor.close();
        }

        closeDatabase();

        return task;
    }

    protected ArrayList<Date> getHistory(Long taskID, Date month) {
        ArrayList<Date> dateList = new ArrayList<Date>();
        String selectQuery = SELECT_FORM + TaskCompletion.TABLE_NAME + SELECT_ARG_FORM + TaskCompletion.TASK_NAME_ID + "=?";;
        
        Cursor cursor = openDatabase().rawQuery(selectQuery, new String[] {String.valueOf(taskID)});
        SimpleDateFormat sdf_ymd = new SimpleDateFormat(SDF_PATTERN_YMD, Locale.JAPAN);
        SimpleDateFormat sdf_ym = new SimpleDateFormat(SDF_PATTERN_YM, Locale.JAPAN);

        if (cursor.moveToFirst()) {
            do {
                String dateString = cursor.getString(cursor.getColumnIndex(TaskCompletion.TASK_COMPLETION_DATE));
                if (dateString != null) {
                    Date date = null;
                    try {
                        date = sdf_ymd.parse(dateString);
                    } catch (ParseException e) {
                        Log.e(TAG, e.getMessage());
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

    private Task getTask(Cursor cursor) {
        Long taskID = Long.parseLong(cursor.getString(cursor.getColumnIndex(TasksToday._ID)));
        String taskName = cursor.getString(cursor.getColumnIndex(TasksToday.TASK_NAME));
        String taskContext = cursor.getString(cursor.getColumnIndex(TasksToday.TASK_CONTEXT));

        Recurrence recurrence = new Recurrence(Boolean.valueOf(cursor.getString(cursor.getColumnIndex(TasksToday.FREQUENCY_MON))),
                                               Boolean.valueOf(cursor.getString(cursor.getColumnIndex(TasksToday.FREQUENCY_TUE))),
                                               Boolean.valueOf(cursor.getString(cursor.getColumnIndex(TasksToday.FREQUENCY_WEN))),
                                               Boolean.valueOf(cursor.getString(cursor.getColumnIndex(TasksToday.FREQUENCY_THR))),
                                               Boolean.valueOf(cursor.getString(cursor.getColumnIndex(TasksToday.FREQUENCY_FRI))),
                                               Boolean.valueOf(cursor.getString(cursor.getColumnIndex(TasksToday.FREQUENCY_SAT))),
                                               Boolean.valueOf(cursor.getString(cursor.getColumnIndex(TasksToday.FREQUENCY_SUN))));

        Reminder reminder;
        String reminderEnabled = cursor.getString(cursor.getColumnIndex(TasksToday.REMINDER_ENABLED));
        String reminderTime = cursor.getString(cursor.getColumnIndex(TasksToday.REMINDER_TIME));
        if ((reminderEnabled == null) || (reminderTime == null)) {
            reminder = new Reminder();
        } else {
            reminder = new Reminder(Boolean.valueOf(reminderEnabled), Long.valueOf(reminderTime));
        }

        Task task = new Task(taskName, taskContext, recurrence);
        task.setReminder(reminder);
        task.setTaskID(taskID);
        return task;
    }
}