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
    public static final String CLICK_ACTION = "com.hkb48.keepdo.widget.CLICK";
    public static final String ACTION_ON_DATE_CHANGED = "com.hkb48.keepdo.ON_DATE_CHANGED";
//    public static String REFRESH_ACTION = "com.hkb48.keepdo.widget.REFRESH";
//    public static String EXTRA_DAY_ID = "com.hkb48.keepdo.widget.day";

//    private static HandlerThread sWorkerThread;
//    private static Handler sWorkerQueue;
//    private static TasksDataProviderObserver sDataObserver;
//    private static final int sMaxDegrees = 96;
    private ContentObserver mContentObserver;

    private boolean mIsLargeLayout = true;
//    private int mHeaderWeatherState = 0;

    public TasksWidgetProvider() {
        // Start the worker thread
//        sWorkerThread = new HandlerThread("TasksWidget-worker");
//        sWorkerThread.start();
//        sWorkerQueue = new Handler(sWorkerThread.getLooper());
//        sWorkerQueue = new Handler();
    }

    @Override
    public void onEnabled(Context context) {
        // Register for external updates to the data to trigger an update of the widget.  When using
        // content providers, the data is often updated via a background service, or in response to
        // user interaction in the main app.  To ensure that the widget always reflects the current
        // state of the data, we must listen for changes and update ourselves accordingly.
//        final ContentResolver r = context.getContentResolver();
//        if (sDataObserver == null) {
//            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
//            final ComponentName cn = new ComponentName(context, TasksWidgetProvider.class);
//            sDataObserver = new TasksDataProviderObserver(mgr, cn, sWorkerQueue);
//            r.registerContentObserver(KeepdoProvider.BASE_CONTENT_URI, true, sDataObserver);
//        }
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
//        if (action.equals(REFRESH_ACTION)) {
//            // BroadcastReceivers have a limited amount of time to do work, so for this sample, we
//            // are triggering an update of the data on another thread.  In practice, this update
//            // can be triggered from a background service, or perhaps as a result of user actions
//            // inside the main application.
//            final Context context = ctx;
//            sWorkerQueue.removeMessages(0);
//            sWorkerQueue.post(new Runnable() {
////                @Override
//                public void run() {
//                    final ContentResolver r = context.getContentResolver();
//                    final Cursor c = r.query(KeepdoProvider.CONTENT_URI, null, null, null, 
//                            null);
//                    final int count = c.getCount();
//
//                    // We disable the data changed observer temporarily since each of the updates
//                    // will trigger an onChange() in our data observer.
//                    r.unregisterContentObserver(sDataObserver);
//                    for (int i = 0; i < count; ++i) {
//                        final Uri uri = ContentUris.withAppendedId(KeepdoProvider.CONTENT_URI, i);
//                        final ContentValues values = new ContentValues();
//                        values.put(DummyWeatherDataProvider.Columns.TEMPERATURE,
//                                new Random().nextInt(sMaxDegrees));
//                        r.update(uri, values, null, null);
//                    }
//                    r.registerContentObserver(KeepdoProvider.CONTENT_URI, true, sDataObserver);
//
//                    final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
//                    final ComponentName cn = new ComponentName(context, TasksWidget.class);
//                    mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.weather_list);
//                }
//            });
//
////            final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
////                    AppWidgetManager.INVALID_APPWIDGET_ID);
//        } else if (action.equals(CLICK_ACTION)) {
        if (action.equals(CLICK_ACTION)) {
            // Show a toast
//            final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
//                    AppWidgetManager.INVALID_APPWIDGET_ID);
            // Launch top activity of KeepDo
            registerContentObserver(context);
            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            final ComponentName cn = new ComponentName(context, TasksWidgetProvider.class);
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.task_list);
            Intent activityLaunchIntent = new Intent(context, TasksActivity.class);
            activityLaunchIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityLaunchIntent);
        } else if (action.equals(ACTION_ON_DATE_CHANGED)) {
            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            final ComponentName cn = new ComponentName(context, TasksWidgetProvider.class);
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.task_list);
        }

        super.onReceive(context, intent);
    }

	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Update each of the widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; ++i) {
            RemoteViews layout = buildLayout(context, appWidgetIds[i], mIsLargeLayout);
            appWidgetManager.updateAppWidget(appWidgetIds[i], layout);
        }
        startAlarm(context);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId, Bundle newOptions) {

//        int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
//        int maxWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        int minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
//        int maxHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);

        RemoteViews layout;
        if (minHeight < 100) {
            mIsLargeLayout = false;
        } else {
            mIsLargeLayout = true;
        }
        layout = buildLayout(context, appWidgetId, mIsLargeLayout);
        appWidgetManager.updateAppWidget(appWidgetId, layout);
    }

    private RemoteViews buildLayout(Context context, int appWidgetId, boolean largeLayout) {
        RemoteViews rv;
//        if (largeLayout) {
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
            final Intent onClickIntent = new Intent(context, TasksWidgetProvider.class);
            onClickIntent.setAction(TasksWidgetProvider.CLICK_ACTION);
            onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            onClickIntent.setData(Uri.parse(onClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
            final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0,
                    onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.task_list, onClickPendingIntent);

            rv.setOnClickPendingIntent(R.id.empty_view, onClickPendingIntent);

            // Bind the click intent for the refresh button on the widget
//            final Intent refreshIntent = new Intent(context, TasksWidget.class);
//            refreshIntent.setAction(TasksWidget.REFRESH_ACTION);

            // Restore the minimal header
//            rv.setTextViewText(R.id.city_name, context.getString(R.string.city_name));
//        } else {
//            rv = new RemoteViews(context.getPackageName(), R.layout.tasks_widget);

            // Update the header to reflect the weather for "today"
//            Cursor c = context.getContentResolver().query(KeepdoProvider.CONTENT_URI, null,
//                    null, null, null);
//            if (c.moveToPosition(0)) {
//                int tempColIndex = c.getColumnIndex(KeepdoProvider.Columns.TASK_NAME);
//                int temp = c.getInt(tempColIndex);
//                String formatStr = context.getResources().getString(R.string.header_format_string);
//                String header = String.format(formatStr, temp,
//                        context.getString(R.string.city_name));
//                rv.setTextViewText(R.id.city_name, header);
//            }
//            c.close();
//        }
        return rv;
    }

    private void registerContentObserver(final Context context) {
        if (mContentObserver == null) {
            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            final ComponentName cn = new ComponentName(context, TasksWidgetProvider.class);
            mContentObserver = new TasksDataProviderObserver(mgr, cn, new Handler());
        }
        context.getContentResolver().registerContentObserver(KeepdoProvider.BASE_CONTENT_URI, true, mContentObserver);
    }

    private void unregisterContentObserver(final Context context) {
        if (mContentObserver != null) {
            context.getContentResolver().unregisterContentObserver(mContentObserver);
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
        Intent intent = new Intent(ACTION_ON_DATE_CHANGED);
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
