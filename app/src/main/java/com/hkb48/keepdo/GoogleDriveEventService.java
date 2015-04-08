package com.hkb48.keepdo;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.drive.events.CompletionEvent;
import com.google.android.gms.drive.events.DriveEventService;

public class GoogleDriveEventService extends DriveEventService {
    private final String TAG = "GoogleDriveEventService";

    public static final String CONFLICT_RESOLVED = "com.hkb48.keepdo.CONFLICT_RESOLVED";
    public static final String COMMIT_COMPLETED = "com.hkb48.keepdo.COMMIT_COMPLETED";
    public static final String COMMIT_COMPLETED_KEY = "commitCompleted";

    @Override
    public void onCompletion(CompletionEvent event) {
        Log.d(TAG, "Action completed with status: " + event.getStatus());

        if (event.getStatus() == CompletionEvent.STATUS_SUCCESS) {
            // Commit completed successfully.
            sendResult(this, COMMIT_COMPLETED, event.getDriveId().encodeToString(), COMMIT_COMPLETED_KEY);
        }

        // Once CompletionEvent is handled, dismiss it.
        event.dismiss();
    }

    /**
     * Notify the UI that the list should be updated
     *
     * @param resolution Resolved grocery list.
     */
    private void sendResult(Context context, String action, String resolution, String key) {
        LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(context);
        Intent intent = new Intent(action);
        intent.putExtra(key, resolution);
        broadcaster.sendBroadcast(intent);
    }
}
