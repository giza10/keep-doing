package com.hkb48.keepdo.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import com.hkb48.keepdo.*
import java.text.MessageFormat
import java.util.*

class TasksWidgetProvider : AppWidgetProvider() {
    override fun onEnabled(context: Context) {
        startAlarm(context)
    }

    override fun onDisabled(context: Context) {
        stopAlarm(context)
        super.onDisabled(context)
    }

    override fun onReceive(ctx: Context, intent: Intent) {
        val action = intent.action
        if (ACTION_APPWIDGET_UPDATE == action || Intent.ACTION_TIME_CHANGED == action || Intent.ACTION_TIMEZONE_CHANGED == action || Intent.ACTION_LOCALE_CHANGED == action) {
            updateAllWidgetsList(ctx)
        } else if (ACTION_ITEM_CLICKED == action) {
            val viewId = intent.getIntExtra(PARAM_VIEWID, -1)
            if (viewId == VIEWID_LIST_ITEM_ICON) {
                selectedItemIndex = intent.getIntExtra(PARAM_POSITION, INVALID_INDEX)
                val appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
                updateWidgetList(ctx, appWidgetId)
                val taskId = intent.getLongExtra(PARAM_TASK_ID, -1)
                Handler(Looper.getMainLooper()).postDelayed({
                    val model = TasksWidgetModel(ctx)
                    val date = model.todayDate
                    val doneToday = model.getDoneStatus(taskId, date)
                    if (!doneToday) {
                        val intent1 = Intent(ctx, ActionHandler::class.java)
                        intent1.putExtra(ActionHandler.INTENT_EXTRA_TASK_ID, taskId)
                        ctx.startService(intent1)
                    }
                    selectedItemIndex = INVALID_INDEX
                    RemindAlarmInitReceiver.updateReminder(ctx)
                }, 500)
            } else {
                val activityLaunchIntent = Intent(ctx, TasksActivity::class.java)
                activityLaunchIntent.flags =
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_NEW_TASK
                ctx.startActivity(activityLaunchIntent)
            }
        }
        super.onReceive(ctx, intent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update each of the widgets with the remote adapter
        for (appWidgetId in appWidgetIds) {
            val layout = buildLayout(context, appWidgetId)
            appWidgetManager.updateAppWidget(appWidgetId, layout)
        }
        startAlarm(context)
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context, appWidgetManager: AppWidgetManager,
        appWidgetId: Int, newOptions: Bundle
    ) {
        val layout = buildLayout(context, appWidgetId)
        appWidgetManager.updateAppWidget(appWidgetId, layout)
    }

    private fun buildLayout(context: Context, appWidgetId: Int): RemoteViews {

        // Specify the service to provide data for the collection widget.  Note that we need to
        // embed the appWidgetId via the data otherwise it will be ignored.
        val intent = Intent(context, TasksWidgetService::class.java)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
        val rv = RemoteViews(context.packageName, R.layout.tasks_widget)
        rv.setRemoteAdapter(R.id.task_list, intent)

        // Set the empty view to be displayed if the collection is empty.  It must be a sibling
        // view of the collection view.
        rv.setEmptyView(R.id.task_list, R.id.empty_view)

        // Bind a click listener template for the contents of the task list.  Note that we
        // need to update the intent's data if we set an extra, since the extras will be
        // ignored otherwise.
        val onItemClickIntent = Intent(context, TasksWidgetProvider::class.java)
        onItemClickIntent.action = ACTION_ITEM_CLICKED
        onItemClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        val onClickPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            onItemClickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        rv.setPendingIntentTemplate(R.id.task_list, onClickPendingIntent)
        rv.setOnClickPendingIntent(R.id.empty_view, onClickPendingIntent)
        return rv
    }

    private fun updateAllWidgetsList(context: Context) {
        val mgr = AppWidgetManager.getInstance(context)
        val cn = ComponentName(context, TasksWidgetProvider::class.java)
        mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.task_list)
    }

    private fun updateWidgetList(context: Context, widgetId: Int) {
        val mgr = AppWidgetManager.getInstance(context)
        mgr.notifyAppWidgetViewDataChanged(widgetId, R.id.task_list)
    }

    private fun startAlarm(context: Context) {
        val nextAlarmTime = DatabaseAdapter.getInstance(context).nextDateChangeTime
        if (nextAlarmTime > 0) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
            alarmManager?.setRepeating(
                AlarmManager.RTC,
                nextAlarmTime,
                AlarmManager.INTERVAL_DAY,
                getPendingIntent(context)
            )
            dumpLog(nextAlarmTime)
        }
    }

    private fun stopAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        alarmManager?.cancel(getPendingIntent(context))
    }

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, TasksWidgetProvider::class.java)
        intent.action = ACTION_APPWIDGET_UPDATE
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun dumpLog(timeInMillis: Long) {
        if (BuildConfig.DEBUG) {
            val time = Calendar.getInstance()
            time.timeInMillis = timeInMillis
            val mf = MessageFormat("{0,date,yyyy/MM/dd HH:mm:ss}")
            val objs = arrayOf<Any>(time.time)
            val result = mf.format(objs)
            Log.v("LOG_KEEPDO", "TasksWidgetProvider: time=$result")
        }
    }

    companion object {
        const val PARAM_POSITION = "position"
        const val PARAM_TASK_ID = "task-id"
        const val PARAM_VIEWID = "view-id"
        const val VIEWID_LIST_ITEM = 0
        const val VIEWID_LIST_ITEM_ICON = 1
        private const val ACTION_APPWIDGET_UPDATE = "com.hkb48.keepdo.action.APPWIDGET_UPDATE"
        private const val ACTION_ITEM_CLICKED = "com.hkb48.keepdo.action.ITEM_CLICKED"
        private const val INVALID_INDEX = -1
        var selectedItemIndex = INVALID_INDEX
            private set

        @JvmStatic
        fun notifyDatasetChanged(context: Context) {
            val intent = Intent(context, TasksWidgetProvider::class.java)
            intent.action = ACTION_APPWIDGET_UPDATE
            context.sendBroadcast(intent)
        }
    }
}