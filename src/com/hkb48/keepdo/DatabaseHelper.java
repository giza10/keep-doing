package com.hkb48.keepdo;

import com.hkb48.keepdo.Database.TaskCompletions;
import com.hkb48.keepdo.Database.Tasks;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "keepdo_tracker.db";
    private static final int DATABASE_VERSION = 1;

	DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Create the Tasks table
		db.execSQL("CREATE TABLE " + Tasks.TASKS_TABLE_NAME + " ("
                + Tasks._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + Tasks.TASK_NAME + " TEXT"
                + Tasks.TASK_FREQUENCY + " TEXT"
                + ");");

		// Create the Completion table
		db.execSQL("CREATE TABLE " + TaskCompletions.TASK_COMPLETION_TABLE_NAME + " ("
                + TaskCompletions._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + TaskCompletions.TASK_NAME_ID + " INTEGER,"
                + TaskCompletions.TASK_COMPLETION_DATE + " TEXT"
                + ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
}