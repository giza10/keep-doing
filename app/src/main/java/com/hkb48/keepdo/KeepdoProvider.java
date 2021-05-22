package com.hkb48.keepdo;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.annotation.NonNull;

import com.hkb48.keepdo.settings.Settings;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class KeepdoProvider extends ContentProvider {
    private static final String TAG = "KeepdoProvider";
    private static final String AUTHORITY = "com.hkb48.keepdo.keepdoprovider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // Task table
    static final class Tasks implements BaseColumns {

        private Tasks() {
        }

        // Incoming URI matches the main table URI pattern
        static final int TABLE_LIST = 10;
        // Incoming URI matches the main table row ID URI pattern
        static final int TABLE_ID = 11;

        static final int MAX_ORDER_ID = 12;

        static final String TABLE_NAME = "table_tasks";
        static final String TABLE_URI = "table_task_uri";
        static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_URI);

        static final String TASK_NAME = "task_name";
        static final String FREQUENCY_MON = "mon_frequency";
        static final String FREQUENCY_TUE = "tue_frequency";
        static final String FREQUENCY_WEN = "wen_frequency";
        static final String FREQUENCY_THR = "thr_frequency";
        static final String FREQUENCY_FRI = "fri_frequency";
        static final String FREQUENCY_SAT = "sat_frequency";
        static final String FREQUENCY_SUN = "sun_frequency";
        static final String TASK_CONTEXT = "task_context";

        static final String REMINDER_ENABLED = "reminder_enabled";
        static final String REMINDER_TIME = "reminder_time";
        static final String TASK_LIST_ORDER = "task_list_order";
    }

    // Task Completion table
    static final class TaskCompletion implements BaseColumns {

        private TaskCompletion() {
        }

        // Incoming URI matches the main table URI pattern
        static final int TABLE_LIST = 20;
        // Incoming URI matches the main table row ID URI pattern
        static final int TABLE_ID = 21;

        static final int MAX_COMPLETION_DATE_ID = 23;
        static final int MIN_COMPLETION_DATE_ID = 24;

        static final String TABLE_NAME = "table_completions";
        static final String TABLE_URI = "table_completion_uri";

        static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_URI);

        static final String TASK_NAME_ID = "task_id";
        static final String TASK_COMPLETION_DATE = "completion_date";
    }

    public static final class DateChangeTime implements BaseColumns {
        private DateChangeTime() {
        }

        // Incoming URI matches the main table URI pattern
        static final int TABLE_LIST = 30;
        // Incoming URI matches the main table row ID URI pattern
        static final int TABLE_ID = 31;

        static final String TABLE_URI = "table_datechangetime_uri";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_URI);
        static final String ADJUSTED_DATE = "date";
        public static final String NEXT_DATE_CHANGE_TIME = "next_date_change_time";
    }

    private ContentResolver mContentResolver;
    private DatabaseHelper mOpenHelper;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, Tasks.TABLE_URI, Tasks.TABLE_LIST);
        sURIMatcher.addURI(AUTHORITY, Tasks.TABLE_URI + "/#", Tasks.TABLE_ID);
        sURIMatcher.addURI(AUTHORITY, Tasks.TABLE_URI + "/max_order", Tasks.MAX_ORDER_ID);
        sURIMatcher.addURI(AUTHORITY, TaskCompletion.TABLE_URI, TaskCompletion.TABLE_LIST);
        sURIMatcher.addURI(AUTHORITY, TaskCompletion.TABLE_URI + "/#", TaskCompletion.TABLE_ID);
        sURIMatcher.addURI(AUTHORITY, TaskCompletion.TABLE_URI + "/#/max", TaskCompletion.MAX_COMPLETION_DATE_ID);
        sURIMatcher.addURI(AUTHORITY, TaskCompletion.TABLE_URI + "/#/min", TaskCompletion.MIN_COMPLETION_DATE_ID);
        sURIMatcher.addURI(AUTHORITY, DateChangeTime.TABLE_URI, DateChangeTime.TABLE_LIST);
        sURIMatcher.addURI(AUTHORITY, DateChangeTime.TABLE_URI + "/#", DateChangeTime.TABLE_ID);
    }

    private static final HashMap<String, String> sTasksProjectionMap = new HashMap<>();

    static {
        sTasksProjectionMap.put(Tasks._ID, Tasks._ID);
        sTasksProjectionMap.put(Tasks.TASK_NAME, Tasks.TASK_NAME);
        sTasksProjectionMap.put(Tasks.FREQUENCY_MON, Tasks.FREQUENCY_MON);
        sTasksProjectionMap.put(Tasks.FREQUENCY_TUE, Tasks.FREQUENCY_TUE);
        sTasksProjectionMap.put(Tasks.FREQUENCY_WEN, Tasks.FREQUENCY_WEN);
        sTasksProjectionMap.put(Tasks.FREQUENCY_THR, Tasks.FREQUENCY_THR);
        sTasksProjectionMap.put(Tasks.FREQUENCY_FRI, Tasks.FREQUENCY_FRI);
        sTasksProjectionMap.put(Tasks.FREQUENCY_SAT, Tasks.FREQUENCY_SAT);
        sTasksProjectionMap.put(Tasks.FREQUENCY_SUN, Tasks.FREQUENCY_SUN);
        sTasksProjectionMap.put(Tasks.TASK_CONTEXT, Tasks.TASK_CONTEXT);
        sTasksProjectionMap.put(Tasks.REMINDER_ENABLED, Tasks.REMINDER_ENABLED);
        sTasksProjectionMap.put(Tasks.REMINDER_TIME, Tasks.REMINDER_TIME);
        sTasksProjectionMap.put(Tasks.TASK_LIST_ORDER, Tasks.TASK_LIST_ORDER);
    }

    private static final HashMap<String, String> sMaxTaskProjectionMap = new HashMap<>();

    static {
        sMaxTaskProjectionMap.put("MAX(" + Tasks.TASK_LIST_ORDER + ")", "MAX(" + Tasks.TASK_LIST_ORDER + ")");
    }

    private static final HashMap<String, String> sTaskCompletionProjectionMap = new HashMap<>();

    static {
        sTaskCompletionProjectionMap.put(TaskCompletion._ID, TaskCompletion._ID);
        sTaskCompletionProjectionMap.put(TaskCompletion.TASK_NAME_ID, TaskCompletion.TASK_NAME_ID);
        sTaskCompletionProjectionMap.put(TaskCompletion.TASK_COMPLETION_DATE, TaskCompletion.TASK_COMPLETION_DATE);
    }

    private static final HashMap<String, String> sMaxTaskCompletionProjectionMap = new HashMap<>();

    static {
        sMaxTaskCompletionProjectionMap.put("MAX(" + TaskCompletion.TASK_COMPLETION_DATE + ")", "MAX(" + TaskCompletion.TASK_COMPLETION_DATE + ")");
    }

    private static final HashMap<String, String> sMinTaskCompletionProjectionMap = new HashMap<>();

    static {
        sMinTaskCompletionProjectionMap.put("MIN(" + TaskCompletion.TASK_COMPLETION_DATE + ")", "MIN(" + TaskCompletion.TASK_COMPLETION_DATE + ")");
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
    public String getType(@NonNull Uri uri) {
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
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "insert: uri=" + uri + "  values=[" + values + "]");
        }
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
        long newRowId = db.insertOrThrow(tableName, null, values);
        Uri newUri = ContentUris.withAppendedId(uri, newRowId);
        getContentResolver().notifyChange(newUri, null);

        return newUri;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "query: uri=" + uri + "  projection=" + Arrays.toString(projection) +
                    "  selection=[" + selection + "]  args=" + Arrays.toString(selectionArgs) +
                    "  order=[" + sortOrder + "]");
        }

        // Constructs a new query builder and sets its table name
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        if (uri.getQueryParameter("distinct") != null) {
            qb.setDistinct(true);
        }

        switch (sURIMatcher.match(uri)) {

            case Tasks.TABLE_LIST:
                qb.setTables(Tasks.TABLE_NAME);
                qb.setProjectionMap(sTasksProjectionMap);
                qb.setStrict(true);
                break;
            case Tasks.TABLE_ID:
                qb.setTables(Tasks.TABLE_NAME);
                qb.appendWhere(Tasks._ID + "=" + parseTaskIdFromUri(uri));
                qb.setProjectionMap(sTasksProjectionMap);
                qb.setStrict(true);
                break;
            case Tasks.MAX_ORDER_ID:
                qb.setTables(Tasks.TABLE_NAME);
                qb.setProjectionMap(sMaxTaskProjectionMap);
                qb.setStrict(true);
                break;
            case TaskCompletion.TABLE_LIST:
                qb.setTables(TaskCompletion.TABLE_NAME);
                qb.setProjectionMap(sTaskCompletionProjectionMap);
                qb.setStrict(true);
                break;
            case TaskCompletion.TABLE_ID:
                qb.setTables(TaskCompletion.TABLE_NAME);
                qb.appendWhere(TaskCompletion.TASK_NAME_ID + "=" + parseTaskIdFromUri(uri));
                qb.setProjectionMap(sTaskCompletionProjectionMap);
                qb.setStrict(true);
                break;
            case TaskCompletion.MAX_COMPLETION_DATE_ID:
                qb.setTables(TaskCompletion.TABLE_NAME);
                qb.appendWhere(TaskCompletion.TASK_NAME_ID + "=" + parseTaskIdFromUri(uri));
                qb.setProjectionMap(sMaxTaskCompletionProjectionMap);
                qb.setStrict(true);
                break;
            case TaskCompletion.MIN_COMPLETION_DATE_ID:
                qb.setTables(TaskCompletion.TABLE_NAME);
                qb.appendWhere(TaskCompletion.TASK_NAME_ID + "=" + parseTaskIdFromUri(uri));
                qb.setProjectionMap(sMinTaskCompletionProjectionMap);
                qb.setStrict(true);
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
        c.setNotificationUri(getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "update: uri=" + uri + "  selection=[" + selection + "]  args="
                    + Arrays.toString(selectionArgs) + "  values=[" + values + "]");
        }

        if (sURIMatcher.match(uri) == Tasks.TABLE_ID) {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            assert db != null;
            final int id = db.update(Tasks.TABLE_NAME, values, Tasks._ID + "=?",
                    new String[]{String.valueOf(parseTaskIdFromUri(uri))});
            getContentResolver().notifyChange(uri, null);
            return id;
        }
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "delete: uri=" + uri + "  selection=[" + selection + "]  args="
                    + Arrays.toString(selectionArgs));
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        assert db != null;
        switch (sURIMatcher.match(uri)) {

            case Tasks.TABLE_ID:
                int id = db.delete(Tasks.TABLE_NAME, Tasks._ID + "=?",
                        new String[]{String.valueOf(parseTaskIdFromUri(uri))});
                getContentResolver().notifyChange(uri, null);
                return id;
            case TaskCompletion.TABLE_ID:
                if (selection != null) {
                    id = db.delete(TaskCompletion.TABLE_NAME,
                            "(" + selection + ") AND (" + TaskCompletion.TASK_NAME_ID + "=?)",
                            new String[]{selectionArgs[0], String.valueOf(parseTaskIdFromUri(uri))});
                } else {
                    id = db.delete(TaskCompletion.TABLE_NAME, TaskCompletion.TASK_NAME_ID + "=?",
                            new String[]{String.valueOf(parseTaskIdFromUri(uri))});
                }
                getContentResolver().notifyChange(uri, null);
                return id;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

    }

    @NonNull
    private ContentResolver getContentResolver() {
        if (mContentResolver == null) {
            Context context = getContext();
            assert context != null;
            mContentResolver = context.getContentResolver();
        }
        return mContentResolver;
    }

    /**
     * Parses the call Id from the given uri, assuming that this is a uri that
     * matches TABLE_ID. For other uri types the behaviour is undefined.
     *
     * @throws IllegalArgumentException if the id included in the Uri is not a valid long value.
     */
    private long parseTaskIdFromUri(Uri uri) {
        try {
            return Long.parseLong(uri.getPathSegments().get(1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid call id in uri: " + uri, e);
        }
    }
}