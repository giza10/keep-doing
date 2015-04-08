package com.hkb48.keepdo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.ExecutionOptions;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class GoogleDriveServicesActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "GoogleDriveActivity";

    private static final int REQUEST_CODE_RESOLUTION = 1;
    private static final String KEY_IN_RESOLUTION = "is_in_resolution";
    private static final String DRIVE_FOLDER_NAME = "hkb_keepdo";
    private static final String DRIVE_FILE_NAME = "keepdo.db";
    private static final String DRIVE_FILE_MIME_TYPE = "application/octet-stream";

    private DatabaseAdapter mDBAdapter = null;
    protected BroadcastReceiver mBroadcastReceiver;
    private boolean mIsInResolution = false;
    private GoogleApiClient mGoogleApiClient;
    private DriveId mClientDataFolderDriveId = null;
    private DriveFile driveFile = null;
    private ProgressBar mSpinner = null;
    private ProgressDialog mProgressDialog = null;

    /**
     * Called when the activity is starting. Restores the activity state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);

        mSpinner = (ProgressBar)findViewById(R.id.progressBarDrive);
        mSpinner.setVisibility(View.VISIBLE);

        if (savedInstanceState != null) {
            Log.d(TAG, "saveInstanceStat is not null");
            mIsInResolution = savedInstanceState.getBoolean(KEY_IN_RESOLUTION, false);
        } else {
            mDBAdapter = DatabaseAdapter.getInstance(this);

            mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(GoogleDriveEventService.COMMIT_COMPLETED)) {
                        // Receives DriveId
                        String resolvedStr = intent.getStringExtra(GoogleDriveEventService.COMMIT_COMPLETED_KEY);
                        Log.d(TAG, "Received resourceId is " + DriveId.decodeFromString(resolvedStr).toString());
                        finishActivity();
                    }
                }
            };
        }
    }

    /**
     * Called when the Activity is made visible.
     * A connection to Play Services need to be initiated as
     * soon as the activity is visible. Registers {@code ConnectionCallbacks}
     * and {@code OnConnectionFailedListener} on the
     * activities itself.
     */
    @Override
    protected void onStart() {
        super.onStart();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    //.addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(GoogleDriveEventService.COMMIT_COMPLETED));
    }

    /**
     * Called when activity gets invisible. Connection to Play Services needs to
     * be disconnected as soon as an activity is invisible.
     */
    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }

        if (mBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        }

        super.onStop();
    }

    /**
     * Saves the resolution state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IN_RESOLUTION, mIsInResolution);
    }

    /**
     * Handles Google Play Services resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                retryConnecting();
                break;
        }
    }

    private void retryConnecting() {
        mIsInResolution = false;
        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Called when {@code mGoogleApiClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Drive.DriveApi.requestSync(getGoogleApiClient()).setResultCallback(syncCallback);
    }

    /**
     * Called when {@code mGoogleApiClient} connection is suspended.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        retryConnecting();
    }

    /**
     * Called when {@code mGoogleApiClient} is trying to connect but failed.
     * Handle {@code result.getResolution()} if there is a resolution
     * available.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "onConnectionFailed(): " + result.toString());

        if (!result.hasResolution()) {
            // Show a localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(
                    result.getErrorCode(), this, 0, new OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            retryConnecting();
                        }
                    }).show();
            return;
        }

        // If there is an existing resolution error being displayed or a resolution
        // activity has started before, do nothing and wait for resolution
        // progress to be completed.
        if (mIsInResolution) {
            return;
        }

        mIsInResolution = true;
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity: " + e.getLocalizedMessage());
            retryConnecting();
        }
    }

    // Callback when requested sync returns.
    private ResultCallback<Status> syncCallback = new ResultCallback<Status>() {
        @Override
        public void onResult(Status status) {
            if (!status.isSuccess()) {
                Log.e(TAG, "Unable to sync.");
            }

            Query query = new Query.Builder()
                    .addFilter(Filters.and(
                            Filters.eq(SearchableField.MIME_TYPE, DRIVE_FILE_MIME_TYPE),
                            Filters.eq(SearchableField.TITLE, DRIVE_FILE_NAME),
                            Filters.eq(SearchableField.STARRED, true),
                            Filters.eq(SearchableField.TRASHED, false))).build();
            Drive.DriveApi.query(getGoogleApiClient(), query).setResultCallback(metadataCallback);
        }
    };

    private ResultCallback<DriveApi.MetadataBufferResult> metadataCallback =
        new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                if (!metadataBufferResult.getStatus().isSuccess()) {
                    showMessage("Problem while retrieving results");
                    return;
                }

                int results = metadataBufferResult.getMetadataBuffer().getCount();
                if (results > 0) {
                    // If the file exists then use it.
                    DriveId driveId = metadataBufferResult.getMetadataBuffer().get(0).getDriveId();
                    driveFile = Drive.DriveApi.getFile(getGoogleApiClient(), driveId);
                    //driveFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).setResultCallback(driveContentsCallback);
                    Log.d(TAG, "Found metadata DriveId: " + driveId.toString() + " , to edit");
                    new UpdateClientDataAsyncTask(GoogleDriveServicesActivity.this).execute(driveFile);
                } else {
                    Log.d(TAG, "Create new client drive file");
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(DRIVE_FOLDER_NAME).build();
                    Drive.DriveApi.getRootFolder(getGoogleApiClient()).createFolder(
                            //Drive.DriveApi.getAppFolder(getGoogleApiClient()).createFolder(
                            getGoogleApiClient(), changeSet).setResultCallback(folderCreatedCallback);
                }
            }
        };

    public class UpdateClientDataAsyncTask extends ApiClientAsyncTask<DriveFile, Void, Boolean> {
        public UpdateClientDataAsyncTask(Context context) {
            super(context);
        }

        @Override
        protected Boolean doInBackgroundConnected(DriveFile... args) {
            Log.i(TAG, "doInBackgroundConnected()...");

            DriveFile file = args[0];
            try {
                DriveApi.DriveContentsResult driveContentsResult = file.open(
                        getGoogleApiClient(), DriveFile.MODE_WRITE_ONLY, null).await();
                if (!driveContentsResult.getStatus().isSuccess()) {
                    Log.i(TAG, "doInBackgroundConnected() failed");
                    return false;
                }

                ExecutionOptions executionOptions = new ExecutionOptions.Builder().setNotifyOnCompletion(true)
                        .build();

                DriveContents driveContents = driveContentsResult.getDriveContents();
                OutputStream outputStream = driveContents.getOutputStream();
                BufferedInputStream in = new BufferedInputStream(mDBAdapter.readDatabaseStream());
                byte[] buffer = new byte[1024];
                while (in.read(buffer) >= 0) {
                    outputStream.write(buffer);
                }

                /**
                 * Toggle to send back a completion event
                 */
                com.google.android.gms.common.api.Status status =
                        driveContents.commit(getGoogleApiClient(), null, executionOptions).await();

                in.close();

                return status.getStatus().isSuccess();
            } catch (IOException e) {
                Log.e(TAG, "IOException while appending to the output stream", e);
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                showMessage("Error while editing contents");
                finishActivity();
                return;
            }

            mSpinner.setVisibility(View.GONE);
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            //finishActivity();
            showMessage(getResources().getString(R.string.backup_done));
        }
    }

    final private ResultCallback<DriveFolder.DriveFolderResult> folderCreatedCallback = new ResultCallback<DriveFolder.DriveFolderResult>() {
        @Override
        public void onResult(DriveFolder.DriveFolderResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Error while trying to create the folder");
                return;
            }

            mClientDataFolderDriveId = result.getDriveFolder().getDriveId();
            showMessage("Created new folder");

            // create new contents resource
            Drive.DriveApi.newDriveContents(getGoogleApiClient()).setResultCallback(driveContentsCallback);
        }
    };

    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(DriveApi.DriveContentsResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Error while trying to create new contents");
                return;
            }

            DriveFolder folder = Drive.DriveApi.getFolder(getGoogleApiClient(), mClientDataFolderDriveId);
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(DRIVE_FILE_NAME)
                    .setMimeType(DRIVE_FILE_MIME_TYPE)
                    .setStarred(true).build();
            folder.createFile(getGoogleApiClient(), changeSet, result.getDriveContents())
                    .setResultCallback(fileCreatedCallback);
        }
    };

    final private ResultCallback<DriveFolder.DriveFileResult> fileCreatedCallback = new ResultCallback<DriveFolder.DriveFileResult>() {
        @Override
        public void onResult(DriveFolder.DriveFileResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Error while trying to create the file");
                return;
            }

            showMessage("Created new file");

            // Fed content to the empty file have created
            DriveFile file = Drive.DriveApi.getFile(getGoogleApiClient(), result.getDriveFile().getDriveId());
            new UpdateClientDataAsyncTask(GoogleDriveServicesActivity.this).execute(file);
        }
    };

    /**
     * Terminate the Google Drive activity
     */
    private void finishActivity() {
        GoogleDriveServicesActivity.this.finish();
    }

    /**
     * Shows a toast message.
     */
    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Getter for the {@code GoogleApiClient}.
     */
    private GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }
}
