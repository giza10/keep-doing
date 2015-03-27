package com.hkb48.keepdo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.MediaCodec;
import android.os.Environment;
import android.util.Log;

import com.hkb48.keepdo.KeepdoProvider.TaskCompletion;
import com.hkb48.keepdo.KeepdoProvider.Tasks;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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

class DatabaseAdapter {
    private static final String TAG = "#KEEPDO_DB_ADAPTER: ";

    // Backup & Restore
    private static final String BACKUP_FILE_NAME = "/keepdo.db";
	private static final String BACKUP_DIR_NAME = "/keepdo";
    private static final String BACKUP_DIR_PATH = Environment.getExternalStorageDirectory().getPath() + BACKUP_DIR_NAME;

    private static final String SDF_PATTERN_YMD = "yyyy-MM-dd";
    private static final String SDF_PATTERN_YM = "yyyy-MM";

    private static DatabaseAdapter INSTANCE = null;
    private DatabaseHelper mDatabaseHelper = null;
    private final ContentResolver mContentResolver;

    private DatabaseAdapter(Context context) {
        mDatabaseHelper = DatabaseHelper.getInstance(context.getApplicationContext());
        mContentResolver = context.getContentResolver();
    }

    static synchronized DatabaseAdapter getInstance(Context context) {
    	if (INSTANCE == null) {
    		INSTANCE = new DatabaseAdapter(context);
    	}

    	return INSTANCE;
    }

    public synchronized void close() {
		mDatabaseHelper.close();
    }

