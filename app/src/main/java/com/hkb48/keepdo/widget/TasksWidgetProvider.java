package com.hkb48.keepdo.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.RemoteViews;

import com.hkb48.keepdo.ActionHandler;
import com.hkb48.keepdo.BuildConfig;
import com.hkb48.keepdo.DatabaseAdapter;
import com.hkb48.keepdo.R;
import com.hkb48.keepdo.RemindAlarmInitReceiver;
import com.hkb48.keepdo.TasksActivity;

import java.text.MessageFormat;
import java.util.Calendar;

public class TasksWidgetProvider extends AppWidgetProvider {
    public static final String PARAM_POSITION = "position";
    public static final String PARAM_TASK_ID = "task-id";
    public static final String PARAM_VIEWID = "view-id";
    public static final int VIEWID_LIST_ITEM = 0;
    public static final int VIEWID_LIST_ITEM_ICON = 1;
    private static final String ACTION_APPWIDGET_UPDATE = "com.hkb48.keepdo.action.APPWIDGET_UPDATE";
    private static final String ACTION_ITEM_CLICKED = "com.hkb48.keepdo.action.ITEM_CLICKED";
    private static final int INVALID_INDEX = -1;

    private static int sSelectedPosition = INVALID_INDEX;

    public TasksWidgetProvider() {
    }

    public static int getSelectedItemIndex() {
        return sSelectedPosition;
    }

    public static void notifyDatasetChanged(final Context context) {
        final Intent intent = new Intent(context, TasksWidgetProvider.class);
        intent.setAction(ACTION_APPWIDGET_UPDATE);
        context.sendBroadcast(intent);
    }

    @Override
    public void onEnabled(Context context) {
        startAlarm(context);
    }

    @Override
    public void onDisabled(Context context) {
        stopAlarm(context);
        super.onDisabled(context);
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        final String action = intent.getAction();
        final Context context = ctx;

        if (ACTION_APPWIDGET_UPDATE.equals(action) ||
                Intent.ACTION_TIME_CHANGED.equals(action) ||
                Intent.ACTION_TIMEZONE_CHANGED.equals(action) ||
                Intent.ACTION_LOCALE_CHANGED.equals(action)) {
            updateAllWidgetsList(context);
        } else if (ACTION_ITEM_CLICKED.equals(action)) {
            final int viewId = intent.getIntExtra(PARAM_VIEWID, -1);
            if (viewId == VIEWID_LIST_ITEM_ICON) {
                sSelectedPosition = intent.getIntExtra(PARAM_POSITION, INVALID_INDEX);
                final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);
                updateWidgetList(context, appWidgetId);

                final long taskId = intent.getLongExtra(PARAM_TASK_ID, -1);
                new Handler().postDelayed(() -> {
                    TasksWidgetModel model = new TasksWidgetModel(context);
                    String date = model.getTodayDate();
                    boolean doneToday = model.getDoneStatus(taskId, date);
                    if (!doneToday) {
                        Intent intent1 = new Intent(context, ActionHandler.class);
                        intent1.putExtra(ActionHandler.INTENT_EXTRA_TASK_ID, taskId);
                        context.startService(intent1);
                    }
                    sSelectedPosition = INVALID_INDEX;
                    RemindAlarmInitReceiver.updateReminder(context);
                }, 500);
            } else {
                Intent activityLaunchIntent = new Intent(context, TasksActivity.class);
                activityLaunchIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(activityLaunchIntent);
            }
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Update each of the widgets with the remote adapter
        for (int appWidgetId : appWidgetIds) {
            RemoteViews layout = buildLayout(context, appWidgetId);
            appWidgetManager.updateAppWidget(appWidgetId, layout);
        }
        startAlarm(context);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        RemoteViews layout = buildLayout(context, appWidgetId);
        appWidgetManager.updateAppWidget(appWidgetId, layout);
    }

    private RemoteViews buildLayout(Context context, int appWidgetId) {
        RemoteViews rv;

        // Specify the service to provide data for the collection widget.  Note that we need to
        // embed the appWidgetId via the data otherwise it will be ignored.
        final Intent intent = new Intent(context, TasksWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        rv = new RemoteViews(context.getPackageName(), R.layout.tasks_widget);
        rv.setRemoteAdapter(R.id.task_list, intent);

        // Set the empty view to be displayed if the collection is empty.  It must be a sibling
        // view of the collection view.
        rv.setEmptyView(R.id.task_list, R.id.empty_view);

        // Bind a click listener template for the contents of the task list.  Note that we
        // need to update the intent's data if we set an extra, since the extras will be
        // ignored otherwise.
        final Intent onItemClickIntent = new Intent(context, TasksWidgetProvider.class);
        onItemClickIntent.setAction(ACTION_ITEM_CLICKED);
        onItemClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, onItemClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.task_list, onClickPendingIntent);

        rv.setOnClickPendingIntent(R.id.empty_view, onClickPendingIntent);
        return rv;
    }

    private void updateAllWidgetsList(final Context context) {
        final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        final ComponentName cn = new ComponentName(context, TasksWidgetProvider.class);
        mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.task_list);
    }

    private void updateWidgetList(final Context context, final int widgetId) {
        final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        mgr.notifyAppWidgetViewDataChanged(widgetId, R.id.task_list);
    }

    private void startAlarm(final Context context) {
        long nextAlarmTime = DatabaseAdapter.getInstance(context).getNextDateChangeTime();
        if (nextAlarmTime > 0) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setRepeating(AlarmManager.RTC, nextAlarmTime, AlarmManager.INTERVAL_DAY, getPendingIntent(context));
                dumpLog(nextAlarmTime);
            }
        }
    }

    private void stopAlarm(final Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(getPendingIntent(context));
        }
    }


    private PendingIntent getPendingIntent(Context context) {
        final Intent intent = new Intent(context, TasksWidgetProvider.class);
        intent.setAction(ACTION_APPWIDGET_UPDATE);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void dumpLog(long timeInMillis) {
        if (BuildConfig.DEBUG) {
            Calendar time = Calendar.getInstance();
            time.setTimeInMillis(timeInMillis);
            MessageFormat mf = new MessageFormat("{0,date,yyyy/MM/dd HH:mm:ss}");
            Object[] objs = {time.getTime()};
            String result = mf.format(objs);
            Log.v("LOG_KEEPDO", "TasksWidgetProvider: time=" + result);
        }
    }
}
