package com.hkb48.keepdo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.hkb48.keepdo.Database.TaskCompletion;
import com.hkb48.keepdo.Database.TasksToday;

class DatabaseAdapter {
    private static final String TAG = "#KEEPDO_DB_ADAPTER: ";
    private static final String SDF_PATTERN_YMD = "yyyy-MM-dd"; 
    private static final String SDF_PATTERN_YM = "yyyy-MM";
    private static final String SELECT_FORM = "select * from ";
    private static final String SELECT_ARG_FORM = " where ";

    private static DatabaseAdapter INSTANCE = null;
    private DatabaseHelper mDatabaseHelper = null;
    private SQLiteDatabase mDatabase = null;

    mDatabaseHelper = DatabaseHelper.getInstance(context.getApplicationContext());
}

    static synchronized DatabaseAdapter getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DatabaseAdapter(context);


    private DatabaseAdapter(Context context) {
    	}

    	return INSTANCE;
    }

    private SQLiteDatabase openDatabase() {
    	synchronized (this) {
	    	try {
	    		mDatabase = mDatabaseHelper.getWritableDatabase();
	    	} catch (SQLiteException e) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, e.getLocalizedMessage());
                }
	    		throw e;
	    	}
    	}
 
    	return mDatabase;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
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
        String selectQuery = SELECT_FORM + TasksToday.TABLE_NAME
                + " order by " + TasksToday.TASK_LIST_ORDER + " asc;";

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

    public void addTask(Task task) {
        long rowID = Task.INVALID_TASKID;
        String taskName = task.getName();
        String taskContext = task.getContext();
        Recurrence recurrence = task.getRecurrence();
        Reminder reminder = task.getReminder();

        if ((taskName == null) || (taskName.isEmpty()) || (recurrence == null) || (reminder == null)) {
            return;
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
            contentValues.put(TasksToday.TASK_LIST_ORDER, task.getOrder());

            rowID = openDatabase().insertOrThrow(TasksToday.TABLE_NAME, null, contentValues);
            closeDatabase();
            
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    void editTask(Task task) {
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
        contentValues.put(TasksToday.TASK_LIST_ORDER, task.getOrder());

        String whereClause = TasksToday._ID + "=?";
        String whereArgs[] = {taskID.toString()};

        try {
        	openDatabase().update(TasksToday.TABLE_NAME, contentValues, whereClause, whereArgs);
        	closeDatabase();
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    void deleteTask(Long taskID) {
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

	void setDoneStatus(Long taskID, Date date, Boolean doneSwitch) {
        if (taskID == null) {
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(SDF_PATTERN_YMD, Locale.JAPAN);

        if (doneSwitch != null && doneSwitch) {
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

    boolean getDoneStatus(Long taskID, Date day) {
        boolean isDone = false;
        String selectQuery = SELECT_FORM + TaskCompletion.TABLE_NAME + SELECT_ARG_FORM + TaskCompletion.TASK_NAME_ID + "=?";
        Cursor cursor = openDatabase().rawQuery(selectQuery, new String[] {String.valueOf(taskID)});
        SimpleDateFormat sdf_ymd = new SimpleDateFormat(SDF_PATTERN_YMD, Locale.JAPAN);

        if (cursor.moveToFirst()) {
            do {
                Date date = getDate(cursor);
                if (date != null) {
                    if (sdf_ymd.format(date).equals(sdf_ymd.format(day))) {
                        isDone = true;
                        break;
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

    public Date getFirstDoneDate(Long taskID) {
        final SimpleDateFormat sdf_ymd = new SimpleDateFormat(SDF_PATTERN_YMD, Locale.JAPAN);
        Date date = null;
        String selectQuery =  "select min(" + TaskCompletion.TASK_COMPLETION_DATE + ") from "
                + TaskCompletion.TABLE_NAME + SELECT_ARG_FORM + TaskCompletion.TASK_NAME_ID + "=?";
        Cursor cursor = openDatabase().rawQuery(selectQuery, new String[] {String.valueOf(taskID)});
        if (cursor != null) {
            cursor.moveToFirst();
            String dateString = cursor.getString(0);
            if (dateString != null) {
                try {
                    date = sdf_ymd.parse(dateString);
                } catch (ParseException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
            cursor.close();
        }
        closeDatabase();
        return date;
    }

    public Date getLastDoneDate(Long taskID) {
        final SimpleDateFormat sdf_ymd = new SimpleDateFormat(SDF_PATTERN_YMD, Locale.JAPAN);
        Date date = null;
        String selectQuery =  "select max(" + TaskCompletion.TASK_COMPLETION_DATE + ") from "
                + TaskCompletion.TABLE_NAME + SELECT_ARG_FORM + TaskCompletion.TASK_NAME_ID + "=?";
        Cursor cursor = openDatabase().rawQuery(selectQuery, new String[] {String.valueOf(taskID)});
        if (cursor != null) {
            cursor.moveToFirst();
            String dateString = cursor.getString(0);
            if (dateString != null) {
                try {
                    date = sdf_ymd.parse(dateString);
                } catch (ParseException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
            cursor.close();
        }
        closeDatabase();
        return date;
    }

    Task getTask(Long taskID) {
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

    ArrayList<Date> getHistory(Long taskID, Date month) {
        ArrayList<Date> dateList = new ArrayList<Date>();
        String selectQuery = SELECT_FORM + TaskCompletion.TABLE_NAME + SELECT_ARG_FORM + TaskCompletion.TASK_NAME_ID + "=?";

        Cursor cursor = openDatabase().rawQuery(selectQuery, new String[] {String.valueOf(taskID)});
        SimpleDateFormat sdf_ym = new SimpleDateFormat(SDF_PATTERN_YM, Locale.JAPAN);

        if (cursor.moveToFirst()) {
            do {
                Date date = getDate(cursor);
                if (date != null) {
                    if (sdf_ym.format(date).equals(sdf_ym.format(month))) {
                        dateList.add(date);
                    }
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        closeDatabase();
        
        return dateList;
    }

    ComboCount getComboCount(Long taskID) {
    	int currentCount = 0;
    	int maxCount = 0;

        // Task Recurrence
        Recurrence recurrence = getTask(taskID).getRecurrence();

    	// execute SQL to get task completion dates
        String selectQuery = SELECT_FORM + TaskCompletion.TABLE_NAME + SELECT_ARG_FORM + TaskCompletion.TASK_NAME_ID + "=?" 
        					+ " order by " + TaskCompletion.TASK_COMPLETION_DATE + " asc;";
        Cursor cursor = openDatabase().rawQuery(selectQuery, new String[] {String.valueOf(taskID)});

        // Count
        if (cursor != null) {
        	if (cursor.moveToFirst()) {
                Calendar calToday = getCalendar(DateChangeTimeUtil.getDate());
        		Calendar calDone = getCalendar(getDate(cursor));
        		Calendar calIndex = (Calendar)calDone.clone();
        		boolean isCompleted = false;
            	do {
            		if (calIndex.equals(calDone)) {
            			// count up combo
            			currentCount ++;
        				if (currentCount > maxCount) {
            				maxCount = currentCount;
        				}
            			if (cursor.moveToNext()) {
            				calDone = getCalendar(getDate(cursor));
            			} else {
            				isCompleted = true;
            			}
            			calIndex.add(Calendar.DAY_OF_MONTH, 1);
            		} else {
            			if (recurrence.isValidDay(calIndex.get(Calendar.DAY_OF_WEEK))) {
            				// stop combo
            				if (!calIndex.equals(calToday)) {
            					currentCount = 0;
            				}
            				if (!isCompleted) {
            					calIndex = (Calendar)calDone.clone();
            				} else {
                    			calIndex.add(Calendar.DAY_OF_MONTH, 1);
            				}
            			} else {
                			calIndex.add(Calendar.DAY_OF_MONTH, 1);
            			}
            		}
            	} while (!calIndex.after(calToday));
        	}
        	cursor.close();
        }

        closeDatabase();

        return new ComboCount(currentCount, maxCount);
    }

    private Date getDate(Cursor cursor) {
        String dateString = cursor.getString(cursor.getColumnIndex(TaskCompletion.TASK_COMPLETION_DATE));
        SimpleDateFormat sdf_ymd = new SimpleDateFormat(SDF_PATTERN_YMD, Locale.JAPAN);
        Date date = null;
        if (dateString != null) {
            try {
                date = sdf_ymd.parse(dateString);
            } catch (ParseException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return date;
    }

    private Calendar getCalendar(Date date) {
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(date);
    	return calendar;
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
        long taskOrder = cursor.getInt(cursor.getColumnIndex(TasksToday.TASK_LIST_ORDER));
        if ((reminderEnabled == null) || (reminderTime == null)) {
            reminder = new Reminder();
        } else {
            reminder = new Reminder(Boolean.valueOf(reminderEnabled), Long.valueOf(reminderTime));
        }

        Task task = new Task(taskName, taskContext, recurrence);
        task.setReminder(reminder);
        task.setTaskID(taskID);
        task.setOrder(taskOrder);
        return task;
    }

    public int getMaxSortOrderId() {
        int maxOrderId = 0;
        String selectQuery = "select max(" + TasksToday.TASK_LIST_ORDER +") from " + TasksToday.TABLE_NAME;
        Cursor cursor = openDatabase().rawQuery(selectQuery, null);
        if (cursor != null) {
            cursor.moveToFirst();
            String idString = cursor.getString(0);
            if (idString != null) {
                maxOrderId = Integer.parseInt(idString);
            }
            cursor.close();
        }
        closeDatabase();

        return maxOrderId;
    }

    /**
     * Backup and Restore database.
     * @param backupDirPath
     * @param backupFileName
     */
    void backupDataBase(String backupDirPath, String backupFileName) {
    	File dir = new File(backupDirPath);

    	if (!dir.exists()) {
            if (!dir.mkdir()) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "backDataBase failed.");
                }
            }
        }

    	final String DB_FILE_PATH = mDatabaseHelper.databasePath();
		copyDataBase(DB_FILE_PATH, backupDirPath + backupFileName);
    }

    void restoreDataBase(String backupPath) {
    	final String DB_FILE_PATH = mDatabaseHelper.databasePath();
    	copyDataBase(backupPath, DB_FILE_PATH);
    }

    private synchronized void copyDataBase(String fromPath, String toPath) {
		InputStream inputStream = null;
		OutputStream outputStream = null;

		try {
			inputStream = new FileInputStream(fromPath);
			outputStream = new FileOutputStream(toPath);
			
	        byte[] buffer = new byte[1024];
	        int length;

			while ((length = inputStream.read(buffer)) > 0) {
			    outputStream.write(buffer, 0, length);
			}    	        
	        outputStream.flush();

		} catch (IOException e) {
            if (BuildConfig.DEBUG) {
            	Log.e(TAG, e.getMessage());
            }
		} finally {
			try {
    	         if (outputStream != null) {
    	        	 outputStream.close();
    	         }

    	         if (inputStream != null) {
    	        	 inputStream.close();
    	         }
			} catch (IOException e) {
                if (BuildConfig.DEBUG) {
                	Log.e(TAG, e.getMessage());
                }
			}
		}
	}
}