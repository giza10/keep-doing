package com.hkb48.keepdo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.hkb48.keepdo.Database.TaskCompletions;
import com.hkb48.keepdo.Database.TasksToday;

class DatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = "#KEEPDO_DBHELP: ";
    private static final String DB_PATH = "/data/data/com.hkb48.keepdo/databases/";
    private static final String DB_NAME = "keepdo_tracker.db";

    /*
     * The first version is 2, the latest version is 3;
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
                                                     + TasksToday.REMINDER_TIME_HOUR + " TEXT,"
                                                     + TasksToday.REMINDER_TIME_MINUTE + " TEXT" + ");";

    private static final String STRING_CREATE_COMPLETION = "CREATE TABLE " + TaskCompletions.TABLE_NAME + " ("
                                                     + TaskCompletions._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                                                     + TaskCompletions.TASK_NAME_ID + " INTEGER NOT NULL CONSTRAINT "
                                                     + TaskCompletions.TASK_NAME_ID + " REFERENCES "
                                                     + TasksToday.TABLE_NAME + "(" + TasksToday._ID + ")" + " ON DELETE CASCADE, "
                                                     + TaskCompletions.TASK_COMPLETION_DATE + " DATE" + ");";

    private final Context mContext;

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
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	Log.d(TAG, "Updating database version from " + oldVersion + " to " + newVersion);
    	
    	/*
    	 *  The initial version is 2, while updated to the latest version to 4.  
    	 */
    	if (oldVersion < 3) {
    		db.beginTransaction();
    		try {
	    		db.execSQL("ALTER TABLE " + TasksToday.TABLE_NAME + " ADD COLUMN " + TasksToday.TASK_CONTEXT + " TEXT");
	    		db.execSQL("ALTER TABLE " + TasksToday.TABLE_NAME + " ADD COLUMN " + TasksToday.REMINDER_ENABLED + " TEXT");
	    		db.execSQL("ALTER TABLE " + TasksToday.TABLE_NAME + " ADD COLUMN " + TasksToday.REMINDER_TIME_HOUR + " TEXT");
	    		db.execSQL("ALTER TABLE " + TasksToday.TABLE_NAME + " ADD COLUMN " + TasksToday.REMINDER_TIME_MINUTE + " TEXT");    		

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

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     */
    public void createDataBase() throws IOException {
        boolean dbExist = checkDataBase();
        if (dbExist) {
            //do nothing - database already exist
        } else {
            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();
            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            String dbPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch(SQLiteException e) {
            //database does't exist yet.
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null ? true : false;
    }

    /**
      * Copies your database from your local assets-folder to the just created empty database in the
      * system folder, from where it can be accessed and handled.
      * This is done by transferring bytestream.
      */
    private void copyDataBase() throws IOException {
        //Open your local db as the input stream
        InputStream inputStream = mContext.getAssets().open(DB_NAME);
        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;
        //Open the empty db as the output stream
        OutputStream outputStream = new FileOutputStream(outFileName);
        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }     	//Close the streams
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }
}