package com.hkb48.keepdo.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.hkb48.keepdo.R;

public class TasksWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private final Context mContext;
    private TasksWidgetModel mModel;


    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
    }

    public void onCreate() {
        mModel = new TasksWidgetModel(mContext);
    }

    public void onDestroy() {
    }

    public int getCount() {
        return mModel.getItemCount();
    }

    public RemoteViews getViewAt(int position) {
        final int itemId = R.layout.widget_item;
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), itemId);
        rv.setTextViewText(R.id.widget_item_text, mModel.getTaskName(position));
        rv.setImageViewResource(R.id.widget_item_icon, R.drawable.ic_not_done_3);

        final Intent intentForListItem = new Intent();
        final Bundle extras = new Bundle();
        extras.putInt(TasksWidgetProvider.PARAM_VIEWID, TasksWidgetProvider.VIEWID_LIST_ITEM);
        intentForListItem.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.widget_item_background, intentForListItem);
        
        final Intent intentForDoneIcon = new Intent();
        final Bundle extras2 = new Bundle();
        extras2.putInt(TasksWidgetProvider.PARAM_VIEWID, TasksWidgetProvider.VIEWID_LIST_ITEM_ICON);
        extras2.putLong(TasksWidgetProvider.PARAM_TASK_ID, mModel.getTaskId(position));
        extras2.putInt(TasksWidgetProvider.PARAM_POSITION, position);
        intentForDoneIcon.putExtras(extras2);
        rv.setOnClickFillInIntent(R.id.widget_item_icon, intentForDoneIcon);

        if (TasksWidgetProvider.getSelectedItemIndex() == position) {
            rv.setImageViewResource(R.id.widget_item_icon, R.drawable.ic_done_3);
        }

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
        mModel.reload();
    }
}
