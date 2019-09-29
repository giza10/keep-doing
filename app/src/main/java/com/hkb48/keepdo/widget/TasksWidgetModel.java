package com.hkb48.keepdo.widget;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.hkb48.keepdo.KeepdoProvider.DateChangeTime;
import com.hkb48.keepdo.KeepdoProvider.TaskCompletion;
import com.hkb48.keepdo.KeepdoProvider.Tasks;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class TasksWidgetModel {
    private static final String TAG = "#TasksWidgetModel: ";
    private static final String SDF_PATTERN_YMD = "yyyy-MM-dd";

    private final Context mContext;
    private final List<Task> mTaskList = new ArrayList<>();

    public TasksWidgetModel (Context context) {
        mContext = context;
    }

    public void reload() {
        mTaskList.clear();
        String sortOrder = Tasks.TASK_LIST_ORDER + " asc";
        Cursor cursor = mContext.getContentResolver().query(Tasks.CONTENT_URI, null, null,
                null, sortOrder);
        if (cursor.moveToFirst()) {
            String date = getTodayDate();
            do {
                final int taskIdColIndex = cursor.getColumnIndex(Tasks._ID);
                final long taskId = cursor.getLong(taskIdColIndex);
                if (! getDoneStatus(taskId, date) && isValidDay(cursor, date)) {
                    final int taskNameColIndex = cursor.getColumnIndex(Tasks.TASK_NAME);
                    final Task task = new Task(taskId, cursor.getString(taskNameColIndex));
                    mTaskList.add(task);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    public int getItemCount() {
        return mTaskList.size();
    }

    public long getTaskId(int position) {
        return mTaskList.get(position).id;
    }

    public String getTaskName(int position) {
        return mTaskList.get(position).name;
    }

    public String getTodayDate() {
        String date = "";
        Cursor cursor = mContext.getContentResolver().query(DateChangeTime.CONTENT_URI, null, null,
                null, null);
        if (cursor.moveToFirst()) {
            final int dateColIndex = cursor.getColumnIndex(DateChangeTime.ADJUSTED_DATE);
            date = cursor.getString(dateColIndex);
        }
        cursor.close();
        return date;
    }

    public boolean getDoneStatus(long taskId, String date) {
        boolean isDone = false;

        String selection = TaskCompletion.TASK_NAME_ID + "=? and " + TaskCompletion.TASK_COMPLETION_DATE + "=?";
        String selectionArgs[] = {String.valueOf(taskId), date};
        Cursor cursor = mContext.getContentResolver().query(TaskCompletion.CONTENT_URI, null, selection,
                selectionArgs, null);
        if (cursor.getCount() > 0) {
            isDone =  true;
        }
        cursor.close();
        return isDone;
    }

    private boolean isValidDay(Cursor cursor, String dateString) {
        final SimpleDateFormat sdf_ymd = new SimpleDateFormat(SDF_PATTERN_YMD, Locale.JAPAN);
        Date date = null;
        if (dateString != null) {
            try {
                date = sdf_ymd.parse(dateString);
            } catch (ParseException e) {
                Log.e(TAG, e.getMessage());
                return false;
            }
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String columnString;
        switch(calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY: columnString = Tasks.FREQUENCY_MON;  break;
            case Calendar.TUESDAY: columnString = Tasks.FREQUENCY_TUE;  break;
            case Calendar.WEDNESDAY: columnString = Tasks.FREQUENCY_WEN;  break;
            case Calendar.THURSDAY: columnString = Tasks.FREQUENCY_THR;  break;
            case Calendar.FRIDAY: columnString = Tasks.FREQUENCY_FRI;  break;
            case Calendar.SATURDAY: columnString = Tasks.FREQUENCY_SAT;  break;
            case Calendar.SUNDAY: columnString = Tasks.FREQUENCY_SUN; break;
            default: return false;
        }
        return Boolean.valueOf(cursor.getString(cursor.getColumnIndex(columnString)));
    }

    static class Task {
        final long id;
        final String name;

        public Task(long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
