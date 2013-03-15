package com.hkb48.keepdo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.hkb48.keepdo.Database.TaskCompletion;
import com.hkb48.keepdo.Database.TasksToday;

class DatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = "#KEEPDO_DB_HELPER: ";
    private static final String DB_NAME = "keepdo_tracker.db";

    /*
     * The first version is 1, the latest version is 2
     * version 1: initial columns
     * version 2: adding context and reminder column
     */
    private static final int DB_VERSION = 2;

    private static final String STRING_CREATE_TASK = "CREATE TABLE " + TasksToday.TABLE_NAME + " ("
                                                     + TasksToday._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                                                     + TasksToday.TASK_NAME + " TEXT NOT NULL, "
                                                     + TasksToday.FREQUENCY_MON + " TEXT, "
                                                     + TasksToday.FREQUENCY_TUE + " TEXT, "
                                                     + TasksToday.FREQUENCY_WEN + " TEXT, "
                                                     + TasksToday.FREQUENCY_THR + " TEXT, "
                                                     + TasksToday.FREQUENCY_FRI + " TEXT, "
                                                     + TasksToday.FREQUENCY_SAT + " TEXT, "
                                                     + TasksToday.FREQUENCY_SUN + " TEXT,"
                                                     + TasksToday.TASK_CONTEXT + " TEXT,"
                                                     + TasksToday.REMINDER_ENABLED + " TEXT,"
                                                     + TasksToday.REMINDER_TIME + " TEXT" + ");";

    private static final String STRING_CREATE_COMPLETION = "CREATE TABLE " + TaskCompletion.TABLE_NAME + " ("
                                                     + TaskCompletion._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                                                     + TaskCompletion.TASK_NAME_ID + " INTEGER NOT NULL CONSTRAINT "
                                                     + TaskCompletion.TASK_NAME_ID + " REFERENCES "
                                                     + TasksToday.TABLE_NAME + "(" + TasksToday._ID + ")" + " ON DELETE CASCADE, "
                                                     + TaskCompletion.TASK_COMPLETION_DATE + " DATE" + ");";

    DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	try {
            db.execSQL(STRING_CREATE_TASK);
            db.execSQL(STRING_CREATE_COMPLETION);
        } catch (SQLiteException e) {
            if (BuildConfig.DEBUG) {
            	Log.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Updating database version from " + oldVersion + " to " + newVersion);
        }

    	/*
    	 *  The initial version is 1, while updated to the latest version to 2.  
    	 */
    	if (oldVersion < 2) {
    		db.beginTransaction();
    		try {
	    		db.execSQL("ALTER TABLE " + TasksToday.TABLE_NAME + " ADD COLUMN " + TasksToday.TASK_CONTEXT + " TEXT");
	    		db.execSQL("ALTER TABLE " + TasksToday.TABLE_NAME + " ADD COLUMN " + TasksToday.REMINDER_ENABLED + " TEXT");
                db.execSQL("ALTER TABLE " + TasksToday.TABLE_NAME + " ADD COLUMN " + TasksToday.REMINDER_TIME + " TEXT");		

	    		db.setVersion(newVersion);
	            db.setTransactionSuccessful();
    		} finally {
    			db.endTransaction();
    		}
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }
}