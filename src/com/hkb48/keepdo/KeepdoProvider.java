package com.hkb48.keepdo;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class KeepdoProvider extends ContentProvider {
//    private static final String TAG = "#KEEPDO_PROVIDER: ";
    public static final Uri CONTENT_URI = Database.TasksToday.CONTENT_URI;

    public static class Columns {
        public static final String TASK_ID = Database.TasksToday._ID;
        public static final String TASK_NAME = Database.TasksToday.TASK_NAME;
    }

    private DatabaseHelper mOpenHelper;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
    	sURIMatcher.addURI(Database.AUTHORITY, Database.TasksToday.TABLE_URI, Database.TasksToday.TABLE_LIST);
    	sURIMatcher.addURI(Database.AUTHORITY, Database.TasksToday.TABLE_URI + "/#", Database.TasksToday.TABLE_ID);
    	sURIMatcher.addURI(Database.AUTHORITY, Database.TaskCompletion.TABLE_URI, Database.TaskCompletion.TABLE_LIST);
    	sURIMatcher.addURI(Database.AUTHORITY, Database.TaskCompletion.TABLE_URI + "/#", Database.TaskCompletion.TABLE_ID);
    }

    @Override
    public boolean onCreate() {
        // Assumes that any failures will be reported by a thrown exception.
        mOpenHelper = DatabaseHelper.getInstance(getContext());
        return false;
    }

	@Override
	public String getType(Uri uri) {

		//TODO: have to be corrected
		switch (sURIMatcher.match(uri)) {
		case Database.TasksToday.TABLE_LIST:		        	
        	return String.valueOf(Database.TasksToday.TABLE_LIST);	        	
        case Database.TasksToday.TABLE_ID:
        	return String.valueOf(Database.TasksToday.TABLE_ID);	        	
        case Database.TaskCompletion.TABLE_LIST:		        	
        	return String.valueOf(Database.TaskCompletion.TABLE_LIST);	        	
        case Database.TaskCompletion.TABLE_ID:
        	return String.valueOf(Database.TaskCompletion.TABLE_ID);	        	
        default:
        	throw new IllegalArgumentException("Unknown URI " + uri);
    	}
	}

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // Constructs a new query builder and sets its table name
        String tableName;
        switch (sURIMatcher.match(uri)) {

        case Database.TasksToday.TABLE_LIST:
        case Database.TasksToday.TABLE_ID:
            tableName = Database.TasksToday.TABLE_NAME;
            break;

        case Database.TaskCompletion.TABLE_LIST:
        case Database.TaskCompletion.TABLE_ID:
            tableName = Database.TaskCompletion.TABLE_NAME;
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        assert db != null;
        db.insertOrThrow(tableName, null, values);
        getContext().getContentResolver().notifyChange(uri, null);

        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

        // Constructs a new query builder and sets its table name
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sURIMatcher.match(uri)) {

            case Database.TasksToday.TABLE_LIST:
            case Database.TasksToday.TABLE_ID:
                qb.setTables(Database.TasksToday.TABLE_NAME);
            	break;

            case Database.TaskCompletion.TABLE_LIST:
            case Database.TaskCompletion.TABLE_ID:
            	qb.setTables(Database.TaskCompletion.TABLE_NAME);
            	break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        assert db != null;
        Cursor c = qb.query(db, projection, selection, selectionArgs,
                null /* no group */, null /* no filter */, sortOrder);

        assert c != null;
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        String tableName;
        switch (sURIMatcher.match(uri)) {

        case Database.TasksToday.TABLE_LIST:
        case Database.TasksToday.TABLE_ID:
            tableName = Database.TasksToday.TABLE_NAME;
            break;

        case Database.TaskCompletion.TABLE_LIST:
        case Database.TaskCompletion.TABLE_ID:
            tableName = Database.TaskCompletion.TABLE_NAME;
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        assert db != null;
        final int id = db.update(tableName, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return id;
	}

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String tableName;
        switch (sURIMatcher.match(uri)) {

        case Database.TasksToday.TABLE_LIST:
        case Database.TasksToday.TABLE_ID:
            tableName = Database.TasksToday.TABLE_NAME;
            break;

        case Database.TaskCompletion.TABLE_LIST:
        case Database.TaskCompletion.TABLE_ID:
            tableName = Database.TaskCompletion.TABLE_NAME;
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        assert db != null;
        final int id = db.delete(tableName, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return id;
    }
}