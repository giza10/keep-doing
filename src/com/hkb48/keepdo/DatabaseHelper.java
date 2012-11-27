package com.hkb48.keepdo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hkb48.keepdo.Database.TaskCompletions;
import com.hkb48.keepdo.Database.TasksToday;

class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "keepdo_tracker.db";
    private static final int DATABASE_VERSION = 1;
    private static final String STRING_CREATE_TASK = "CREATE TABLE " + TasksToday.TASKS_TABLE_NAME + " ("
    												 + TasksToday._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
    												 + TasksToday.TASK_NAME + " TEXT"
    												 + TasksToday.FREQUENCY_MON + " TEXT"
    												 + TasksToday.FREQUENCY_TUE + " TEXT"
    												 + TasksToday.FREQUENCY_WEN + " TEXT"
    												 + TasksToday.FREQUENCY_THR + " TEXT"
    												 + TasksToday.FREQUENCY_FRI + " TEXT"
    												 + TasksToday.FREQUENCY_SAT + " TEXT"
    												 + TasksToday.FREQUENCY_SUN + " TEXT" + ");";

    private static final String STRING_CREATE_COMPLETION = "CREATE TABLE " + TaskCompletions.TASK_COMPLETION_TABLE_NAME + " ("
    												 + TaskCompletions._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
    												 + TaskCompletions.TASK_NAME_ID + " INTEGER,"
    												 + TaskCompletions.TASK_COMPLETION_DATE + " DATE"
    												 + ");";
    
	DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Create the Tasks table
		db.execSQL(STRING_CREATE_TASK);

		// Create the Completion table
		db.execSQL(STRING_CREATE_COMPLETION);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS "+TasksToday.TASKS_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS "+TaskCompletions.TASK_COMPLETION_TABLE_NAME);
		onCreate(db);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
}