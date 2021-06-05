package com.hkb48.keepdo.widget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import android.widget.RemoteViewsService.RemoteViewsFactory
import com.hkb48.keepdo.R

class TasksWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return StackRemoteViewsFactory(this.applicationContext)
    }
}

internal class StackRemoteViewsFactory(private val mContext: Context) :
    RemoteViewsFactory {
    private lateinit var mModel: TasksWidgetModel
    override fun onCreate() {
        mModel = TasksWidgetModel(mContext)
    }

    override fun onDestroy() {}
    override fun getCount(): Int {
        return mModel.itemCount
    }

    override fun getViewAt(position: Int): RemoteViews {
        val itemId = R.layout.widget_item
        val rv = RemoteViews(mContext.packageName, itemId)
        rv.setTextViewText(R.id.widget_item_text, mModel.getTaskName(position))
        rv.setImageViewResource(R.id.widget_item_icon, R.drawable.ic_not_done_3)
        val intentForListItem = Intent()
        val extras = Bundle()
        extras.putInt(TasksWidgetProvider.PARAM_VIEWID, TasksWidgetProvider.VIEWID_LIST_ITEM)
        intentForListItem.putExtras(extras)
        rv.setOnClickFillInIntent(R.id.widget_item_background, intentForListItem)
        val intentForDoneIcon = Intent()
        val extras2 = Bundle()
        extras2.putInt(TasksWidgetProvider.PARAM_VIEWID, TasksWidgetProvider.VIEWID_LIST_ITEM_ICON)
        extras2.putLong(TasksWidgetProvider.PARAM_TASK_ID, mModel.getTaskId(position))
        extras2.putInt(TasksWidgetProvider.PARAM_POSITION, position)
        intentForDoneIcon.putExtras(extras2)
        rv.setOnClickFillInIntent(R.id.widget_item_icon, intentForDoneIcon)
        if (TasksWidgetProvider.selectedItemIndex == position) {
            rv.setImageViewResource(R.id.widget_item_icon, R.drawable.ic_done_3)
        }
        return rv
    }

    override fun getLoadingView(): RemoteViews? {
        // We aren't going to return a default loading view in this sample
        return null
    }

    override fun getViewTypeCount(): Int {
        // Technically, we have two types of views (the dark and light background views)
        return 2
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun onDataSetChanged() {
        mModel.reload()
    }
}