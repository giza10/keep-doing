package com.hkb48.keepdo;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class KeepdoProvider extends ContentProvider {
    static final String TAG = "#KEEPDOPROVIDER : ";

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
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
                qb.setTables(Database.TasksToday.TABLE_NAME);
            	break;

            case Database.TaskCompletions.TABLE_LIST:
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