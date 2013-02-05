package com.hkb48.keepdo;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class KeepdoProvider extends ContentProvider {
    private static final String TAG = "#KEEPDO_PROVIDER: ";

    private DatabaseHelper mOpenHelper;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
    	sURIMatcher.addURI(Database.AUTHORITY, Database.TasksToday.TABLE_URI, Database.TasksToday.TABLE_LIST);
    	sURIMatcher.addURI(Database.AUTHORITY, Database.TasksToday.TABLE_URI + "/#", Database.TasksToday.TABLE_ID);
    	sURIMatcher.addURI(Database.AUTHORITY, Database.TaskCompletions.TABLE_URI, Database.TaskCompletions.TABLE_LIST);
    	sURIMatcher.addURI(Database.AUTHORITY, Database.TaskCompletions.TABLE_URI + "/#", Database.TaskCompletions.TABLE_ID);
    }

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {

		//TODO: have to be corrected
		switch (sURIMatcher.match(uri)) {
		case Database.TasksToday.TABLE_LIST:		        	
        	return String.valueOf(Database.TasksToday.TABLE_LIST);	        	
        case Database.TasksToday.TABLE_ID:
        	return String.valueOf(Database.TasksToday.TABLE_ID);	        	
        case Database.TaskCompletions.TABLE_LIST:		        	
        	return String.valueOf(Database.TaskCompletions.TABLE_LIST);	        	
        case Database.TaskCompletions.TABLE_ID:
        	return String.valueOf(Database.TaskCompletions.TABLE_ID);	        	
        default:
        	throw new IllegalArgumentException("Unknown URI " + uri);
    	}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
        // Constructs a new query builder and sets its table name
		String tableName = null;
  		switch (sURIMatcher.match(uri)) {
        
        case Database.TasksToday.TABLE_LIST:
        case Database.TasksToday.TABLE_ID:
        	tableName = Database.TasksToday.TABLE_NAME;
        	break;

        case Database.TaskCompletions.TABLE_LIST:
        case Database.TaskCompletions.TABLE_ID:
        	tableName = Database.TaskCompletions.TABLE_NAME;
        	break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
  		
  		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
	    final long id = db.insertOrThrow(tableName, null, values);
	    final Uri newUri = Uri.parse("random" + id);
	    getContext().getContentResolver().notifyChange(newUri, null);
	
	    return newUri;
	}

	@Override
	public boolean onCreate() {
        // Assumes that any failures will be reported by a thrown exception.
		mOpenHelper = new DatabaseHelper(getContext());
		return false;
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

            case Database.TaskCompletions.TABLE_LIST:
            case Database.TaskCompletions.TABLE_ID:
            	qb.setTables(Database.TaskCompletions.TABLE_NAME);
            	break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        
        Cursor c = qb.query(db, projection, selection, selectionArgs,
                null /* no group */, null /* no filter */, sortOrder);

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
}