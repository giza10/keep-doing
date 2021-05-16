package com.hkb48.keepdo;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.hkb48.keepdo.KeepdoProvider.TaskCompletion;
import com.hkb48.keepdo.KeepdoProvider.Tasks;

import java.io.File;

final class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "#KEEPDO_DB_HELPER: ";

    private static final String DB_NAME = "keepdo_tracker.db";
    private static DatabaseHelper INSTANCE = null;
    private final Context mContext;

    /*
     * The first version is 1, the latest version is 3
     * Version [1] initial columns
     * Version [2] adding context and reminder column
     * Version [3] adding task order column
     */
    private static final int DB_VERSION = 3;

    private static final String STRING_CREATE_TASK = "CREATE TABLE " + Tasks.TABLE_NAME + " ("
            + Tasks._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
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
            + Tasks.TASK_LIST_ORDER + " INTEGER" + ");";


    private static final String STRING_CREATE_COMPLETION = "CREATE TABLE " + TaskCompletion.TABLE_NAME + " ("
            + TaskCompletion._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TaskCompletion.TASK_NAME_ID + " INTEGER NOT NULL CONSTRAINT "
            + TaskCompletion.TASK_NAME_ID + " REFERENCES "
            + Tasks.TABLE_NAME + "(" + Tasks._ID + ")" + " ON DELETE CASCADE, "
            + TaskCompletion.TASK_COMPLETION_DATE + " DATE" + ");";

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.mContext = context;
    }

    static DatabaseHelper getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DatabaseHelper(context);
        }

        return INSTANCE;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(STRING_CREATE_TASK);
            db.execSQL(STRING_CREATE_COMPLETION);
        } catch (SQLiteException e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Exception: execSQL() : e = " + e);
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Updating database version from " + oldVersion + " to " + newVersion);
        }

        boolean success = false;
        if (oldVersion < newVersion) {
            db.beginTransaction();

            for (int version = oldVersion; version < newVersion; version++) {
                int nextVersion = version + 1;

                switch (nextVersion) {
                    case 2:
                        success = upgradeDatabase2(db);
                        break;
                    case 3:
                        success = upgradeDatabase3(db);
                        break;
                    default:
                        break;
                }

                if (!success) {
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "Error updating database, reverting changes!");
                    break;
                }
            }

            if (success) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Database updated successfully!");
                db.setVersion(newVersion);
                db.setTransactionSuccessful();
            }
            db.endTransaction();

        } else {
            clearDatabase();
            this.onCreate(db);
        }
    }

    /*
     *  The original version is 1, while upgrade to 2.
     */
    private boolean upgradeDatabase2(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + Tasks.TABLE_NAME + " ADD COLUMN " + Tasks.TASK_CONTEXT + " TEXT");
            db.execSQL("ALTER TABLE " + Tasks.TABLE_NAME + " ADD COLUMN " + Tasks.REMINDER_ENABLED + " TEXT");
            db.execSQL("ALTER TABLE " + Tasks.TABLE_NAME + " ADD COLUMN " + Tasks.REMINDER_TIME + " TEXT");
        } catch (SQLException e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Exception: execSQL() : e = " + e);
            }
            return false;
        }

        return true;
    }

    /*
     *  The original version is 2, while upgrade to 3.
     */
    private boolean upgradeDatabase3(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + Tasks.TABLE_NAME + " ADD COLUMN " + Tasks.TASK_LIST_ORDER + " INTEGER DEFAULT 0");
        } catch (SQLException e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Exception: execSQL() : e = " + e);
            }
            return false;
        }

        return true;
    }

    /*
     * Remove the database file.
     */
    private void clearDatabase() {
        final String data_base = mContext.getDatabasePath(DB_NAME).getPath();

        File file = new File(data_base);
        if (file.delete()) if (BuildConfig.DEBUG) Log.d(TAG, "Database removed!");
    }

    final String databasePath() {
        return mContext.getDatabasePath(DatabaseHelper.DB_NAME).getPath();
    }
}