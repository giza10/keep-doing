package com.hkb48.keepdo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.hkb48.keepdo.Database.TaskCompletion;
import com.hkb48.keepdo.Database.TasksToday;

class DatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = "#KEEPDO_DB_HELPER: ";
    private static final String DB_NAME = "keepdo_tracker.db";
    private final Context mContext;

    /*
     * The first version is 1, the latest version is 3
     * Version [1] initial columns
     * Version [2] adding context and reminder column
     * Version [3] adding task order column
     */
    private static final int DB_VERSION = 3;

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
                                                     + TasksToday.REMINDER_TIME + " TEXT,"
    												 + TasksToday.TASK_LIST_ORDER + " INTEGER" + ");";


    private static final String STRING_CREATE_COMPLETION = "CREATE TABLE " + TaskCompletion.TABLE_NAME + " ("
                                                     + TaskCompletion._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                                                     + TaskCompletion.TASK_NAME_ID + " INTEGER NOT NULL CONSTRAINT "
                                                     + TaskCompletion.TASK_NAME_ID + " REFERENCES "
                                                     + TasksToday.TABLE_NAME + "(" + TasksToday._ID + ")" + " ON DELETE CASCADE, "
                                                     + TaskCompletion.TASK_COMPLETION_DATE + " DATE" + ");";

    DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.mContext = context;
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
	    		
	    		if (!success && BuildConfig.DEBUG) {
    		    	Log.d(TAG, "Error updating database, reverting changes!");
    		    	break;
    		    }
	    	}

    		if (success) {
	    		if (BuildConfig.DEBUG) {
    		    	Log.d(TAG, "Database updated successfully!");
    		    }

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
    		db.execSQL("ALTER TABLE " + TasksToday.TABLE_NAME + " ADD COLUMN " + TasksToday.TASK_CONTEXT + " TEXT");
    		db.execSQL("ALTER TABLE " + TasksToday.TABLE_NAME + " ADD COLUMN " + TasksToday.REMINDER_ENABLED + " TEXT");
    		db.execSQL("ALTER TABLE " + TasksToday.TABLE_NAME + " ADD COLUMN " + TasksToday.REMINDER_TIME + " TEXT");
   		} catch (SQLException eSQL) {
            if (BuildConfig.DEBUG) {
            	Log.e(TAG, eSQL.getMessage());
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
    		db.execSQL("ALTER TABLE " + TasksToday.TABLE_NAME + " ADD COLUMN " + TasksToday.TASK_LIST_ORDER + " INTEGER DEFAULT 0");
   		} catch (SQLException eSQL) {
            if (BuildConfig.DEBUG) {
            	Log.e(TAG, eSQL.getMessage());
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
    	if (file.delete() && (BuildConfig.DEBUG)) {
    		Log.d(TAG, "Database removded!");
	    }
    }
    
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    /**
     * Backup and Restore database.
     * @param backupPath
     * @param backupFileName
     */
    public void backupDataBase(String backupDirPath, String backupFileName) {
    	File dir = new File(backupDirPath);

    	if (!dir.exists()) {
    		dir.mkdir();
    	}

    	final String DB_FILE_PATH = mContext.getDatabasePath(DB_NAME).getPath();
		copyDataBase(DB_FILE_PATH, backupDirPath + backupFileName);
    }

    public void restoreDataBase(String backupPath) {
    	final String DB_FILE_PATH = mContext.getDatabasePath(DB_NAME).getPath();
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