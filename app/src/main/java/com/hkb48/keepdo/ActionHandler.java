package com.hkb48.keepdo;

import com.hkb48.keepdo.KeepdoProvider.DateChangeTime;
import com.hkb48.keepdo.KeepdoProvider.TaskCompletion;
import com.hkb48.keepdo.widget.TasksWidgetProvider;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;

public class ActionHandler extends IntentService {
    private static final String SERVICE_NAME = "ActionHandler";
    private static final String PACKAGE_NAME = ActionHandler.class.getPackage().getName();
    public static final String INTENT_EXTRA_TASK_ID = PACKAGE_NAME + ".intent_extra_task_id";

    public ActionHandler() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long taskId = intent.getLongExtra(INTENT_EXTRA_TASK_ID, 0);
        ContentValues contentValues = new ContentValues();
        contentValues.put(TaskCompletion.TASK_NAME_ID, taskId);
        contentValues.put(TaskCompletion.TASK_COMPLETION_DATE, getTodayDate());
        getContentResolver().insert(TaskCompletion.CONTENT_URI, contentValues);

        // Dismiss notification on wearable
        NotificationController.cancelReminder(this, taskId);

        // Dismiss notification on handheld
        NotificationController.cancelReminder(this, NotificationController.NOTIFICATION_ID_HANDHELD);

        // Update App widget
        TasksWidgetProvider.notifyDatasetChanged(getApplicationContext());
    }

    private String getTodayDate() {
        String date = "";
        Cursor cursor = getContentResolver().query(DateChangeTime.CONTENT_URI, null, null,
                null, null);
        if (cursor.moveToFirst()) {
            final int dateColIndex = cursor.getColumnIndex(DateChangeTime.ADJUSTED_DATE);
            date = cursor.getString(dateColIndex);
        }
        cursor.close();
        return date;
    }
    
}