    public List<Task> getTaskList() {
        List<Task> tasks = new ArrayList<Task>();
        String sortOrder = Tasks.TASK_LIST_ORDER + " asc";
        Cursor cursor = mContentResolver.query(Tasks.CONTENT_URI, null, null,
                null, sortOrder);

        if (cursor.moveToFirst()) {
            do {
                tasks.add(getTask(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();

        return tasks;
    }

    public void addTask(Task task) {
        String taskName = task.getName();
        String taskContext = task.getContext();
        Recurrence recurrence = task.getRecurrence();
        Reminder reminder = task.getReminder();

        if ((taskName == null) || (taskName.isEmpty()) || (recurrence == null) || (reminder == null)) {
            return;
        }

        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Tasks.TASK_NAME, taskName);
            contentValues.put(Tasks.FREQUENCY_MON, String.valueOf(recurrence.getMonday()));
            contentValues.put(Tasks.FREQUENCY_TUE, String.valueOf(recurrence.getTuesday()));
            contentValues.put(Tasks.FREQUENCY_WEN, String.valueOf(recurrence.getWednesday()));
            contentValues.put(Tasks.FREQUENCY_THR, String.valueOf(recurrence.getThurday()));
            contentValues.put(Tasks.FREQUENCY_FRI, String.valueOf(recurrence.getFriday()));
            contentValues.put(Tasks.FREQUENCY_SAT, String.valueOf(recurrence.getSaturday()));
            contentValues.put(Tasks.FREQUENCY_SUN, String.valueOf(recurrence.getSunday()));
            contentValues.put(Tasks.TASK_CONTEXT, taskContext);
            contentValues.put(Tasks.REMINDER_ENABLED, String.valueOf(reminder.getEnabled()));
            contentValues.put(Tasks.REMINDER_TIME, String.valueOf(reminder.getTimeInMillis()));
            contentValues.put(Tasks.TASK_LIST_ORDER, task.getOrder());

            mContentResolver.insert(Tasks.CONTENT_URI, contentValues);

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
        contentValues.put(Tasks.TASK_NAME, taskName);
        contentValues.put(Tasks.FREQUENCY_MON, String.valueOf(recurrence.getMonday()));
        contentValues.put(Tasks.FREQUENCY_TUE, String.valueOf(recurrence.getTuesday()));
        contentValues.put(Tasks.FREQUENCY_WEN, String.valueOf(recurrence.getWednesday()));
        contentValues.put(Tasks.FREQUENCY_THR, String.valueOf(recurrence.getThurday()));
        contentValues.put(Tasks.FREQUENCY_FRI, String.valueOf(recurrence.getFriday()));
        contentValues.put(Tasks.FREQUENCY_SAT, String.valueOf(recurrence.getSaturday()));
        contentValues.put(Tasks.FREQUENCY_SUN, String.valueOf(recurrence.getSunday()));
        contentValues.put(Tasks.TASK_CONTEXT, taskContext);
        contentValues.put(Tasks.REMINDER_ENABLED, String.valueOf(reminder.getEnabled()));
        contentValues.put(Tasks.REMINDER_TIME, String.valueOf(reminder.getTimeInMillis()));
        contentValues.put(Tasks.TASK_LIST_ORDER, task.getOrder());

        String whereClause = Tasks._ID + "=?";
        String whereArgs[] = {taskID.toString()};

        try {
            mContentResolver.update(Tasks.CONTENT_URI, contentValues, whereClause, whereArgs);
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    void deleteTask(Long taskID) {
        // Delete task from TASKS_TABLE_NAME
    	String whereClause = Tasks._ID + "=?";
        String whereArgs[] = {taskID.toString()};
        mContentResolver.delete(Tasks.CONTENT_URI, whereClause, whereArgs);

        // Delete records of deleted task from TASK_COMPLETION_TABLE_NAME
        whereClause = TaskCompletion.TASK_NAME_ID + "=?";
        mContentResolver.delete(TaskCompletion.CONTENT_URI, whereClause, whereArgs);
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
                mContentResolver.insert(TaskCompletion.CONTENT_URI, contentValues);
            } catch (SQLiteException e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            String whereClause = TaskCompletion.TASK_NAME_ID + "=? and " + TaskCompletion.TASK_COMPLETION_DATE + "=?";
            String whereArgs[] = {taskID.toString(), dateFormat.format(date)};
            mContentResolver.delete(TaskCompletion.CONTENT_URI, whereClause, whereArgs);
        }
    }

    boolean getDoneStatus(Long taskID, Date day) {
        boolean isDone = false;
        SimpleDateFormat dateFormat = new SimpleDateFormat(SDF_PATTERN_YMD, Locale.JAPAN);
        String selection = TaskCompletion.TASK_NAME_ID + "=? and " + TaskCompletion.TASK_COMPLETION_DATE + "=?";
        String selectionArgs[] = {String.valueOf(taskID), dateFormat.format(day)};

        Cursor cursor = mContentResolver.query(TaskCompletion.CONTENT_URI, null, selection,
                selectionArgs, null);
        if (cursor.getCount() > 0) {
            isDone =  true;
        }
        cursor.close();

        return isDone;
    }

    public int getNumberOfDone(Long taskID) {
        int numberOfDone = 0;
        String selection = TaskCompletion.TASK_NAME_ID + "=?";
        String selectionArgs[] = {String.valueOf(taskID)};
        Cursor cursor = mContentResolver.query(TaskCompletion.CONTENT_URI, null, selection,
                selectionArgs, null);
        if (cursor != null) {
            numberOfDone = cursor.getCount();
            cursor.close();
        }
        return numberOfDone;
    }

    public Date getFirstDoneDate(Long taskID) {
        final SimpleDateFormat sdf_ymd = new SimpleDateFormat(SDF_PATTERN_YMD, Locale.JAPAN);
        Date date = null;
        String[] projection = { "min(" + TaskCompletion.TASK_COMPLETION_DATE + ")" };
        String selection = TaskCompletion.TASK_NAME_ID + "=?";
        String selectionArgs[] = {String.valueOf(taskID)};
        Cursor cursor = mContentResolver.query(TaskCompletion.CONTENT_URI, projection, selection,
                selectionArgs, null);

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
        return date;
    }

    public Date getLastDoneDate(Long taskID) {
        final SimpleDateFormat sdf_ymd = new SimpleDateFormat(SDF_PATTERN_YMD, Locale.JAPAN);
        Date date = null;
        String[] projection = { "max(" + TaskCompletion.TASK_COMPLETION_DATE + ")" };
        String selection = TaskCompletion.TASK_NAME_ID + "=?";
        String selectionArgs[] = {String.valueOf(taskID)};
        Cursor cursor = mContentResolver.query(TaskCompletion.CONTENT_URI, projection, selection,
                selectionArgs, null);

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
        return date;
    }

    Task getTask(Long taskID) {
        Task task = null;
        String selection = Tasks._ID + "=?";
        String selectionArgs[] = {String.valueOf(taskID)};
        Cursor cursor = mContentResolver.query(Tasks.CONTENT_URI, null, selection,
                selectionArgs, null);
        if (cursor != null){
            cursor.moveToFirst();
            task = getTask(cursor);
            cursor.close();
        }

        return task;
    }

    ArrayList<Date> getHistory(Long taskID, Date month) {
        ArrayList<Date> dateList = new ArrayList<Date>();
        String selection = TaskCompletion.TASK_NAME_ID + "=?";
        String selectionArgs[] = {String.valueOf(taskID)};
        Cursor cursor = mContentResolver.query(TaskCompletion.CONTENT_URI, null, selection,
                selectionArgs, null);
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

        return dateList;
    }

    ComboCount getComboCount(Long taskID) {
    	int currentCount = 0;
    	int maxCount = 0;

        Recurrence recurrence = getTask(taskID).getRecurrence();
        String[] projection = { "distinct " + TaskCompletion.TASK_COMPLETION_DATE };
        String selection = TaskCompletion.TASK_NAME_ID + "=?";
        String selectionArgs[] = {String.valueOf(taskID)};
        String sortOrder = TaskCompletion.TASK_COMPLETION_DATE + " asc";
        Cursor cursor = mContentResolver.query(TaskCompletion.CONTENT_URI, projection, selection,
                selectionArgs, sortOrder);

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
        Long taskID = Long.parseLong(cursor.getString(cursor.getColumnIndex(Tasks._ID)));
        String taskName = cursor.getString(cursor.getColumnIndex(Tasks.TASK_NAME));
        String taskContext = cursor.getString(cursor.getColumnIndex(Tasks.TASK_CONTEXT));

        Recurrence recurrence = new Recurrence(Boolean.valueOf(cursor.getString(cursor.getColumnIndex(Tasks.FREQUENCY_MON))),
                                               Boolean.valueOf(cursor.getString(cursor.getColumnIndex(Tasks.FREQUENCY_TUE))),
                                               Boolean.valueOf(cursor.getString(cursor.getColumnIndex(Tasks.FREQUENCY_WEN))),
                                               Boolean.valueOf(cursor.getString(cursor.getColumnIndex(Tasks.FREQUENCY_THR))),
                                               Boolean.valueOf(cursor.getString(cursor.getColumnIndex(Tasks.FREQUENCY_FRI))),
                                               Boolean.valueOf(cursor.getString(cursor.getColumnIndex(Tasks.FREQUENCY_SAT))),
                                               Boolean.valueOf(cursor.getString(cursor.getColumnIndex(Tasks.FREQUENCY_SUN))));

        Reminder reminder;
        String reminderEnabled = cursor.getString(cursor.getColumnIndex(Tasks.REMINDER_ENABLED));
        String reminderTime = cursor.getString(cursor.getColumnIndex(Tasks.REMINDER_TIME));
        long taskOrder = cursor.getInt(cursor.getColumnIndex(Tasks.TASK_LIST_ORDER));
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
        String[] projection = { "max(" + Tasks.TASK_LIST_ORDER + ")" };
        Cursor cursor = mContentResolver.query(Tasks.CONTENT_URI, projection, null,
                null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            String idString = cursor.getString(0);
            if (idString != null) {
                maxOrderId = Integer.parseInt(idString);
            }
            cursor.close();
        }

        return maxOrderId;
    }

    String getBackupFilePath() {
        return (BACKUP_DIR_PATH + BACKUP_FILE_NAME);
    }

    void backupDataBase() {
        File dir = new File(BACKUP_DIR_PATH);
    	if (!dir.exists()) {
    		if (!dir.mkdir()) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "backup Database failed.");
                }
                return;
            }
    	}

		copyDataBase(mDatabaseHelper.databasePath(), getBackupFilePath());
    }

    void restoreDatabase() {
        copyDataBase(getBackupFilePath(), mDatabaseHelper.databasePath());
    }

    synchronized final FileInputStream readDatabaseStream() {
        FileInputStream inputStream = null;

        try {
            inputStream = new FileInputStream(mDatabaseHelper.databasePath());
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }

        return inputStream;
    }

    synchronized void writeDatabaseStream(final InputStream in) {
        BufferedOutputStream bufferOutStream = null;
        BufferedInputStream bufferInputStream = null;

         try {
             OutputStream outputStream = new FileOutputStream(mDatabaseHelper.databasePath());
             bufferOutStream = new BufferedOutputStream(outputStream);

             bufferInputStream = new BufferedInputStream(in);
             byte[] buffer = new byte[1024];

             while (bufferInputStream.read(buffer) >= 0) {
                bufferOutStream.write(buffer);
             }

             bufferOutStream.flush();
         } catch (IOException e) {
             Log.e(TAG, e.getStackTrace().toString());
         } finally {
             if (bufferOutStream != null) {
                 try {
                     bufferOutStream.close();
                 } catch (IOException e) {
                     Log.e(TAG, e.getMessage());
                 }
             }

             if (bufferInputStream != null) {
                 try {
                     bufferInputStream.close();
                 } catch (IOException e) {
                     Log.e(TAG, e.getMessage());
                 }
             }
         }
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

    String backupFileName() {
    	return BACKUP_FILE_NAME;
    }

    String backupDirName() {
    	return BACKUP_DIR_NAME;
    }

    String backupDirPath() {
    	return BACKUP_DIR_PATH;
    }
}