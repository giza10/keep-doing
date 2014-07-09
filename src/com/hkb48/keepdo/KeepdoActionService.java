package com.hkb48.keepdo;

import com.hkb48.keepdo.KeepdoProvider.DateChangeTime;
import com.hkb48.keepdo.KeepdoProvider.TaskCompletion;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;

public class KeepdoActionService extends IntentService {
    private static final String SERVICE_NAME = "KeepdoActionService";
    private static final String PACKAGE_NAME = KeepdoActionService.class.getPackage().getName();
    public static final String INTENT_EXTRA_MESSAGE_ID = PACKAGE_NAME + ".intent_extra_task_id";

    public KeepdoActionService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long taskId = intent.getLongExtra(INTENT_EXTRA_MESSAGE_ID, 0);
        ContentValues contentValues = new ContentValues();
        contentValues.put(TaskCompletion.TASK_NAME_ID, taskId);
        contentValues.put(TaskCompletion.TASK_COMPLETION_DATE, getTodayDate());
        getContentResolver().insert(TaskCompletion.CONTENT_URI, contentValues);
        NotificationHelper.cancelReminder(this);
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
