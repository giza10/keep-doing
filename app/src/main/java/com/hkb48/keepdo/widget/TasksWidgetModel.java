package com.hkb48.keepdo.widget;

import android.content.Context;
import android.util.Log;

import com.hkb48.keepdo.DatabaseAdapter;
import com.hkb48.keepdo.Task;

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
    private List<Task> mTaskList = new ArrayList<>();

    TasksWidgetModel (Context context) {
        mContext = context;
    }

    void reload() {
        mTaskList.clear();
        List<Task> fullTaskList = DatabaseAdapter.getInstance(mContext).getTaskList();
        String today = getTodayDate();
        for (Task task : fullTaskList) {
            if (! getDoneStatus(task.getTaskID(), today) && isValidDay(task, today)) {
                mTaskList.add(task);
            }
        }
    }

    int getItemCount() {
        return mTaskList.size();
    }

    long getTaskId(int position) {
        return mTaskList.get(position).getTaskID();
    }

    String getTaskName(int position) {
        return mTaskList.get(position).getName();
    }

    boolean getDoneStatus(long taskId, String date) {
        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(mContext);
        return dbAdapter.getDoneStatus(taskId, date);
    }

    String getTodayDate() {
        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(mContext);
        return dbAdapter.getTodayDate();
    }

    private boolean isValidDay(Task task, String dateString) {
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

        if (date == null) {
            return false;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return task.getRecurrence().isValidDay(dayOfWeek);
    }
}
