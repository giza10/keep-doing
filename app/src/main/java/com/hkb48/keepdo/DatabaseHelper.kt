package com.hkb48.keepdo

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import com.hkb48.keepdo.KeepdoProvider.TaskCompletion
import com.hkb48.keepdo.KeepdoProvider.Tasks
import java.io.File

internal class DatabaseHelper(private val mContext: Context) : SQLiteOpenHelper(
    mContext, DB_NAME, null, DB_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        try {
            db.execSQL(STRING_CREATE_TASK)
            db.execSQL(STRING_CREATE_COMPLETION)
        } catch (e: SQLiteException) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Updating database version from $oldVersion to $newVersion")
        }
        var success = false
        if (oldVersion < newVersion) {
            db.beginTransaction()
            for (version in oldVersion until newVersion) {
                when (version + 1) {
                    2 -> success = upgradeDatabase2(db)
                    3 -> success = upgradeDatabase3(db)
                    else -> {
                    }
                }
                if (!success) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "Error updating database, reverting changes!")
                    break
                }
            }
            if (success) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Database updated successfully!")
                db.version = newVersion
                db.setTransactionSuccessful()
            }
            db.endTransaction()
        } else {
            clearDatabase()
            onCreate(db)
        }
    }

    /*
     *  The original version is 1, while upgrade to 2.
     */
    private fun upgradeDatabase2(db: SQLiteDatabase): Boolean {
        try {
            db.execSQL("ALTER TABLE " + Tasks.TABLE_NAME + " ADD COLUMN " + Tasks.TASK_CONTEXT + " TEXT")
            db.execSQL("ALTER TABLE " + Tasks.TABLE_NAME + " ADD COLUMN " + Tasks.REMINDER_ENABLED + " TEXT")
            db.execSQL("ALTER TABLE " + Tasks.TABLE_NAME + " ADD COLUMN " + Tasks.REMINDER_TIME + " TEXT")
        } catch (e: SQLException) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
            return false
        }
        return true
    }

    /*
     *  The original version is 2, while upgrade to 3.
     */
    private fun upgradeDatabase3(db: SQLiteDatabase): Boolean {
        try {
            db.execSQL("ALTER TABLE " + Tasks.TABLE_NAME + " ADD COLUMN " + Tasks.TASK_LIST_ORDER + " INTEGER DEFAULT 0")
        } catch (e: SQLException) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
            return false
        }
        return true
    }

    /*
     * Remove the database file.
     */
    private fun clearDatabase() {
        val db = mContext.getDatabasePath(DB_NAME).path
        val file = File(db)
        if (file.delete()) if (BuildConfig.DEBUG) Log.d(TAG, "Database removed!")
    }

    fun databasePath(): String {
        return mContext.getDatabasePath(DB_NAME).path
    }

    companion object {
        private const val TAG = "#KEEPDO_DB_HELPER: "
        private const val DB_NAME = "keepdo_tracker.db"

        /*
         * The first version is 1, the latest version is 3
         * Version [1] initial columns
         * Version [2] adding context and reminder column
         * Version [3] adding task order column
         */
        private const val DB_VERSION = 3
        private const val STRING_CREATE_TASK = ("CREATE TABLE " + Tasks.TABLE_NAME + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Tasks.TASK_NAME + " TEXT NOT NULL, "
                + Tasks.FREQUENCY_MON + " TEXT, "
                + Tasks.FREQUENCY_TUE + " TEXT, "
                + Tasks.FREQUENCY_WEN + " TEXT, "
                + Tasks.FREQUENCY_THR + " TEXT, "
                + Tasks.FREQUENCY_FRI + " TEXT, "
                + Tasks.FREQUENCY_SAT + " TEXT, "
                + Tasks.FREQUENCY_SUN + " TEXT,"
                + Tasks.TASK_CONTEXT + " TEXT,"
                + Tasks.REMINDER_ENABLED + " TEXT,"
                + Tasks.REMINDER_TIME + " TEXT,"
                + Tasks.TASK_LIST_ORDER + " INTEGER" + ");")
        private const val STRING_CREATE_COMPLETION =
            ("CREATE TABLE " + TaskCompletion.TABLE_NAME + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TaskCompletion.TASK_NAME_ID + " INTEGER NOT NULL CONSTRAINT "
                    + TaskCompletion.TASK_NAME_ID + " REFERENCES "
                    + Tasks.TABLE_NAME + "(" + BaseColumns._ID + ")" + " ON DELETE CASCADE, "
                    + TaskCompletion.TASK_COMPLETION_DATE + " DATE" + ");")
    }
}