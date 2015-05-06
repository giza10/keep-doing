package com.hkb48.keepdo;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class KeepdoProvider extends ContentProvider {
//    private static final String TAG = "#KEEPDO_PROVIDER: ";
    public static final String AUTHORITY = "com.hkb48.keepdo.keepdoprovider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // Task table
    public static final class Tasks implements BaseColumns {

        private Tasks() {}

        // Incoming URI matches the main table URI pattern
        public static final int TABLE_LIST = 10;
        // Incoming URI matches the main table row ID URI pattern
        public static final int TABLE_ID = 20;

        public static final String TABLE_NAME = "table_tasks";
        public static final String TABLE_URI = "table_task_uri";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_URI);

        public static final String TASK_NAME = "task_name";
        public static final String FREQUENCY_MON = "mon_frequency";
        public static final String FREQUENCY_TUE = "tue_frequency";
        public static final String FREQUENCY_WEN = "wen_frequency";
        public static final String FREQUENCY_THR = "thr_frequency";
        public static final String FREQUENCY_FRI = "fri_frequency";
        public static final String FREQUENCY_SAT = "sat_frequency";
        public static final String FREQUENCY_SUN = "sun_frequency";
        public static final String TASK_CONTEXT = "task_context";

        public static final String REMINDER_ENABLED = "reminder_enabled";
        public static final String REMINDER_TIME = "reminder_time";
        public static final String TASK_LIST_ORDER = "task_list_order";
    }

    // Task Completion table
    public static final class TaskCompletion implements BaseColumns {

        private TaskCompletion() {}

        // Incoming URI matches the main table URI pattern
        public static final int TABLE_LIST = 30;
        // Incoming URI matches the main table row ID URI pattern
        public static final int TABLE_ID = 40;

        public static final String TABLE_NAME = "table_completions";
        public static final String TABLE_URI = "table_completion_uri";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_URI);

        public static final String TASK_NAME_ID = "task_id";
        public static final String TASK_COMPLETION_DATE = "completion_date";
    }

    public static final class DateChangeTime implements BaseColumns {
        // Incoming URI matches the main table URI pattern
        public static final int TABLE_LIST = 50;
        // Incoming URI matches the main table row ID URI pattern
        public static final int TABLE_ID = 60;

        public static final String TABLE_URI = "table_datechangetime_uri";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_URI);
        public static final String ADJUSTED_DATE = "date";
        public static final String NEXT_DATE_CHANGE_TIME = "next_date_change_time";
    }

    private DatabaseHelper mOpenHelper;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, Tasks.TABLE_URI, Tasks.TABLE_LIST);
        sURIMatcher.addURI(AUTHORITY, Tasks.TABLE_URI + "/#", Tasks.TABLE_ID);
        sURIMatcher.addURI(AUTHORITY, TaskCompletion.TABLE_URI, TaskCompletion.TABLE_LIST);
        sURIMatcher.addURI(AUTHORITY, TaskCompletion.TABLE_URI + "/#", TaskCompletion.TABLE_ID);
        sURIMatcher.addURI(AUTHORITY, DateChangeTime.TABLE_URI, DateChangeTime.TABLE_LIST);
        sURIMatcher.addURI(AUTHORITY, DateChangeTime.TABLE_URI + "/#", DateChangeTime.TABLE_ID);
    }

    @Override
    public boolean onCreate() {
        // Assumes that any failures will be reported by a thrown exception.
        final Context context = getContext();
        mOpenHelper = DatabaseHelper.getInstance(context);
        Settings.initialize(context);
        return false;
    }

	@Override
	public String getType(Uri uri) {

		//TODO: have to be corrected
		switch (sURIMatcher.match(uri)) {
		case Tasks.TABLE_LIST:	
        	return String.valueOf(Tasks.TABLE_LIST);
        case Tasks.TABLE_ID:
        	return String.valueOf(Tasks.TABLE_ID);
        case TaskCompletion.TABLE_LIST:
        	return String.valueOf(TaskCompletion.TABLE_LIST);
        case TaskCompletion.TABLE_ID:
        	return String.valueOf(TaskCompletion.TABLE_ID);
        default:
        	throw new IllegalArgumentException("Unknown URI " + uri);
    	}
	}

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // Constructs a new query builder and sets its table name
        String tableName;
        switch (sURIMatcher.match(uri)) {

        case Tasks.TABLE_LIST:
        case Tasks.TABLE_ID:
            tableName = Tasks.TABLE_NAME;
            break;

        case TaskCompletion.TABLE_LIST:
        case TaskCompletion.TABLE_ID:
            tableName = TaskCompletion.TABLE_NAME;
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

            case Tasks.TABLE_LIST:
            case Tasks.TABLE_ID:
                qb.setTables(Tasks.TABLE_NAME);
            	break;

            case TaskCompletion.TABLE_LIST:
            case TaskCompletion.TABLE_ID:
            	qb.setTables(TaskCompletion.TABLE_NAME);
            	break;

            case DateChangeTime.TABLE_LIST:
            case DateChangeTime.TABLE_ID:
                final String SDF_PATTERN_YMD = "yyyy-MM-dd";
                final SimpleDateFormat dateFormat = new SimpleDateFormat(SDF_PATTERN_YMD, Locale.JAPAN);
                final MatrixCursor mc = new MatrixCursor(new String[]{DateChangeTime.ADJUSTED_DATE, DateChangeTime.NEXT_DATE_CHANGE_TIME});
                Date today = DateChangeTimeUtil.getDateTime();
                DateChangeTimeUtil.DateChangeTime dateChangeTime = DateChangeTimeUtil.getDateChangeTime();
                Calendar dateChangeTimeCalendar = DateChangeTimeUtil.getDateTimeCalendar();
                dateChangeTimeCalendar.add(Calendar.DATE, 1);
                dateChangeTimeCalendar.set(Calendar.HOUR_OF_DAY, dateChangeTime.hourOfDay);
                dateChangeTimeCalendar.set(Calendar.MINUTE, dateChangeTime.minute);
                dateChangeTimeCalendar.set(Calendar.SECOND, 0);
                dateChangeTimeCalendar.set(Calendar.MILLISECOND, 0);
                mc.addRow(new Object[]{dateFormat.format(today), dateChangeTimeCalendar.getTimeInMillis()});
                return mc;

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

        case Tasks.TABLE_LIST:
        case Tasks.TABLE_ID:
            tableName = Tasks.TABLE_NAME;
            break;

        case TaskCompletion.TABLE_LIST:
        case TaskCompletion.TABLE_ID:
            tableName = TaskCompletion.TABLE_NAME;
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

        case Tasks.TABLE_LIST:
        case Tasks.TABLE_ID:
            tableName = Tasks.TABLE_NAME;
            break;

        case TaskCompletion.TABLE_LIST:
        case TaskCompletion.TABLE_ID:
            tableName = TaskCompletion.TABLE_NAME;
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