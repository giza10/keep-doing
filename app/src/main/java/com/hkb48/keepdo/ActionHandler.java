package com.hkb48.keepdo;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;

import com.hkb48.keepdo.KeepdoProvider.TaskCompletion;
import com.hkb48.keepdo.widget.TasksWidgetProvider;

public class ActionHandler extends IntentService {
    private static final String SERVICE_NAME = "ActionHandler";
    private static final String PACKAGE_NAME = ActionHandler.class.getPackage().getName();
    public static final String INTENT_EXTRA_TASK_ID = PACKAGE_NAME + ".intent_extra_task_id";

    public ActionHandler() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        assert intent != null;
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
        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(getApplicationContext());
        return dbAdapter.getTodayDate();
    }
}
