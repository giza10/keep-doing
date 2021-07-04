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

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_APPWIDGET_UPDATE,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_LOCALE_CHANGED -> {
                updateAllWidgetsList(context)
            }
            ACTION_ITEM_CLICKED -> {
                val viewId = intent.getIntExtra(EXTRA_VIEWID, -1)
                if (viewId == VIEWID_LIST_ITEM_ICON) {
                    selectedItemIndex = intent.getIntExtra(EXTRA_POSITION, INVALID_INDEX)
                    val appWidgetId = intent.getIntExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID
                    )
                    updateWidgetList(context, appWidgetId)
                    val taskId = intent.getIntExtra(EXTRA_TASK_ID, TaskInfo.INVALID_TASKID)
                    Handler(Looper.getMainLooper()).postDelayed({
                        val model = TasksWidgetModel(context)
                        val doneToday = model.getDoneStatus(taskId, model.todayDate)
                        if (doneToday.not()) {
                            context.sendBroadcast(
                                Intent(context, ActionReceiver::class.java).apply {
                                    putExtra(ActionReceiver.EXTRA_TASK_ID, taskId)
                                }
                            )
                        }
                        selectedItemIndex = INVALID_INDEX
                        RemindAlarmInitReceiver.updateReminder(context)
                    }, 500)
                } else {
                    context.startActivity(
                        Intent(context, TasksActivity::class.java).apply {
                            flags =
                                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    )
                }
            }
        }
        super.onReceive(context, intent)
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
        val intent = Intent(context, TasksWidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
        }
        // Bind a click listener template for the contents of the task list.  Note that we
        // need to update the intent's data if we set an extra, since the extras will be
        // ignored otherwise.
        val onItemClickIntent = Intent(context, TasksWidgetProvider::class.java).apply {
            action = ACTION_ITEM_CLICKED
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val onClickPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            onItemClickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        return RemoteViews(context.packageName, R.layout.tasks_widget).apply {
            setRemoteAdapter(R.id.task_list, intent)

            // Set the empty view to be displayed if the collection is empty.  It must be a sibling
            // view of the collection view.
            setEmptyView(R.id.task_list, R.id.empty_view)

            setPendingIntentTemplate(R.id.task_list, onClickPendingIntent)
            setOnClickPendingIntent(R.id.empty_view, onClickPendingIntent)
        }
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
        val nextAlarmTime = getNextDateChangeTime()
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
        val intent = Intent(context, TasksWidgetProvider::class.java).apply {
            action = ACTION_APPWIDGET_UPDATE
        }
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getNextDateChangeTime(): Long {
        val dateChangeTime = DateChangeTimeUtil.dateChangeTime
        val dateChangeTimeCalendar = DateChangeTimeUtil.dateTimeCalendar
        dateChangeTimeCalendar.add(Calendar.DATE, 1)
        dateChangeTimeCalendar[Calendar.HOUR_OF_DAY] = dateChangeTime.hourOfDay
        dateChangeTimeCalendar[Calendar.MINUTE] = dateChangeTime.minute
        dateChangeTimeCalendar[Calendar.SECOND] = 0
        dateChangeTimeCalendar[Calendar.MILLISECOND] = 0
        return dateChangeTimeCalendar.timeInMillis
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
        const val EXTRA_POSITION = "position"
        const val EXTRA_TASK_ID = "task-id"
        const val EXTRA_VIEWID = "view-id"
        const val VIEWID_LIST_ITEM = 0
        const val VIEWID_LIST_ITEM_ICON = 1
        private val PACKAGE_NAME = TasksWidgetProvider::class.java.getPackage()!!.name
        private val ACTION_APPWIDGET_UPDATE = "$PACKAGE_NAME.action.APPWIDGET_UPDATE"
        private val ACTION_ITEM_CLICKED = "$PACKAGE_NAME.action.ITEM_CLICKED"
        private const val INVALID_INDEX = -1
        var selectedItemIndex = INVALID_INDEX
            private set

        fun notifyDatasetChanged(context: Context) {
            val intent = Intent(context, TasksWidgetProvider::class.java).apply {
                action = ACTION_APPWIDGET_UPDATE
            }
            context.sendBroadcast(intent)
        }
    }
}