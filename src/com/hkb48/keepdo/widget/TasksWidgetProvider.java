package com.hkb48.keepdo.widget;

import java.text.MessageFormat;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.RemoteViews;

import com.hkb48.keepdo.BuildConfig;
import com.hkb48.keepdo.KeepdoProvider;
import com.hkb48.keepdo.R;
import com.hkb48.keepdo.TasksActivity;
import com.hkb48.keepdo.KeepdoProvider.DateChangeTime;

class TasksDataProviderObserver extends ContentObserver {
    private final AppWidgetManager mAppWidgetManager;
    private final ComponentName mComponentName;

    TasksDataProviderObserver(AppWidgetManager mgr, ComponentName cn, Handler h) {
        super(h);
        mAppWidgetManager = mgr;
        mComponentName = cn;
    }

    @Override
    public void onChange(boolean selfChange) {
        // The data has changed, so notify the widget that the collection view needs to be updated.
        // In response, the factory's onDataSetChanged() will be called which will requery the
        // cursor for the new data.
        mAppWidgetManager.notifyAppWidgetViewDataChanged(
                mAppWidgetManager.getAppWidgetIds(mComponentName), R.id.task_list);
    }
}

public class TasksWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_APPWIDGET_UPDATE = "com.hkb48.keepdo.action.APPWIDGET_UPDATE";
    public static final String ACTION_PROVIDER_CREATED = "com.hkb48.keepdo.action.PROVIDER_CREATED";
    private static final String ACTION_ITEM_CLICKED = "com.hkb48.keepdo.action.ITEM_CLICKED";

    public static final String PARAM_POSITION = "position";
    public static final String PARAM_TASK_ID = "task-id";
    public static final String PARAM_VIEWID = "view-id";
    public static final int VIEWID_DONE_ICON = 1;

    public static final int INVALID_INDEX = -1;

    private static int sSelectedPosition = INVALID_INDEX;
    private static ContentObserver sContentObserver;

    public TasksWidgetProvider() {
    }

    @Override
    public void onEnabled(Context context) {
        registerContentObserver(context);
        startAlarm(context);
    }

    @Override
    public void onDisabled(Context context) {
        stopAlarm(context);
        unregisterContentObserver(context);
        super.onDisabled(context);
    }

	@Override
    public void onReceive(Context ctx, Intent intent) {
        final String action = intent.getAction();
        final Context context = ctx;

        if (action.equals(ACTION_APPWIDGET_UPDATE) ||
                action.equalsIgnoreCase("android.intent.action.TIME_SET") ||
                action.equalsIgnoreCase("android.intent.action.TIMEZONE_CHANGED") ||
                action.equalsIgnoreCase("android.intent.action.LOCALE_CHANGED")) {
            updateWidgetList(context);
        } else if (action.equals(ACTION_PROVIDER_CREATED)) {
            registerContentObserver(context);
        } else if (action.equals(ACTION_ITEM_CLICKED)) {
            final int viewId = intent.getIntExtra(PARAM_VIEWID, -1);
            if (viewId == VIEWID_DONE_ICON) {
                sSelectedPosition = intent.getIntExtra(PARAM_POSITION, -1);
                updateWidgetList(context);

                final long taskId = intent.getLongExtra(PARAM_TASK_ID, -1);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        TasksWidgetModel model = new TasksWidgetModel(context);
                        String date = model.getTodayDate();
                        boolean doneToday = model.getDoneStatus(taskId, date);
                        if (! doneToday) {
                            model.setDoneStatus(taskId, date);
                        }
                        sSelectedPosition = INVALID_INDEX;
                    }
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
        for (int i = 0; i < appWidgetIds.length; ++i) {
            RemoteViews layout = buildLayout(context, appWidgetIds[i]);
            appWidgetManager.updateAppWidget(appWidgetIds[i], layout);
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

    public static int getSelectedItemIndex() {
        return sSelectedPosition;
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

    private void updateWidgetList(final Context context) {
        final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        final ComponentName cn = new ComponentName(context, TasksWidgetProvider.class);
        mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.task_list);
    }
    private void registerContentObserver(final Context context) {
        if (sContentObserver == null) {
            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            final ComponentName cn = new ComponentName(context, TasksWidgetProvider.class);
            sContentObserver = new TasksDataProviderObserver(mgr, cn, new Handler());
        }
        context.getContentResolver().registerContentObserver(KeepdoProvider.BASE_CONTENT_URI, true, sContentObserver);
    }

    private void unregisterContentObserver(final Context context) {
        if (sContentObserver != null) {
            context.getContentResolver().unregisterContentObserver(sContentObserver);
        }
    }

    private void startAlarm(final Context context) {
        Cursor cursor = context.getContentResolver().query(DateChangeTime.CONTENT_URI, null, null,
                null, null);
        if (cursor.moveToFirst()) {
            final int colIndex = cursor.getColumnIndex(DateChangeTime.NEXT_DATE_CHANGE_TIME);
            long nextAlarmTime = cursor.getLong(colIndex);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.RTC, nextAlarmTime, AlarmManager.INTERVAL_DAY, getPendingIntent(context));
            dumpLog(nextAlarmTime);
        }
        cursor.close();
    }

    private void stopAlarm(final Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getPendingIntent(context));
    }

    private PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(ACTION_APPWIDGET_UPDATE);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void dumpLog(long timeInMillis) {
        if (BuildConfig.DEBUG) {
            Calendar time = Calendar.getInstance();
            time.setTimeInMillis(timeInMillis);
            MessageFormat mf = new MessageFormat("{0,date,yyyy/MM/dd HH:mm:ss}");
            Object[] objs = {time.getTime()};
            String result = mf.format(objs);
            Log.v("LOG_KEEPDO:" + "TasksWidgetProvider", "time:" + result);
        }
    }
}
