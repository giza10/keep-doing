package com.hkb48.keepdo.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.hkb48.keepdo.KeepdoProvider.DateChangeTime;
import com.hkb48.keepdo.KeepdoProvider.TaskCompletion;
import com.hkb48.keepdo.KeepdoProvider.Tasks;
import com.hkb48.keepdo.R;

public class TasksWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private final Context mContext;
    private final List<String> mTaskList = new ArrayList<String>();
//    private int mAppWidgetId;

    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
//        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
//                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public void onCreate() {
        // Since we reload the cursor in onDataSetChanged() which gets called immediately after
        // onCreate(), we do nothing here.
    }

    public void onDestroy() {
    }

    public int getCount() {
        return mTaskList.size();
    }

    public RemoteViews getViewAt(int position) {
        // Get the data for this position from the content provider
//        String taskName = "Unknown Task";
//        int temp = 0;
//        if (mCursor.moveToPosition(position)) {
//            final int taskNameColIndex = mCursor.getColumnIndex(KeepdoProvider.Columns.TASK_NAME);
//            final int dayColIndex = mCursor.getColumnIndex(KeepdoProvider.Columns.TASK_NAME);
//            final int tempColIndex = mCursor.getColumnIndex(
//                    DummyWeatherDataProvider.Columns.TEMPERATURE);
//            taskName = mCursor.getString(taskNameColIndex);
//            temp = mCursor.getInt(tempColIndex);
//        }

        // Return a proper item with the proper day and temperature
//        final String formatStr = mContext.getResources().getString(R.string.item_format_string);
        final int itemId = R.layout.widget_item;
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), itemId);
        rv.setTextViewText(R.id.widget_item, mTaskList.get(position));

        // Set the click intent so that we can handle it and show a toast message
        final Intent fillInIntent = new Intent();
        final Bundle extras = new Bundle();
//        extras.putString(TasksWidget.EXTRA_DAY_ID, day);
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);

        return rv;
    }

    public RemoteViews getLoadingView() {
        // We aren't going to return a default loading view in this sample
        return null;
    }

    public int getViewTypeCount() {
        // Technically, we have two types of views (the dark and light background views)
        return 2;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        mTaskList.clear();
        String sortOrder = Tasks.TASK_LIST_ORDER + " asc";
        Cursor cursor = mContext.getContentResolver().query(Tasks.CONTENT_URI, null, null,
                null, sortOrder);
        if (cursor.moveToFirst()) {
            final String today = getDateChangeTime();
            do {
                final int taskIdColIndex = cursor.getColumnIndex(Tasks._ID);
                final long taskId = cursor.getLong(taskIdColIndex);
                if (! getDoneStatus(taskId, today)) {
                    final int taskNameColIndex = cursor.getColumnIndex(Tasks.TASK_NAME);
                    mTaskList.add(cursor.getString(taskNameColIndex));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private String getDateChangeTime() {
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

    private boolean getDoneStatus(long taskId, String date) {
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
}
